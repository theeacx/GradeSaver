package com.example.gradesaver

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.gradesaver.database.dao.AppDao
import com.example.gradesaver.notifications.NotificationReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Date

class GradeSaverApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Any initial setup if needed
    }

    fun scheduleTodaysNotificationsForUser(userId: Int, dao: AppDao) {
        GlobalScope.launch(Dispatchers.IO) {
            val today = Date()
            val startOfDay = today.time - (today.time % (24 * 60 * 60 * 1000))
            val endOfDay = startOfDay + (24 * 60 * 60 * 1000)

            val personalActivities = dao.getPersonalActivitiesByDay(userId, Date(startOfDay), Date(endOfDay))
            val generalActivities = dao.getActivitiesForProfessorByDay(userId, Date(startOfDay), Date(endOfDay))

            personalActivities.forEach { activity ->
                scheduleNotification(activity.activityName, "Personal", activity.dueDate)
            }

            generalActivities.forEach { activity ->
                scheduleNotification(activity.activityName, activity.activityType, activity.dueDate)
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification(activityName: String, activityType: String, dueDate: Date) {
        val notificationIntent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("activityName", activityName)
            putExtra("activityType", activityType)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            activityName.hashCode(),
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            dueDate.time,
            pendingIntent
        )
    }
}
