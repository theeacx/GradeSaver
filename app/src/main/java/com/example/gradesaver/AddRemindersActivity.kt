package com.example.gradesaver

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.dao.AppDao
import com.example.gradesaver.database.entities.Activity
import com.example.gradesaver.database.entities.Reminder
import com.example.gradesaver.database.entities.ReminderSchedule
import com.example.gradesaver.database.entities.User
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit


class AddRemindersActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var dao: AppDao
    private lateinit var timePicker: TimePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reminders)

        timePicker = findViewById(R.id.timePicker)
        timePicker.setIs24HourView(true)

        db = AppDatabase.getInstance(applicationContext)
        dao = db.appDao()

        val activity = intent.getSerializableExtra("ACTIVITY") as? Activity
        val user = intent.getSerializableExtra("USER_DETAILS") as? User

        val titleText: TextView = findViewById(R.id.titleText)
        titleText.text = if (activity != null) "Reminders for ${activity.activityName}" else "Reminders for Unknown Activity"

        val spinner: Spinner = findViewById(R.id.reminderTypeSpinner)
        val numberField: EditText = findViewById(R.id.numberField)
        val messageField: EditText = findViewById(R.id.messageField)
        val addReminderButton: Button = findViewById(R.id.addReminderButton)

        if (activity != null) {
            val activityName = activity.activityName

            // Set the TextView to include the name of the activity
            val titleText: TextView = findViewById(R.id.titleText)
            titleText.text = "Reminders for $activityName"



            // Initialize other UI elements
            initSpinner()
            handleSpinnerSelection()
        }

        addReminderButton.setOnClickListener {
            if (activity != null) {
                if (user != null) {
                    handleAddReminderButtonClick(spinner, numberField, messageField, activity, user)
                }
            }
        }
    }
    private fun initSpinner() {
        val spinner: Spinner = findViewById(R.id.reminderTypeSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.reminder_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    private fun handleSpinnerSelection() {
        val spinner: Spinner = findViewById(R.id.reminderTypeSpinner)
        val numberField: EditText = findViewById(R.id.numberField)
        val messageField: EditText = findViewById(R.id.messageField)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedType = parent.getItemAtPosition(position).toString()
                when (selectedType) {
                    "Custom" -> {
                        numberField.visibility = View.VISIBLE
                        messageField.hint = "Enter custom message for the reminders"
                    }
                    "Daily", "Weekly", "Monthly" -> {
                        numberField.visibility = View.GONE // Hide number field if not custom
                        messageField.hint = "Enter message for $selectedType reminders"
                    }
                    else -> {
                        numberField.visibility = View.GONE
                        messageField.hint = "Enter message"
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                numberField.visibility = View.GONE // Ensure it's hidden if nothing is selected
            }
        }
    }

    private fun handleAddReminderButtonClick(spinner: Spinner, numberField: EditText, messageField: EditText, activity: Activity, user: User) {
        val reminderType = spinner.selectedItem.toString()
        val numberOfReminders = if (reminderType == "Custom") {
            numberField.text.toString().toIntOrNull() ?: throw IllegalArgumentException("Please enter a valid number of reminders.")
        } else null
        val message = messageField.text.toString()

        // Ensure timePicker is initialized and activity is not null
        if (this::timePicker.isInitialized && activity != null && user != null) {
            val hour = timePicker.hour
            val minute = timePicker.minute
            if (reminderType == "Custom" && numberOfReminders == null) {
                Toast.makeText(this, "Please enter the number of reminders.", Toast.LENGTH_SHORT).show()
            } else {
                insertReminderSchedule(activity, user, reminderType, numberOfReminders, message, hour, minute)
            }
        }
    }

    private fun insertReminderSchedule(activity: Activity, user: User, reminderType: String, numberOfReminders: Int?, message: String?, hour: Int, minute: Int) {
        // Adjust calendar instance to include selected hour and minute
        val startTime = Calendar.getInstance().apply {
            time = Date() // Use current date but set specific hour and minute from timePicker
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        val newSchedule = ReminderSchedule(
            activityId = activity.activityId,
            studentId = user.userId,
            reminderType = reminderType,
            startDate = startTime.time,
            endDate = activity.dueDate,
            numberOfReminders = numberOfReminders
        )

        lifecycleScope.launch {
            try {
                val scheduleId = dao.insertReminderSchedule(newSchedule)
                val reminderDates = generateReminders(newSchedule, reminderType, numberOfReminders)
                reminderDates.forEach { date ->
                    val reminder = Reminder(
                        reminderScheduleId = scheduleId.toInt(),
                        reminderDate = date,
                        reminderMessage = message
                    )
                    dao.insertReminder(reminder)
                }
                runOnUiThread {
                    Toast.makeText(this@AddRemindersActivity, "Reminders added successfully!", Toast.LENGTH_LONG).show()
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@AddRemindersActivity, "Failed to add reminders: ${e.message}", Toast.LENGTH_LONG).show()
                }
                e.printStackTrace()
            }
        }
    }


//    private fun handleAddReminderButtonClick(spinner: Spinner, numberField: EditText, messageField: EditText, activity: Activity, user: User) {
//        val reminderType = spinner.selectedItem.toString()
//        val numberOfReminders = if (reminderType == "Custom") numberField.text.toString().toIntOrNull() else null
//        val message = if (reminderType == "Custom") messageField.text.toString() else ""
//
//        if (reminderType == "Custom" && numberOfReminders == null) {
//            Toast.makeText(this, "Please enter the number of reminders.", Toast.LENGTH_SHORT).show()
//        } else {
//            insertReminderSchedule(activity, user, reminderType, numberOfReminders, message)
//        }
//    }
//
//    private fun insertReminderSchedule(activity: Activity, user: User, reminderType: String, numberOfReminders: Int?, message: String?) {
////        val newSchedule = ReminderSchedule(
////            activityId = activity.activityId,
////            studentId = user.userId,
////            reminderType = reminderType,
////            startDate = Date(),
////            endDate = activity.dueDate,
////            numberOfReminders = numberOfReminders
////        )
////
////        lifecycleScope.launch {
////            try {
////                val scheduleId = dao.insertReminderSchedule(newSchedule)
////                val reminderDates = generateReminders(newSchedule, reminderType, numberOfReminders)
////                reminderDates.forEach { date ->
////                    val reminder = Reminder(
////                        reminderScheduleId = scheduleId.toInt(),
////                        reminderDate = date,
////                        reminderMessage = message
////                    )
////                    dao.insertReminder(reminder)
////                }
////                runOnUiThread {
////                    Toast.makeText(this@AddRemindersActivity, "Reminders added successfully!", Toast.LENGTH_LONG).show()
////                    finish()
////                }
////            } catch (e: Exception) {
////                runOnUiThread {
////                    Toast.makeText(this@AddRemindersActivity, "Failed to add reminders: ${e.message}", Toast.LENGTH_LONG).show()
////                }
////                e.printStackTrace()
////            }
////        }
//        Calendar startTime = Calendar.getInstance();
//        startTime.setTime(new Date()); // Start from current date and set the hour and minute from TimePicker
//        startTime.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
//        startTime.set(Calendar.MINUTE, timePicker.getMinute());
//
//        ReminderSchedule newSchedule = new ReminderSchedule(
//            activityId = activity.activityId,
//            studentId = user.userId,
//            reminderType = reminderType,
//            startDate = startTime.getTime(),
//            endDate = activity.dueDate,
//            numberOfReminders = numberOfReminders
//        );
//
//        lifecycleScope.launch {
//            try {
//                long scheduleId = dao.insertReminderSchedule(newSchedule);
//                List<Date> reminderDates = generateReminders(newSchedule, reminderType, numberOfReminders);
//                for (Date date : reminderDates) {
//                    Reminder reminder = new Reminder(
//                        reminderScheduleId = (int) scheduleId,
//                        reminderDate = date,
//                        reminderMessage = message
//                    );
//                    dao.insertReminder(reminder);
//                }
//                runOnUiThread(() -> {
//                    Toast.makeText(AddRemindersActivity.this, "Reminders added successfully!", Toast.LENGTH_LONG).show();
//                    finish();
//                });
//            } catch (Exception e) {
//                runOnUiThread(() -> Toast.makeText(AddRemindersActivity.this, "Failed to add reminders: " + e.getMessage(), Toast.LENGTH_LONG).show());
//                e.printStackTrace();
//            }
//        }
//    }


    private fun generateReminders(schedule: ReminderSchedule, reminderType: String, numberOfReminders: Int?): List<Date> {
        val reminderDates = mutableListOf<Date>()
        val startDate = schedule.startDate
        val endDate = schedule.endDate ?: return emptyList()

        when (reminderType) {
            "Daily" -> {
                var currentDate = startDate
                while (currentDate.before(endDate) || currentDate.equals(endDate)) {
                    reminderDates.add(currentDate)
                    currentDate = Date(currentDate.time + TimeUnit.DAYS.toMillis(1))
                }
            }
            "Weekly" -> {
                var currentDate = startDate
                while (currentDate.before(endDate) || currentDate.equals(endDate)) {
                    reminderDates.add(currentDate)
                    currentDate = Date(currentDate.time + TimeUnit.DAYS.toMillis(7))
                }
            }
            "Monthly" -> {
                val calendar = Calendar.getInstance()
                calendar.time = startDate
                while (calendar.time.before(endDate) || calendar.time.equals(endDate)) {
                    reminderDates.add(calendar.time)
                    calendar.add(Calendar.MONTH, 1)
                }
            }
            "Custom" -> {
                if (numberOfReminders != null && numberOfReminders > 0) {
                    val interval = (endDate.time - startDate.time) / numberOfReminders
                    var currentTime = startDate.time
                    for (i in 0 until numberOfReminders) {
                        reminderDates.add(Date(currentTime))
                        currentTime += interval
                    }
                }
            }
        }
        return reminderDates.map { date ->
            getNoonDate(date) // Ensure all dates are set to noon
        }
    }

    private fun getNoonDate(date: Date): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }
}