package com.example.gradesaver

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.adapters.StudentCoursesExpandableListAdapter
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.Activity
import com.example.gradesaver.database.entities.Course
import com.example.gradesaver.database.entities.User
import kotlinx.coroutines.launch

class StudentsCourseActivity : AppCompatActivity() {
    private lateinit var expandableListView: ExpandableListView
    private lateinit var adapter: StudentCoursesExpandableListAdapter
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_students_course)

        user = intent.getSerializableExtra("USER_DETAILS") as? User ?: return
        expandableListView = findViewById(R.id.courseExpandableListView)

        // Initialize the adapter with a lambda function that handles deletion
        adapter = StudentCoursesExpandableListAdapter(this, mutableListOf(), mutableMapOf()) { course ->
            onDeleteCourse(course)
        }
        expandableListView.setAdapter(adapter)
        loadCourses()
        val addCourse: Button = findViewById(R.id.addCourseButton)
        addCourse.setOnClickListener {
            val intent = Intent(this, EnrollmentActivity::class.java).apply {
                putExtra("USER_DETAILS", user)
            }
            startActivity(intent)
        }

        // Set up the search functionality
        val searchEditText: EditText = findViewById(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {
                // Not used
            }
        })
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
