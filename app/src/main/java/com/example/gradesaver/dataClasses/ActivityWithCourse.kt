package com.example.gradesaver.dataClasses

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gradesaver.database.entities.Activity
import com.example.gradesaver.database.entities.Course

data class ActivityWithCourse(
    @Embedded val activity: Activity,
    @Relation(
        parentColumn = "courseId",
        entityColumn = "courseId"
    )
    val course: Course
)
