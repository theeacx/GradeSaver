package com.example.gradesaver

import android.app.DatePickerDialog
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
    private lateinit var database: AppDatabase // Instance of the database
    private var selectedDeadline: Date? = null // Define the variable here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_activity)
        // Initialize the database instance
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
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            selectedDeadline = calendar.time  // Set the selectedDeadline variable
            val btnAddDeadline: Button = findViewById(R.id.btnAddDeadline)
            // Use Romanian Locale for formatting the date
            btnAddDeadline.text = SimpleDateFormat("MMM dd, yyyy", Locale("ro", "RO")).format(selectedDeadline)
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun validateAndAddActivity() {
        val etActivityName: EditText = findViewById(R.id.etActivityName)
        val activityName = etActivityName.text.toString().trim()

        val radioGroupActivityType: RadioGroup = findViewById(R.id.radioGroupActivityType)
        val selectedTypeId = radioGroupActivityType.checkedRadioButtonId
        val radioButton = findViewById<RadioButton>(selectedTypeId)
        val selectedActivityType = radioButton?.text.toString()

        if (activityName.isEmpty()) {
            Toast.makeText(this, "Please enter an activity name.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedTypeId == -1) {
            Toast.makeText(this, "Please select an activity type.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDeadline == null) {
            Toast.makeText(this, "Please add a deadline.", Toast.LENGTH_SHORT).show()
            return
        }

        val courseId = intent.getIntExtra("COURSE_ID", -1)
        if (courseId == -1) {
            Toast.makeText(this, "Invalid course ID.", Toast.LENGTH_SHORT).show()
            return
        }

        // If all validations are passed, add the activity to the database
        addActivityToDatabase(activityName, selectedActivityType, selectedDeadline!!, courseId)
        finish() // Closes this activity and returns to the previous one
    }
    private fun addActivityToDatabase(activityName: String, activityType: String, deadline: Date, courseId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val newActivity = Activity(
                courseId = courseId,
                activityName = activityName,
                activityType = activityType,
                dueDate = deadline
            )
            database.appDao().insertActivity(newActivity)
        }
    }

    private fun fetchCourseAndSetCourseName(courseId: Int) {
        // Using lifecycleScope to launch a coroutine which ensures the coroutine is canceled when the lifecycle is destroyed.
        lifecycleScope.launch {
            // Switching to the IO dispatcher for database operations
            val course = withContext(Dispatchers.IO) {
                // Fetch course from the database using the provided courseId
                database.appDao().getCourseById(courseId)
            }

            // Back on the main thread to update the UI
            if (course != null) {
                // Successfully retrieved the course, update the TextView
                val tvAddActivityForCourse: TextView = findViewById(R.id.tvAddActivityForCourse)
                tvAddActivityForCourse.text = getString(R.string.add_activity_for_course, course.courseName)
            } else {
                // Course not found, handle this case possibly by showing a Toast or finishing the activity
                Toast.makeText(this@AddActivityActivity, "Course not found", Toast.LENGTH_LONG).show()
                finish() // Optionally finish the activity if the course is critical to further operations
            }
        }
    }
}
