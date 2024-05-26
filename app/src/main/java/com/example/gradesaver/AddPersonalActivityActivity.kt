//package com.example.gradesaver
//
//import android.annotation.SuppressLint
//import android.app.AlarmManager
//import android.app.DatePickerDialog
//import android.app.PendingIntent
//import android.app.TimePickerDialog
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import android.widget.EditText
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.lifecycleScope
//import com.example.gradesaver.database.AppDatabase
//import com.example.gradesaver.database.entities.PersonalActivity
//import com.example.gradesaver.notifications.NotificationReceiver
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Date
//import java.util.Locale
//
//class AddPersonalActivityActivity : AppCompatActivity() {
//    private lateinit var activityNameEditText: EditText
//    private lateinit var dueDateTextView: TextView
//    private lateinit var dueDateButton: Button
//    private lateinit var dueTimeTextView: TextView
//    private lateinit var dueTimeButton: Button
//    private lateinit var addPersonalActivityButton: Button
//
//    private var selectedDate: Calendar = Calendar.getInstance()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_add_personal_activity)
//
//        activityNameEditText = findViewById(R.id.activityNameEditText)
//        dueDateTextView = findViewById(R.id.dueDateTextView)
//        dueDateButton = findViewById(R.id.dueDateButton)
//        dueTimeTextView = findViewById(R.id.dueTimeTextView)
//        dueTimeButton = findViewById(R.id.dueTimeButton)
//        addPersonalActivityButton = findViewById(R.id.addPersonalActivityButton)
//
//        dueDateButton.setOnClickListener { showDatePickerDialog() }
//        dueTimeButton.setOnClickListener { showTimePickerDialog() }
//        addPersonalActivityButton.setOnClickListener { addPersonalActivity() }
//    }
//
//    private fun showDatePickerDialog() {
//        val calendar = Calendar.getInstance()
//        DatePickerDialog(this, R.style.PurpleDatePickerDialog, { _, year, month, dayOfMonth ->
//            selectedDate.set(Calendar.YEAR, year)
//            selectedDate.set(Calendar.MONTH, month)
//            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
//            updateDateInView()
//        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
//    }
//
//    private fun showTimePickerDialog() {
//        val calendar = Calendar.getInstance()
//        TimePickerDialog(this, R.style.PurpleTimePickerDialog, { _, hourOfDay, minute ->
//            selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
//            selectedDate.set(Calendar.MINUTE, minute)
//            updateTimeInView()
//        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
//    }
//
//    private fun updateDateInView() {
//        val myFormat = "yyyy-MM-dd" // mention the format you need
//        val sdf = SimpleDateFormat(myFormat, Locale.US)
//        dueDateTextView.text = sdf.format(selectedDate.time)
//    }
//
//    private fun updateTimeInView() {
//        val myFormat = "HH:mm" // mention the format you need
//        val sdf = SimpleDateFormat(myFormat, Locale.US)
//        dueTimeTextView.text = sdf.format(selectedDate.time)
//    }
//
//    private fun addPersonalActivity() {
//        val activityName = activityNameEditText.text.toString()
//        val userId = intent.getIntExtra("USER_ID", -1)
//
//        if (activityName.isEmpty() || userId == -1) {
//            Toast.makeText(this, "Please enter activity name and select a valid user.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val personalActivity = PersonalActivity(
//            userId = userId,
//            activityName = activityName,
//            activityType = "Personal",
//            dueDate = selectedDate.time
//        )
//
//        lifecycleScope.launch {
//            val dao = AppDatabase.getInstance(applicationContext).appDao()
//            val personalActivityId = dao.insertPersonalActivity(personalActivity)
//            withContext(Dispatchers.Main) {
//                scheduleNotification(personalActivityId.toInt(), activityName, "Personal", selectedDate.time, true)
//            }
//            Toast.makeText(this@AddPersonalActivityActivity, "Personal Activity added.", Toast.LENGTH_SHORT).show()
//            finish() // Return to the previous activity
//        }
//    }
//
//    @SuppressLint("ScheduleExactAlarm")
//    private fun scheduleNotification(activityId: Int, activityName: String, activityType: String, dueDate: Date, isPersonal: Boolean) {
//        val notificationIntent = Intent(this, NotificationReceiver::class.java).apply {
//            putExtra("activityId", activityId)
//            putExtra("activityName", activityName)
//            putExtra("activityType", activityType)
//            putExtra("isPersonal", isPersonal)
//        }
//        val pendingIntent = PendingIntent.getBroadcast(
//            this,
//            activityId,
//            notificationIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
//        alarmManager.setExact(
//            AlarmManager.RTC_WAKEUP,
//            dueDate.time,
//            pendingIntent
//        )
//    }
//}
package com.example.gradesaver

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.PersonalActivity
import com.example.gradesaver.notifications.NotificationReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddPersonalActivityActivity : AppCompatActivity() {
    private lateinit var activityNameEditText: EditText
    private lateinit var dueDateTextView: TextView
    private lateinit var dueDateButton: Button
    private lateinit var dueTimeTextView: TextView
    private lateinit var dueTimeButton: Button
    private lateinit var addPersonalActivityButton: Button

    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_personal_activity)

        activityNameEditText = findViewById(R.id.activityNameEditText)
        dueDateTextView = findViewById(R.id.dueDateTextView)
        dueDateButton = findViewById(R.id.dueDateButton)
        dueTimeTextView = findViewById(R.id.dueTimeTextView)
        dueTimeButton = findViewById(R.id.dueTimeButton)
        addPersonalActivityButton = findViewById(R.id.addPersonalActivityButton)

        dueDateButton.setOnClickListener { showDatePickerDialog() }
        dueTimeButton.setOnClickListener { showTimePickerDialog() }
        addPersonalActivityButton.setOnClickListener { addPersonalActivity() }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, R.style.PurpleDatePickerDialog, { _, year, month, dayOfMonth ->
            selectedDate.set(Calendar.YEAR, year)
            selectedDate.set(Calendar.MONTH, month)
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(this, R.style.PurpleTimePickerDialog, { _, hourOfDay, minute ->
            selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selectedDate.set(Calendar.MINUTE, minute)
            updateTimeInView()
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun updateDateInView() {
        val myFormat = "yyyy-MM-dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        dueDateTextView.text = sdf.format(selectedDate.time)
    }

    private fun updateTimeInView() {
        val myFormat = "HH:mm" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        dueTimeTextView.text = sdf.format(selectedDate.time)
    }

    private fun addPersonalActivity() {
        val activityName = activityNameEditText.text.toString()
        val userId = intent.getIntExtra("USER_ID", -1)

        if (activityName.isEmpty() || userId == -1) {
            Toast.makeText(this, "Please enter activity name and select a valid user.", Toast.LENGTH_SHORT).show()
            return
        }

        val personalActivity = PersonalActivity(
            userId = userId,
            activityName = activityName,
            activityType = "Personal",
            dueDate = selectedDate.time
        )

        lifecycleScope.launch {
            val dao = AppDatabase.getInstance(applicationContext).appDao()
            val personalActivityId = dao.insertPersonalActivity(personalActivity)
            withContext(Dispatchers.Main) {
                scheduleNotification(personalActivityId.toInt(), activityName, "Personal", selectedDate.time, true)
            }
            Toast.makeText(this@AddPersonalActivityActivity, "Personal Activity added.", Toast.LENGTH_SHORT).show()
            finish() // Return to the previous activity
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification(activityId: Int, activityName: String, activityType: String, dueDate: Date, isPersonal: Boolean) {
        val notificationIntent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("activityId", activityId)
            putExtra("activityName", activityName)
            putExtra("activityType", activityType)
            putExtra("isPersonal", isPersonal)
            putExtra("reminderId", -1)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            activityId,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            dueDate.time,
            pendingIntent
        )
    }
}
