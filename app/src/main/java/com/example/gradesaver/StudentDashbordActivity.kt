package com.example.gradesaver

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.dataClasses.ActivityReminders
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.User
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

class StudentDashbordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_dashbord)

        val user = intent.getSerializableExtra("USER_DETAILS") as? User
        user?.let {
            loadActivityRemindersByType(it.userId)
        }
    }
    private fun loadActivityRemindersByType(studentId: Int) {
        val barChart: BarChart = findViewById(R.id.barChart)
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(applicationContext)
            val reminderCounts = database.appDao().getReminderCountByActivityType(studentId)

            updateBarChart(barChart, reminderCounts)
        }
    }

    private fun updateBarChart(barChart: BarChart, data: List<ActivityReminders>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        data.forEachIndexed { index, activityReminder ->
            entries.add(BarEntry(index.toFloat(), activityReminder.numberOfReminders.toFloat()))
            labels.add(activityReminder.activityType)
        }

        val dataSet = BarDataSet(entries, "Number of Reminders by Activity Type")
        dataSet.color = Color.parseColor("#8692f7") // Using your app's main color
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 10f

        val data = BarData(dataSet)
        barChart.data = data

        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawAxisLine(true)
            granularity = 1f
            labelCount = labels.size
            labelRotationAngle = 15f // Slight incline to avoid overlap
            textSize = 10f
        }

        barChart.axisLeft.apply {
            axisMinimum = 0f // Start Y-axis at 0
            granularity = 1f // Interval of 1
        }

        barChart.axisRight.isEnabled = false // Disable the right axis

        barChart.description.text = "Reminders by Activity Type"
        barChart.description.isEnabled = true

        // Adjusting the legend to appear above the chart
        barChart.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            yOffset = 12f // Adjust the gap
        }

        // Provide more space at the bottom to ensure labels are not cut off
        barChart.setExtraOffsets(5f, 10f, 5f, 30f) // Left, Top, Right, Bottom offsets

        barChart.animateY(1000)
        barChart.invalidate() // Refresh the chart
    }


}