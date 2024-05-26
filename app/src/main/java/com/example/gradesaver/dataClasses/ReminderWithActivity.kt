package com.example.gradesaver.dataClasses

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation
import com.example.gradesaver.database.entities.Activity
import com.example.gradesaver.database.entities.Reminder

data class ReminderWithActivity(
    @Embedded val reminder: Reminder,
    @ColumnInfo(name = "activityId") val activityId: Int,
    @Relation(
        parentColumn = "activityId",
        entityColumn = "activityId"
    )
    val activity: Activity?
)