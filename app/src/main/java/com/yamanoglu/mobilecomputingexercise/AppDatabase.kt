package com.yamanoglu.mobilecomputingexercise

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TableReminder::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao

}