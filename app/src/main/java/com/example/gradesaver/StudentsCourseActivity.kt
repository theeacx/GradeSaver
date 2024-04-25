package com.example.gradesaver

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.adapters.StudentCoursesExpandableListAdapter
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.Activity
import com.example.gradesaver.database.entities.Course
import com.example.gradesaver.database.entities.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class StudentsCourseActivity : AppCompatActivity() {
    private lateinit var expandableListView: ExpandableListView
    private lateinit var adapter: StudentCoursesExpandableListAdapter
    private lateinit var user: User
    private var currentFilter: String = "All"
    private var searchText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_students_course)
        user = intent.getSerializableExtra("USER_DETAILS") as User
        expandableListView = findViewById(R.id.courseExpandableListView)
        setupAdapter()
        setupSpinner()
        setupSearch()
        loadCourses()
        val addCourse: Button = findViewById(R.id.addCourseButton)
        addCourse.setOnClickListener {
            val intent = Intent(this, EnrollmentActivity::class.java).apply {
                putExtra("USER_DETAILS", user)
            }
            startActivity(intent)
        }
    }

    private fun setupAdapter() {
        adapter = StudentCoursesExpandableListAdapter(this,user, mutableListOf(), mutableMapOf()) { course ->
            onDeleteCourse(course)
        }
        expandableListView.setAdapter(adapter)
    }

    private fun setupSpinner() {
        val spinner: Spinner = findViewById(R.id.courseFilterSpinner)
        ArrayAdapter.createFromResource(this, R.array.course_filters, android.R.layout.simple_spinner_item).also { arrayAdapter ->
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = arrayAdapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                currentFilter = parent.getItemAtPosition(position).toString()
                applyFilterAndSearch()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupSearch() {
        val searchEditText: EditText = findViewById(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s.toString()
                applyFilterAndSearch()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun applyFilterAndSearch() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext).appDao()
            val userEnrolledCourses = db.getEnrollmentsByStudent(user.userId).mapNotNull { enrollment ->
                db.getCourseById(enrollment.courseId)
            }

            val coursesToShow = if (searchText.isNotBlank()) {
                userEnrolledCourses.filter { it.courseName.contains(searchText, ignoreCase = true) }
            } else {
                userEnrolledCourses
            }

            val filteredCourseDetails = mutableMapOf<Course, List<Activity>>()
            for (course in coursesToShow) {
                val activities = db.getActivitiesByCourse(course.courseId)
                val filteredActivities = when (currentFilter) {
                    "With Reminders" -> activities.filter { activity -> db.getRemindersByActivity(activity.activityId).isNotEmpty() }
                    "Without Reminders" -> activities.filter { activity -> db.getRemindersByActivity(activity.activityId).isEmpty() }
                    "Soon" -> activities.filter { activity -> activity.dueDate.after(Date()) && activity.dueDate.before(getOneWeekFromNow()) }
                    "Past Deadline" -> activities.filter { activity -> activity.dueDate.before(Date()) }
                    else -> activities
                }

                if (currentFilter == "All" || filteredActivities.isNotEmpty()) {
                    filteredCourseDetails[course] = filteredActivities
                }
            }

            withContext(Dispatchers.Main) {
                adapter.updateData(filteredCourseDetails.keys.toMutableList(), filteredCourseDetails)
            }
        }
    }

    private fun getOneWeekFromNow(): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        return calendar.time
    }

    private fun onDeleteCourse(course: Course) {
        AlertDialog.Builder(this)
            .setTitle("Unenroll from Course")
            .setMessage("Are you sure you want to unenroll from ${course.courseName}?")
            .setPositiveButton("Yes") { dialog, which ->
                deEnrollFromCourse(course)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deEnrollFromCourse(course: Course) {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val enrollment = db.appDao().getEnrollmentsByStudent(user.userId).find { it.courseId == course.courseId }
            if (enrollment != null) {
                db.appDao().deleteEnrollment(enrollment)
                runOnUiThread {
                    Toast.makeText(this@StudentsCourseActivity, "You have been unenrolled from ${course.courseName}.", Toast.LENGTH_SHORT).show()
                    loadCourses() // Reload the courses after de-enrollment
                }
            }
        }
    }
    private fun loadCourses() {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val enrollments = db.appDao().getEnrollmentsByStudent(user.userId)
            val courses = mutableListOf<Course>()
            val details = mutableMapOf<Course, List<Activity>>()
            enrollments.forEach { enrollment ->
                db.appDao().getCourseById(enrollment.courseId)?.let { course ->
                    courses.add(course)
                    details[course] = db.appDao().getActivitiesByCourse(course.courseId)
                }
            }
            runOnUiThread {
                adapter.updateData(courses, details)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadCourses()
    }
}

