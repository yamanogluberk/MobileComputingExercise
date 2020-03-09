package com.yamanoglu.mobilecomputingexercise

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.google.android.gms.dynamic.SupportFragmentWrapper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.activity_time.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback{

    lateinit var gMap: GoogleMap
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var selectedLocation: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        (map_fragment as SupportMapFragment).getMapAsync(this)

        map_create.setOnClickListener {

            val message = reminder_message.text.toString()
            if(message.isEmpty()){
                toast("Message is needed!!")
                return@setOnClickListener
            }

            if(!::selectedLocation.isInitialized){
                toast("Please select a location on map!!")
                return@setOnClickListener
            }

            val reminder = TableReminder(
                uuid = null,
                time = null,
                location = String.format("%.3f, %.3f", selectedLocation.latitude, selectedLocation.longitude),
                message = message
            )

            doAsync {
                val db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "reminders"
                ).build()
                db.reminderDao().insert(reminder)
                db.close()

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
            fusedLocationClient=LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it==null)
                    return@addOnSuccessListener

                var latLong = LatLng(it.latitude,it.longitude)
                with(gMap){
                    animateCamera(CameraUpdateFactory.newLatLngZoom(latLong,13f))
                }

            }
        }else{
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),123)
        }
        gMap.setOnMapClickListener {
            with(gMap){
                clear()
                animateCamera(CameraUpdateFactory.newLatLngZoom(it,13f))


                val geocoder = Geocoder(applicationContext, Locale.getDefault())
                var title = ""
                var city = ""
                try {
                    val addressList = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    city = addressList.get(0).locality
                    title = addressList.get(0).getAddressLine(0)

                }catch (e:Exception){

                }
                val marker = addMarker(MarkerOptions().position(it).snippet(title).title(city))
                marker.showInfoWindow()

                selectedLocation = it
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        startActivity(
            Intent(this,MapActivity::class.java)
        )
        finish()

    }
}
