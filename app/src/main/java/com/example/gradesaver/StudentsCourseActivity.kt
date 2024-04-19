package com.example.gradesaver

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.gradesaver.database.entities.User

class StudentsCourseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_students_course)

        val user = intent.getSerializableExtra("USER_DETAILS") as? User

        val addCourse: Button = findViewById(R.id.addCourseButton)
        addCourse.setOnClickListener {
            val intent = Intent(this, EnrollmentActivity::class.java).apply {
                putExtra("USER_DETAILS", user)
            }
            startActivity(intent)
        }
    }
}