package com.yamanoglu.mobilecomputingexercise

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.google.android.gms.dynamic.SupportFragmentWrapper
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.activity_time.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var gMap: GoogleMap
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var selectedLocation: LatLng
    lateinit var geofencingClient: GeofencingClient


    val GEOFENCE_ID = "GEOFENCE_ID"
    val GEOFENCE_RADIUS = 500
    val GEOFENCE_EXPIRATION = 120 * 24 * 60 * 60 * 1000
    val GEOFENCE_DELAY = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        (map_fragment as SupportMapFragment).getMapAsync(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        map_create.setOnClickListener {

            val message = reminder_message.text.toString()
            if (message.isEmpty()) {
                toast("Message is needed!!")
                return@setOnClickListener
            }

            if (!::selectedLocation.isInitialized) {
                toast("Please select a location on map!!")
                return@setOnClickListener
            }

            val reminder = TableReminder(
                uuid = null,
                time = null,
                location = String.format(
                    "%.3f, %.3f",
                    selectedLocation.latitude,
                    selectedLocation.longitude
                ),
                message = message
            )

            doAsync {
                val db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "reminders"
                ).build()
                val uuid = db.reminderDao().insert(reminder).toInt()
                reminder.uuid = uuid
                db.close()
                createGeofence(selectedLocation, reminder, geofencingClient)

                finish()
            }
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        gMap = map ?: return

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            gMap.isMyLocationEnabled = true
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it == null)
                    return@addOnSuccessListener

                var latLong = LatLng(it.latitude, it.longitude)
                with(gMap) {
                    animateCamera(CameraUpdateFactory.newLatLngZoom(latLong, 13f))
                }

            }
        } else {
            var permissions = mutableListOf<String>()
            permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions.add(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 123)
        }
        gMap.setOnMapClickListener {
            with(gMap) {
                clear()
                animateCamera(CameraUpdateFactory.newLatLngZoom(it, 13f))


                val geocoder = Geocoder(applicationContext, Locale.getDefault())
                var title = ""
                var city = ""
                try {
                    val addressList = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    city = addressList.get(0).locality
                    title = addressList.get(0).getAddressLine(0)

                } catch (e: Exception) {

                }
                val marker = addMarker(MarkerOptions().position(it).snippet(title).title(city))
                marker.showInfoWindow()
                addCircle(
                    CircleOptions().center(it).strokeColor(
                        Color.argb(
                            50,
                            70,
                            70,
                            70
                        )
                    ).fillColor(Color.argb(100, 150, 150, 150)).radius(200.0)
                )
                selectedLocation = it
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED)) {
                toast("All permissions are required")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (grantResults.isNotEmpty() && grantResults[2] == PackageManager.PERMISSION_DENIED) {
                    toast("All permissions are required")
                }
            }
        }

    }

    private fun createGeofence(
        selectedLocation: LatLng,
        reminder: TableReminder,
        geofencingClient: GeofencingClient
    ) {
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(
                selectedLocation.latitude,
                selectedLocation.longitude,
                GEOFENCE_RADIUS.toFloat()
            ).setExpirationDuration(GEOFENCE_EXPIRATION.toLong())
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
            .setLoiteringDelay(GEOFENCE_DELAY).build()

        val geofenceRequest =
            GeofencingRequest.Builder().apply {
                setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                addGeofence(geofence)
            }.build()

        val intent = Intent(this, GeofenceReceiver::class.java).putExtra("uuid", reminder.uuid)
            .putExtra("message", reminder.message).putExtra("location", reminder.location)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        geofencingClient.addGeofences(geofenceRequest, pendingIntent)
    }
}
