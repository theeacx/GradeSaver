package com.example.gradesaver

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.adapters.ActivityListAdapter
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.dao.AppDao
import com.example.gradesaver.database.entities.Activity
import com.example.gradesaver.database.entities.Course
import kotlinx.coroutines.launch
import java.util.Date

class StudentActivityListActivity : AppCompatActivity() {
    private lateinit var dao: AppDao
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_list)

        dao = AppDatabase.getInstance(this).appDao()
        listView = findViewById(R.id.activityListView)

        loadActivities()
    }

    private fun loadActivities() {
        lifecycleScope.launch {
            val currentDate = Date()
            val activitiesWithCourses = dao.getUpcomingActivities(currentDate)
            val activitiesWithProfessorEmail = activitiesWithCourses.map { activityWithCourse ->
                val professor = dao.getUserById(activityWithCourse.course.professorId)
                ActivityWithProfessorEmail(activityWithCourse.activity, activityWithCourse.course, professor?.email ?: "No email")
            }
            val adapter = ActivityListAdapter(this@StudentActivityListActivity, activitiesWithProfessorEmail)
            listView.adapter = adapter
        }
    }
}

data class ActivityWithProfessorEmail(
    val activity: Activity,
    val course: Course,
    val professorEmail: String
)
