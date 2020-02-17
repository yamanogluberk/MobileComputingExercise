package com.yamanoglu.mobilecomputingexercise

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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
