package com.example.gradesaver.dataClasses

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.example.gradesaver.database.entities.Activity

data class ActivityWithCourse(
    @Embedded val activity: Activity,
    @ColumnInfo(name = "courseName") val courseName: String
)
