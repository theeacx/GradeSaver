package com.example.gradesaver.dataClasses

data class ActivityExclusionInfo(
    val activityId: Int,
    val activityName: String,
    val day: Int,   // Use Int for day and month if they represent numerical values
    val month: Int
)

