package com.example.gradesaver.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "checkedActivities",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("userId"),
            childColumns = arrayOf("userId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Activity::class,
            parentColumns = arrayOf("activityId"),
            childColumns = arrayOf("activityId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonalActivity::class,
            parentColumns = arrayOf("personalActivityId"),
            childColumns = arrayOf("personalActivityId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Reminder::class,
            parentColumns = arrayOf("reminderId"),
            childColumns = arrayOf("reminderId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CheckedActivity(
    @PrimaryKey(autoGenerate = true) val checkedActivityId: Int = 0,
    val userId: Int,
    val activityId: Int? = null,
    val personalActivityId: Int? = null,
    val reminderId: Int? = null,
    val isChecked: Boolean
)
