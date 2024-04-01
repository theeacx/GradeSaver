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
import com.example.gradesaver.database.entities.Course
import com.example.gradesaver.database.entities.User
import kotlinx.coroutines.launch



class ProfessorCoursesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_professor_courses)

        // Retrieve the professor's User object from the intent
        val professor = intent.getSerializableExtra("USER_DETAILS") as? User

        // Now you have the professor's ID in professor?.userId
        val professorId = professor?.userId

        lifecycleScope.launch {
            professorId?.let {
                val courses = AppDatabase.getInstance(applicationContext).appDao().getCoursesByProfessor(professorId)
                val courseDetails = HashMap<String, List<String>>() // Populate this with details for each course

                // Now, set the adapter on the ExpandableListView
                val expandableListView = findViewById<ExpandableListView>(R.id.courseExpandableListView)
                val adapter = CoursesExpandableListAdapter(this@ProfessorCoursesActivity, courses, courseDetails,lifecycleScope, professorId)
                expandableListView.setAdapter(adapter)
            }
        }
        findViewById<Button>(R.id.addCourseButton).setOnClickListener {
            // Initialization of the dialog components.
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

            // Set OK button with a custom click listener
            dialogBuilder.setPositiveButton("OK", null)
            dialogBuilder.setNegativeButton("Cancel", null)

            val dialog = dialogBuilder.create()

            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val courseName = courseNameEditText.text.toString().trim()
                    val enrollmentCode = enrollmentCodeEditText.text.toString().trim()

                    if (courseName.isNotEmpty() && enrollmentCode.isNotEmpty()) {
                        val professorId = professor?.userId ?: return@setOnClickListener
                        val newCourse = Course(courseName = courseName, professorId = professorId, enrollmentCode = enrollmentCode)

                        lifecycleScope.launch {
                            AppDatabase.getInstance(applicationContext).appDao().insertCourse(newCourse)
                            val updatedCourses = AppDatabase.getInstance(applicationContext).appDao().getCoursesByProfessor(professorId)
                            val newAdapter = CoursesExpandableListAdapter(this@ProfessorCoursesActivity, updatedCourses, HashMap(), lifecycleScope, professorId)
                            findViewById<ExpandableListView>(R.id.courseExpandableListView).setAdapter(newAdapter)
                            dialog.dismiss()
                        }
                    } else {
                        Toast.makeText(this@ProfessorCoursesActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            dialog.show()
        }


    }
}