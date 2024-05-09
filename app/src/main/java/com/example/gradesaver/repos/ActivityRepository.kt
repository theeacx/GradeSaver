package com.example.gradesaver.repos

import com.example.gradesaver.database.dao.AppDao
import com.example.gradesaver.database.entities.Activity
import com.example.gradesaver.workers.NotificationScheduler

class ActivityRepository(private val dao: AppDao, private val notificationScheduler: NotificationScheduler) {

    suspend fun insertActivityAndScheduleNotification(activity: Activity) {
        dao.insertActivity(activity) // Insert the activity
        notificationScheduler.scheduleNotification(activity) // Schedule the notification
    }
}
