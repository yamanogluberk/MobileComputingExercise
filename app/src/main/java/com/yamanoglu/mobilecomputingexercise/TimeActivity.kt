package com.yamanoglu.mobilecomputingexercise

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_time.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.util.*

class TimeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time)

        button.setOnClickListener {
            @Suppress("DEPRECATION") val calendar = GregorianCalendar(
                datepicker.year,
                datepicker.month,
                datepicker.dayOfMonth,
                timepicker.currentHour,
                timepicker.currentMinute
            )

            if (TextUtils.isEmpty(et_message.text.toString()) || calendar.timeInMillis <= System.currentTimeMillis()) {
                toast("Wrong Data!!")

            } else {
                val reminder = TableReminder(
                    uuid = null,
                    time = calendar.timeInMillis,
                    location = null,
                    message = et_message.text.toString()
                )

                doAsync {
                    val db = Room.databaseBuilder(
                        applicationContext,
                        AppDatabase::class.java,
                        "reminders"
                    ).build()
                    val uuid = db.reminderDao().insert(reminder).toInt()
                    db.close()

                    setAlarm(uuid, reminder.time!!, reminder.message)
                    finish()
                }
            }

        }
    }

    private fun setAlarm(uuid:Int ,time: Long, message: String) {
        val intent = Intent(this, ReminderReceiver::class.java)
        intent.putExtra("message", message)
        intent.putExtra("uuid", uuid)
        val pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_ONE_SHOT)

        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.setExact(AlarmManager.RTC, time, pendingIntent)

        runOnUiThread { toast("reminder is set") }
    }
}
