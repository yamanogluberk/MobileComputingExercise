package com.yamanoglu.mobilecomputingexercise

import androidx.room.*

@Entity(tableName = "reminders")
data class TableReminder(
    @PrimaryKey(autoGenerate = true) var uuid: Int?,
    @ColumnInfo(name = "time") var time: Long?,
    @ColumnInfo(name = "location") var location: String?,
    @ColumnInfo(name = "message") var message: String
)

@Dao
interface ReminderDao{
    @Transaction @Insert
    fun insert(reminder: TableReminder)

    @Delete
    fun delete(reminder: TableReminder)

    @Query("SELECT * FROM reminders")
    fun getReminders():List<TableReminder>
}