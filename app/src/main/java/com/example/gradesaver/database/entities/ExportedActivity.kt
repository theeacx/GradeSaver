package com.example.gradesaver.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exportedActivities")
data class ExportedActivity(
    @PrimaryKey(autoGenerate = true) val exportedActivityId: Int = 0,
    val activityId: Int,
    val activityType: String // "university" or "personal"
)
