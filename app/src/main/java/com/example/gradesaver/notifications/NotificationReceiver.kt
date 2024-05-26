//package com.example.gradesaver.notifications
//
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import androidx.core.app.NotificationCompat
//import com.example.gradesaver.ProfessorCalendarActivity
//import com.example.gradesaver.R
//
//class NotificationReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent) {
//        Log.d("NotificationReceiver", "Notification received")
//
//        val activityId = intent.getIntExtra("activityId", -1)
//        val activityName = intent.getStringExtra("activityName")
//        val activityType = intent.getStringExtra("activityType")
//        val isPersonal = intent.getBooleanExtra("isPersonal", false)
//
//        val reminderId = intent.getIntExtra("reminderId", -1)
//        val reminderMessage = intent.getStringExtra("message")
//
//        if (activityName == null && reminderMessage == null) {
//            Log.e("NotificationReceiver", "Invalid data received")
//            return
//        }
//
//        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val notificationIntent = Intent(context, ProfessorCalendarActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(
//            context,
//            if (activityId != -1) activityId else reminderId,
//            notificationIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val doneIntent = Intent(context, MarkAsDoneReceiver::class.java).apply {
//            putExtra("activityId", if (!isPersonal && activityId != -1) activityId else -1)
//            putExtra("personalActivityId", if (isPersonal) activityId else -1)
//            putExtra("reminderId", if (reminderId != -1) reminderId else -1)
//        }
//        val donePendingIntent = PendingIntent.getBroadcast(
//            context,
//            if (activityId != -1) activityId else reminderId,
//            doneIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val notificationBuilder = NotificationCompat.Builder(context, if (activityId != -1) "ActivityDeadlineChannel" else "ReminderChannel")
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle(if (activityId != -1) "Activity Deadline" else "Reminder")
//            .setContentText(if (activityId != -1) "Deadline for $activityName ($activityType)" else reminderMessage)
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)
//            .addAction(R.drawable.ic_done, "Mark as Done", donePendingIntent)
//
//        notificationManager.notify(if (activityId != -1) activityId else reminderId, notificationBuilder.build())
//    }
//}
package com.example.gradesaver.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.gradesaver.ProfessorCalendarActivity
import com.example.gradesaver.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "Notification received")

        val activityId = intent.getIntExtra("activityId", -1)
        val activityName = intent.getStringExtra("activityName")
        val activityType = intent.getStringExtra("activityType")
        val isPersonal = intent.getBooleanExtra("isPersonal", false)

        val reminderId = intent.getIntExtra("reminderId", -1)
        val reminderMessage = intent.getStringExtra("message")

        val isActivityNotification = activityId != -1 && activityName != null && activityType != null
        val isReminderNotification = reminderId != -1 && reminderMessage != null

        if (!isActivityNotification && !isReminderNotification) {
            Log.e("NotificationReceiver", "Invalid data received")
            return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationIntent = Intent(context, ProfessorCalendarActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            if (isActivityNotification) activityId else reminderId,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val doneIntent = Intent(context, MarkAsDoneReceiver::class.java).apply {
            putExtra("activityId", if (isActivityNotification && !isPersonal) activityId else -1)
            putExtra("personalActivityId", if (isActivityNotification && isPersonal) activityId else -1)
            putExtra("reminderId", if (isReminderNotification) reminderId else -1)
            putExtra("userId", getUserIdFromPreferences(context)) // Ensure userId is included
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            if (isActivityNotification) activityId else reminderId,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, if (isActivityNotification) "ActivityDeadlineChannel" else "ReminderChannel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(if (isActivityNotification) "Activity Deadline" else "Reminder")
            .setContentText(if (isActivityNotification) "Deadline for $activityName ($activityType)" else reminderMessage)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_done, "Mark as Done", donePendingIntent)

        notificationManager.notify(if (isActivityNotification) activityId else reminderId, notificationBuilder.build())
    }

    private fun getUserIdFromPreferences(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1)
    }
}
