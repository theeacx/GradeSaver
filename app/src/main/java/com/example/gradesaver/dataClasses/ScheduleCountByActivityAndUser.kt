package com.example.gradesaver.dataClasses

data class ScheduleCountByActivityAndUser(
    val activityName: String,
    val email: String,
    val scheduleCount: Int
)
