package com.example.gradesaver

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.adapters.CoursesExpandableListAdapter
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.Activity
import com.example.gradesaver.database.entities.Course
import com.example.gradesaver.database.entities.User
import kotlinx.coroutines.launch

class ProfessorCoursesActivity : AppCompatActivity() {
    private var adapter: CoursesExpandableListAdapter? = null
    private lateinit var expandableListView: ExpandableListView
    private var professorId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_professor_courses)

        // Initialize the ExpandableListView
        expandableListView = findViewById(R.id.courseExpandableListView)

        // Retrieve the professor's User object from the intent
        professorId = (intent.getSerializableExtra("USER_DETAILS") as? User)?.userId

        // Set up the adapter with initial data
        setupAdapter()

        // Listener for adding a new course
        findViewById<Button>(R.id.addCourseButton).setOnClickListener {
            showAddCourseDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when coming back to this Activity
        setupAdapter()
    }

    private fun setupAdapter() {
        professorId?.let { id ->
            lifecycleScope.launch {
                val courses = AppDatabase.getInstance(applicationContext).appDao().getCoursesByProfessor(id)
                val courseDetails = HashMap<String, List<Activity>>()

                // Fetching activities for each course
                courses.forEach { course ->
                    val activities = AppDatabase.getInstance(applicationContext).appDao().getActivitiesByCourse(course.courseId)
                    courseDetails[course.courseName] = activities
                }

                // Update the adapter and expandable list view
                adapter = CoursesExpandableListAdapter(this@ProfessorCoursesActivity, courses, courseDetails, lifecycleScope, id)
                expandableListView.setAdapter(adapter)
            }
        }
    }

    private fun showAddCourseDialog() {
        val courseNameEditText = EditText(this).apply { hint = "Course Name" }
        val enrollmentCodeEditText = EditText(this).apply { hint = "Enrollment Code" }

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Add New Course")
        dialogBuilder.setView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(courseNameEditText)
            addView(enrollmentCodeEditText)
            setPadding(32, 32, 32, 32)
        })

        dialogBuilder.setPositiveButton("OK") { dialog, which ->
            val courseName = courseNameEditText.text.toString().trim()
            val enrollmentCode = enrollmentCodeEditText.text.toString().trim()

            if (courseName.isNotEmpty() && enrollmentCode.isNotEmpty()) {
                val newCourse = Course(courseName = courseName, professorId = professorId ?: return@setPositiveButton, enrollmentCode = enrollmentCode)

                lifecycleScope.launch {
                    AppDatabase.getInstance(applicationContext).appDao().insertCourse(newCourse)
                    setupAdapter() // Refresh the adapter with the new course data
                }
            } else {
                Toast.makeText(this@ProfessorCoursesActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        dialogBuilder.setNegativeButton("Cancel", null)
        val dialog = dialogBuilder.create()
        dialog.show()
    }
}
