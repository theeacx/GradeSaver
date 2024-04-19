package com.example.gradesaver

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.Course
import com.example.gradesaver.database.entities.Enrollment
import com.example.gradesaver.database.entities.User
import kotlinx.coroutines.launch

class EnrollmentActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var professorAdapter: ArrayAdapter<String>
    private lateinit var courseAdapter: ArrayAdapter<String>
    private var selectedCourseId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enrollment)

        val user = intent.getSerializableExtra("USER_DETAILS") as? User
        val studentId = user?.userId ?: throw IllegalStateException("Student ID must be provided")

        db = AppDatabase.getInstance(this)
        setupProfessorAutocomplete(studentId)
        setupListeners(studentId)
    }

    private fun setupProfessorAutocomplete(studentId: Int) {
        val professorEmailAutocomplete = findViewById<AutoCompleteTextView>(R.id.professorEmailAutocomplete)
        lifecycleScope.launch {
            val professors = db.appDao().getAllUsers().filter { it.role == "Professor" }
            val emails = professors.map { it.email }
            professorAdapter = ArrayAdapter(this@EnrollmentActivity, android.R.layout.simple_dropdown_item_1line, emails)
            professorEmailAutocomplete.setAdapter(professorAdapter)
            professorEmailAutocomplete.setOnClickListener {
                professorEmailAutocomplete.showDropDown()
            }
            professorEmailAutocomplete.setOnItemClickListener { _, _, position, _ ->
                val selectedProfessor = professors[position]
                setupCourseAutocomplete(selectedProfessor.userId, studentId)
            }
        }
    }

    private fun setupCourseAutocomplete(professorId: Int, studentId: Int) {
        val courseNameAutocomplete = findViewById<AutoCompleteTextView>(R.id.courseNameAutocomplete)
        lifecycleScope.launch {
            val allCourses = db.appDao().getCoursesByProfessor(professorId)
            val enrolledCourses = db.appDao().getEnrollmentsByStudent(studentId).map { it.courseId }
            val availableCourses = allCourses.filterNot { it.courseId in enrolledCourses }
            val courseNames = availableCourses.map(Course::courseName)
            courseAdapter = ArrayAdapter(this@EnrollmentActivity, android.R.layout.simple_dropdown_item_1line, courseNames)
            courseNameAutocomplete.setAdapter(courseAdapter)
            courseNameAutocomplete.setOnClickListener {
                courseNameAutocomplete.showDropDown()
            }
            courseNameAutocomplete.setOnItemClickListener { _, _, position, _ ->
                selectedCourseId = availableCourses[position].courseId
            }
        }
    }

    private fun setupListeners(studentId: Int) {
        findViewById<Button>(R.id.confirmEnrollmentButton).setOnClickListener {
            val enrollmentCode = findViewById<EditText>(R.id.enrollmentCodeEditText).text.toString()
            selectedCourseId?.let {
                attemptEnrollment(it, studentId, enrollmentCode)
            } ?: Toast.makeText(this, "Select a course first", Toast.LENGTH_SHORT).show()
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
