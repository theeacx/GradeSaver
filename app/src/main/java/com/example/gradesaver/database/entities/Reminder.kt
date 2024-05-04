package com.example.gradesaver.database.entities
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*
@Entity(
    tableName = "reminders",
    foreignKeys = [ForeignKey(
        entity = ReminderSchedule::class,
        parentColumns = arrayOf("reminderScheduleId"),
        childColumns = arrayOf("reminderScheduleId"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true) val reminderId: Int = 0,
    val reminderScheduleId: Int,
    val reminderDate: Date,
    var reminderMessage: String?
)
