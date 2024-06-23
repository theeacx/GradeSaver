package com.example.gradesaver

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.adapters.ActivityListAdapter
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.dao.AppDao
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
            val activities = dao.getUpcomingActivities(currentDate)
            val adapter = ActivityListAdapter(this@StudentActivityListActivity, activities)
            listView.adapter = adapter
        }
    }
}
