package com.example.gradesaver

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.adapters.ReminderAdapter
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.Activity
import com.example.gradesaver.database.entities.User
import kotlinx.coroutines.launch

class ManageRemindersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_reminders)

        val activity = intent.getSerializableExtra("ACTIVITY") as Activity?
        val user = intent.getSerializableExtra("USER_DETAILS") as User?

        if (activity == null || user == null) {
            finish() // Finish if activity or user details are not properly passed
            return
        }

        val db = AppDatabase.getInstance(this)
        val activityNameTextView: TextView = findViewById(R.id.activityNameTextView)
        val scheduleTextView: TextView = findViewById(R.id.scheduleTextView)
        val numberOfRemindersTextView: TextView = findViewById(R.id.numberOfRemindersTextView)
        val remindersListView: ListView = findViewById(R.id.remindersListView)
        val reminderMessageEditText: EditText = findViewById(R.id.reminderMessageEditText)
        val updateButton: Button = findViewById(R.id.updateScheduleButton)
        val deleteButton: Button = findViewById(R.id.deleteScheduleButton)
        val returnButton: Button = findViewById(R.id.returnButton)

        var currentScheduleId: Int? = null

        lifecycleScope.launch {
            val schedule = db.appDao().getLatestReminderScheduleForUser(user.userId, activity.activityId)
            if (schedule != null) {
                currentScheduleId = schedule.reminderScheduleId
                val reminders = db.appDao().getRemindersBySchedule(schedule.reminderScheduleId)

                runOnUiThread {
                    activityNameTextView.text = "Reminders for ${activity.activityName}"
                    scheduleTextView.text = "The chosen schedule is: ${schedule.reminderType}"
                    if (schedule.reminderType == "Custom") {
                        numberOfRemindersTextView.visibility = View.VISIBLE
                        numberOfRemindersTextView.text = "Number of reminders: ${schedule.numberOfReminders}"
                    } else {
                        numberOfRemindersTextView.visibility = View.GONE
                    }
                    val adapter = ReminderAdapter(this@ManageRemindersActivity, reminders)
                    remindersListView.adapter = adapter
                    reminderMessageEditText.setText(reminders.firstOrNull()?.reminderMessage ?: "No message set")
                }
            }
        }

        updateButton.setOnClickListener {
            // Handle schedule update
        }

        deleteButton.setOnClickListener {
            // Handle schedule deletion
        }

        returnButton.setOnClickListener {
            lifecycleScope.launch {
                currentScheduleId?.let { scheduleId ->
                    val reminders = db.appDao().getRemindersBySchedule(scheduleId)
                    val updatedMessage = reminderMessageEditText.text.toString()
                    reminders.forEach { reminder ->
                        reminder.reminderMessage = updatedMessage
                        db.appDao().updateReminder(reminder)
                    }
                }
                finish()  // Close the activity and return to previous screen
            }
        }
    }
}

