package com.example.gradesaver.dataClasses

data class PendingActivityCountByStudent(
    val activityName: String,
    val email: String,
    val pendingActivities: Int
)
