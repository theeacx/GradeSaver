package com.example.gradesaver.database.entities
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*
@Entity(
    tableName = "activities",
    foreignKeys = [ForeignKey(
        entity = Course::class,
        parentColumns = arrayOf("courseId"),
        childColumns = arrayOf("courseId"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Activity(
    @PrimaryKey(autoGenerate = true) val activityId: Int = 0,
    val courseId: Int,
    val activityName: String,
    val activityType: String,
    val dueDate: Date
)
