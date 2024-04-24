package com.example.gradesaver

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.Activity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddActivityActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private var selectedDeadline: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_activity)
        database = AppDatabase.getInstance(this)

        val courseId = intent.getIntExtra("COURSE_ID", -1)
        if (courseId != -1) {
            fetchCourseAndSetCourseName(courseId)
        }

        val btnAddDeadline: Button = findViewById(R.id.btnAddDeadline)
        btnAddDeadline.setOnClickListener {
            showDatePicker()
        }

        val btnAddActivity: Button = findViewById(R.id.btnAddActivity)
        btnAddActivity.setOnClickListener {
            validateAndAddActivity()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, R.style.PurpleDatePickerDialog, { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            showTimePicker(calendar)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker(calendar: Calendar) {
        TimePickerDialog(this, R.style.PurpleTimePickerDialog, { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            selectedDeadline = calendar.time
            val btnAddDeadline: Button = findViewById(R.id.btnAddDeadline)
            btnAddDeadline.text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale("ro", "RO")).format(selectedDeadline)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun validateAndAddActivity() {
        val etActivityName: EditText = findViewById(R.id.etActivityName)
        val activityName = etActivityName.text.toString().trim()
        val radioGroupActivityType: RadioGroup = findViewById(R.id.radioGroupActivityType)
        val selectedTypeId = radioGroupActivityType.checkedRadioButtonId
        val radioButton = findViewById<RadioButton>(selectedTypeId)
        val selectedActivityType = radioButton?.text.toString()

        if (activityName.isEmpty() || selectedTypeId == -1 || selectedDeadline == null) {
            Toast.makeText(this, "Please complete all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val courseId = intent.getIntExtra("COURSE_ID", -1)
        if (courseId != -1) {
            addActivityToDatabase(activityName, selectedActivityType, selectedDeadline!!, courseId)
            finish()
        } else {
            Toast.makeText(this, "Invalid course ID.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addActivityToDatabase(activityName: String, activityType: String, deadline: Date, courseId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            // You must ensure that the Activity class has a constructor that accepts these parameters.
            val newActivity = Activity(
                activityId = 0, // Assuming autoGenerate is true, you can pass 0 for a new entry
                courseId = courseId,
                activityName = activityName,
                activityType = activityType,
                dueDate = deadline
            )
            database.appDao().insertActivity(newActivity)
        }
    }


    private fun fetchCourseAndSetCourseName(courseId: Int) {
        lifecycleScope.launch {
            val course = withContext(Dispatchers.IO) {
                database.appDao().getCourseById(courseId)
            }
            course?.let {
                val tvAddActivityForCourse: TextView = findViewById(R.id.tvAddActivityForCourse)
                tvAddActivityForCourse.text = getString(R.string.add_activity_for_course, it.courseName)
            } ?: run {
                Toast.makeText(this@AddActivityActivity, "Course not found", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
