package com.example.gradesaver.workers

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.gradesaver.database.entities.Activity
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    fun scheduleNotification(activity: Activity) {
        val workManager = WorkManager.getInstance(context)

        // Create the input data for the worker
        val inputData = workDataOf(
            "activityId" to activity.activityId,
            "courseId" to activity.courseId
        )

        // Create a work request
        val notificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(inputData)
            .setInitialDelay(3, TimeUnit.MINUTES) // Delay of 3 minutes
            .build()

        // Enqueue the work
        workManager.enqueue(notificationWorkRequest)
    }
}
