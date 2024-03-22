package com.example.gradesaver.database.entities
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*
@Entity(
    tableName = "userActivities",
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
data class UserActivity(
    @PrimaryKey(autoGenerate = true) val userActivityId: Int = 0,
    val activityId: Int,
    val studentId: Int,
    val isCompleted: Boolean,
    val importanceLevel: Int?,
    val colorCode: String?
)
