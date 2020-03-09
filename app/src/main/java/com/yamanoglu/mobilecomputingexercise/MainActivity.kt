package com.yamanoglu.mobilecomputingexercise

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fab_time.setOnClickListener {
            startActivity(Intent(this, TimeActivity::class.java))
        }
        fab_map.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        var fabopened = false
        fab.setOnClickListener {
            if (!fabopened) {
                fabopened = true
                fab_time.animate().translationY(-resources.getDimension(R.dimen.standart_66))
                fab_map.animate().translationY(-resources.getDimension(R.dimen.standart_116))
            } else {
                fabopened = false
                fab_time.animate().translationY(0f)
                fab_map.animate().translationY(0f)

            }
        }

        lv_main.setOnItemClickListener{ _, _, pos ,_->
            val selected = lv_main.adapter.getItem(pos) as TableReminder

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Reminder?")
                .setMessage(selected.message)
                .setPositiveButton("Delete"){_,_ ->
                    if(selected.time!=null){
                        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val intent = Intent(this,ReminderReceiver::class.java)
                        val pending = PendingIntent.getBroadcast(this, selected.uuid!!,intent, PendingIntent.FLAG_ONE_SHOT)
                        manager.cancel(pending)

                        doAsync {
                            val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "reminders").build()
                            db.reminderDao().delete(selected)
                            db.close()
                            refreshList()
                        }
                    }
                }
                .setNegativeButton("Cancel"){dialog,_ ->
                    dialog.dismiss()
                }
                .show()

        }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList(){
        doAsync {
            val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "reminders")
                .build()
            val reminders = db.reminderDao().getReminders()
            db.close()
            uiThread {
                if (reminders.isEmpty()) {
                    toast("No Reminder Yet")
                }
                val adapter = ReminderAdapter(this@MainActivity, reminders)
                lv_main.adapter = adapter
            }
        }
    }
}
