package com.example.gradesaver.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.CheckedActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MarkAsDoneReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val activityId = intent.getIntExtra("activityId", -1)
        val personalActivityId = intent.getIntExtra("personalActivityId", -1)
        val reminderId = intent.getIntExtra("reminderId", -1)
        val userId = intent.getIntExtra("userId", -1)

        Log.d("MarkAsDoneReceiver", "Received data - activityId: $activityId, personalActivityId: $personalActivityId, reminderId: $reminderId, userId: $userId")

        if (userId == -1) {
            Log.e("MarkAsDoneReceiver", "User ID not found in preferences")
            return
        }

        GlobalScope.launch {
            val dao = AppDatabase.getInstance(context).appDao()

            try {
                when {
                    activityId != -1 -> {
                        val alreadyChecked = dao.getCheckedActivityByUserAndActivity(userId, activityId)
                        if (alreadyChecked == null) {
                            dao.insertCheckedActivity(
                                CheckedActivity(userId = userId, activityId = activityId, isChecked = true)
                            )
                        }
                    }
                    personalActivityId != -1 -> {
                        val personalActivity = dao.getPersonalActivityById(personalActivityId)
                        if (personalActivity != null) {
                            val alreadyChecked = dao.getCheckedActivityByUserAndPersonalActivity(userId, personalActivityId)
                            if (alreadyChecked == null) {
                                dao.insertCheckedActivity(
                                    CheckedActivity(userId = userId, personalActivityId = personalActivityId, isChecked = true)
                                )
                            }
                        } else {
                            Log.e("MarkAsDoneReceiver", "Personal activity not found with ID: $personalActivityId")
                        }
                    }
                    reminderId != -1 -> {
                        val alreadyChecked = dao.getCheckedActivityByUserAndReminder(userId, reminderId)
                        if (alreadyChecked == null) {
                            dao.insertCheckedActivity(
                                CheckedActivity(userId = userId, reminderId = reminderId, isChecked = true)
                            )
                        }
                    }
                    else -> {
                        Log.e("MarkAsDoneReceiver", "Invalid data: no valid ID to insert checked activity")
                    }
                }

                with(NotificationManagerCompat.from(context)) {
                    when {
                        activityId != -1 -> cancel(activityId)
                        personalActivityId != -1 -> cancel(personalActivityId)
                        reminderId != -1 -> cancel(reminderId)
                    }
                }
            } catch (e: Exception) {
                Log.e("MarkAsDoneReceiver", "Error inserting checked activity", e)
            }
        }
    }
}
