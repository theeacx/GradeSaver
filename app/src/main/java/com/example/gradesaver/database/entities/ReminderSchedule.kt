package com.example.gradesaver.database.entities
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*
@Entity(
    tableName = "reminderSchedules",
    foreignKeys = [
        ForeignKey(
            entity = Activity::class,
            parentColumns = arrayOf("activityId"),
            childColumns = arrayOf("activityId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("userId"),
            childColumns = arrayOf("studentId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReminderSchedule(
    @PrimaryKey(autoGenerate = true) val reminderScheduleId: Int = 0,
    val activityId: Int,
    val studentId: Int,
    val reminderType: String,
    val startDate: Date,
    val endDate: Date?,
    val numberOfReminders: Int?
)
