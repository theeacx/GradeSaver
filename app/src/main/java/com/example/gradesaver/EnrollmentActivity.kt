package com.example.gradesaver

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.Course
import com.example.gradesaver.database.entities.Enrollment
import com.example.gradesaver.database.entities.User
import kotlinx.coroutines.launch

class EnrollmentActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var professorEmailAutocomplete: AutoCompleteTextView
    private lateinit var courseNameAutocomplete: AutoCompleteTextView
    private lateinit var professorAdapter: ArrayAdapter<String>
    private lateinit var courseAdapter: ArrayAdapter<String>
    private var professors: List<User> = listOf()
    private var availableCourses: List<Course> = listOf()
    private var selectedProfessorId: Int? = null
    private var selectedCourseId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enrollment)

        val user = intent.getSerializableExtra("USER_DETAILS") as? User
        val studentId = user?.userId ?: throw IllegalStateException("Student ID must be provided")

        db = AppDatabase.getInstance(this)
        professorEmailAutocomplete = findViewById(R.id.professorEmailAutocomplete)
        courseNameAutocomplete = findViewById(R.id.courseNameAutocomplete)

        setupProfessorAutocomplete(studentId)
        setupListeners(studentId)
    }

    private fun setupProfessorAutocomplete(studentId: Int) {
        lifecycleScope.launch {
            professors = db.appDao().getAllUsers().filter { it.role == "Professor" }
            val emails = professors.map { it.email }
            professorAdapter = ArrayAdapter(this@EnrollmentActivity, android.R.layout.simple_dropdown_item_1line, emails)
            professorEmailAutocomplete.apply {
                setAdapter(professorAdapter)
                setOnClickListener { showDropDown() }
                addTextChangedListener { text ->
                    val matchingProf = professors.find { it.email == text.toString() }
                    selectedProfessorId = matchingProf?.userId
                    if (selectedProfessorId != null) {
                        setupCourseAutocomplete(selectedProfessorId!!, studentId)
                    }
                }
            }
        }
    }

    private fun setupCourseAutocomplete(professorId: Int, studentId: Int) {
        lifecycleScope.launch {
            val allCourses = db.appDao().getCoursesByProfessor(professorId)
            val enrolledCourses = db.appDao().getEnrollmentsByStudent(studentId).map { it.courseId }
            availableCourses = allCourses.filterNot { it.courseId in enrolledCourses }
            val courseNames = availableCourses.map { it.courseName }
            courseAdapter = ArrayAdapter(this@EnrollmentActivity, android.R.layout.simple_dropdown_item_1line, courseNames)
            courseNameAutocomplete.apply {
                setAdapter(courseAdapter)
                setOnClickListener { showDropDown() }
                addTextChangedListener { text ->
                    val matchingCourse = availableCourses.find { it.courseName == text.toString() }
                    selectedCourseId = matchingCourse?.courseId
                }
            }
        }
    }

    private fun setupListeners(studentId: Int) {
        findViewById<Button>(R.id.confirmEnrollmentButton).setOnClickListener {
            val enrollmentCode = findViewById<EditText>(R.id.enrollmentCodeEditText).text.toString()
            if (selectedCourseId != null) {
                attemptEnrollment(selectedCourseId!!, studentId, enrollmentCode)
            } else {
                Toast.makeText(this, "No valid course selected or input does not match", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun attemptEnrollment(courseId: Int, studentId: Int, enrollmentCode: String) {
        lifecycleScope.launch {
            val course = db.appDao().getCourseById(courseId)
            if (course != null && course.enrollmentCode == enrollmentCode) {
                val enrollment = Enrollment(courseId = courseId, studentId = studentId)
                db.appDao().insertEnrollment(enrollment)
                runOnUiThread {
                    Toast.makeText(this@EnrollmentActivity, "Enrollment successful", Toast.LENGTH_SHORT).show()
                    finish()  // Close activity and return
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this@EnrollmentActivity, "Invalid enrollment code", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
