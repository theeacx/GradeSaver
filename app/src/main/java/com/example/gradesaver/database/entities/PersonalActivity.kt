package com.example.gradesaver.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date

@Entity(
    tableName = "personalActivities",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("userId"),
            childColumns = arrayOf("userId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PersonalActivity(
    @PrimaryKey(autoGenerate = true) val personalActivityId: Int = 0,
    val userId: Int,  // Link to the user
    val activityName: String,
    val activityType: String,
    val dueDate: Date,
): Serializable

