package com.yamanoglu.mobilecomputingexercise

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import org.jetbrains.anko.doAsync

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val uuid = intent.getIntExtra("uuid",0)
        val text = intent.getStringExtra("message")

        MainActivity.showNotification(context,text!!)

        doAsync {
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "reminders").build()
            db.reminderDao().deleteWithUuid(uuid)
            db.close()
        }
    }
}