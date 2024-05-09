package com.example.gradesaver.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.Activity

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Extract the activity ID and course ID from the input data
        val activityId = inputData.getInt("activityId", -1)
        val courseId = inputData.getInt("courseId", -1)

        // Validate input data
        if (activityId == -1 || courseId == -1) {
            return Result.failure()
        }

        // Get the database instance
        val db = AppDatabase.getInstance(applicationContext)

        // This example assumes you have a way to get the current user ID
        val currentUserId = getCurrentUserId()
        val enrollments = db.appDao().getEnrollmentsByStudent(currentUserId)
        val isEnrolled = enrollments.any { it.courseId == courseId }

        if (isEnrolled) {
            val activity = db.appDao().getActivityById(activityId)
            if (activity != null) {
                showNotification(activity)
                return Result.success()
            }
        }
        return Result.failure()
    }

    private fun showNotification(activity: Activity) {
        // Logic to show notification using NotificationManager
    }

    private fun getCurrentUserId(): Int {
        // Implementation to retrieve the current logged-in user's ID
        return -1 // Placeholder for actual implementation
    }
}
