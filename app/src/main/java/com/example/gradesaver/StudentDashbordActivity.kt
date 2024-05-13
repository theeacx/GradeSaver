package com.example.gradesaver

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.dataClasses.ActivityReminders
import com.example.gradesaver.dataClasses.CourseActivityCount
import com.example.gradesaver.dataClasses.MonthlyActivityDeadlines
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.User
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch

class StudentDashbordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_dashbord)

        val user = intent.getSerializableExtra("USER_DETAILS") as? User
        user?.let {
            loadActivityRemindersByType(it.userId)
            loadActivityDeadlinesByMonth(it.userId)
            loadActivityCountByCourse(it.userId)
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

    private fun loadActivityDeadlinesByMonth(studentId: Int) {
        val lineChart: LineChart = findViewById(R.id.lineChart)
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(applicationContext)
            val monthlyDeadlines = database.appDao().getActivityDeadlinesByMonth(studentId)

            updateLineChart(lineChart, monthlyDeadlines)
        }
    }

    private fun updateLineChart(lineChart: LineChart, data: List<MonthlyActivityDeadlines>) {
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        data.forEachIndexed { index, deadline ->
            entries.add(Entry(index.toFloat(), deadline.numberOfDeadlines.toFloat()))
            labels.add(deadline.month)
        }

        val dataSet = LineDataSet(entries, "Number of Deadlines by Month")
        dataSet.color = Color.parseColor("#8692f7")  // Main color of your app
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 10f

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(true)
            granularity = 1f
            labelRotationAngle = -45f  // Rotate labels to make them more readable
            textSize = 10f
        }

        lineChart.axisLeft.apply {
            axisMinimum = 0f  // Start at zero
            granularity = 1f  // Interval of 1
        }

        lineChart.axisRight.isEnabled = false  // Disable right Y-axis

        lineChart.description.text = "Monthly Deadlines"
        lineChart.description.isEnabled = true

        // Move the legend above the chart
        lineChart.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            yOffset = 10f  // Adjust the Y offset to give more space
        }

        // Provide more space at the bottom to ensure labels are not cut off
        lineChart.setExtraOffsets(5f, 10f, 5f, 20f)  // Left, Top, Right, Bottom offsets

        lineChart.animateX(1000)
        lineChart.invalidate()  // Refresh the chart
    }

    private fun loadActivityCountByCourse(studentId: Int) {
        val pieChart: PieChart = findViewById(R.id.pieChart)
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(applicationContext)
            val courseActivityCounts = database.appDao().getActivityCountByCourse(studentId)

            updatePieChart(pieChart, courseActivityCounts)
        }
    }

    private fun updatePieChart(pieChart: PieChart, data: List<CourseActivityCount>) {
        val entries = ArrayList<PieEntry>()

        data.forEach {
            entries.add(PieEntry(it.numberOfActivities.toFloat(), it.courseName))
        }

        val dataSet = PieDataSet(entries, "Activities by Course")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList() // Using pre-defined colors
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.description.text = "Distribution of the no of activities by course"
        pieChart.isDrawHoleEnabled = false  // Set this to true for a donut-style chart

        // Adjust legend properties
        pieChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        pieChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        pieChart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
        pieChart.legend.setDrawInside(false)
        pieChart.legend.yOffset = 20f

        pieChart.animateY(1400, Easing.EaseInOutQuad) // Animate the drawing
        pieChart.invalidate() // Refresh the chart
    }


}