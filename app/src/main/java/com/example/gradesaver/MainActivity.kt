package com.example.gradesaver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.gradesaver.database.AppDatabase
import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.database.entities.*
import java.util.Date

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dao = AppDatabase.getInstance(this).appDao()

        lifecycleScope.launch {
            // Insert Users (assuming you have a way to hash or store passwords securely)
            val users = listOf(
                User(email = "professor@example.com", password = "securepassword", role = "Professor"),
                User(email = "student@example.com", password = "securepassword", role = "Student")
            )
            users.forEach { dao.insertUser(it) }

            // Insert Courses
            // Note: Adjust professorId according to the actual IDs from the Users table
            val courses = listOf(
                Course(courseName = "Introduction to Kotlin", professorId = 1, enrollmentCode = "KOTLIN101"),
                Course(courseName = "Advanced Android Development", professorId = 1, enrollmentCode = "ANDROID_ADV")
            )
            courses.forEach { dao.insertCourse(it) }

            // Insert Activities
            // Note: Adjust courseId according to the actual IDs from the Courses table
            val activities = listOf(
                Activity(courseId = 1, activityName = "Kotlin Quiz 1", activityType = "Test", dueDate = Date()),
                Activity(courseId = 2, activityName = "Android Project 1", activityType = "Project", dueDate = Date())
            )
            activities.forEach { dao.insertActivity(it) }

            // Log the inserted data for demonstration
            val allUsers = dao.getAllUsers()
            Log.d("MainActivity", "Users: $allUsers")

            val allCourses = dao.getAllCourses()
            Log.d("MainActivity", "Courses: $allCourses")

            val allActivities = dao.getAllActivities()
            Log.d("MainActivity", "Activities: $allActivities")


        }

    }
}