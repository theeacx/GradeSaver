package com.example.gradesaver

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.dataClasses.EnrollmentCountByCourse
import com.example.gradesaver.dataClasses.MonthlyActivityCount
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.User
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashbord)

        // Retrieve the user ID passed from the previous activity
        val userId = (intent.getSerializableExtra("USER_DETAILS") as? User)?.userId
        // Initialize the BarChart
        val barChart: BarChart = findViewById(R.id.barChart)
        val lineChart: LineChart = findViewById(R.id.lineChart)

        // Load enrollment data if userId is valid
        if (userId != -1) {
            if (userId != null) {
                loadEnrollmentData(barChart, userId)
                loadMonthlyActivityData(lineChart, userId)
            }
        } else {
            // Handle error case where userId isn't passed correctly
            // You might want to close the activity or show an error message
            finish()
        }
    }

    private fun loadEnrollmentData(barChart: BarChart, professorId: Int) {
        // Use a coroutine to fetch data asynchronously
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(applicationContext)
            val enrollmentCounts = database.appDao().getEnrollmentCountsByCourse(professorId)
            updateBarChart(barChart, enrollmentCounts)
        }
    }

    private fun updateBarChart(barChart: BarChart, data: List<EnrollmentCountByCourse>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        // Create entries and label array
        data.forEachIndexed { index, enrollmentCount ->
            entries.add(BarEntry(index.toFloat(), enrollmentCount.enrolledCount.toFloat()))
            labels.add(enrollmentCount.courseName)  // Ensure course names are added correctly
        }

        // Log labels for debugging
        labels.forEach { label ->
            Log.d("ChartLabel", "Course Name: $label")
        }

        val dataSet = BarDataSet(entries, "No of enrollments")
        dataSet.color = resources.getColor(R.color.purple, null)

        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.setExtraOffsets(0f, 0f, 0f, 100f)
        barChart.setScaleEnabled(true)
        // Set the formatter with the labels after setting the data
        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            granularity = 1f
            isGranularityEnabled = true
            setLabelCount(minOf(labels.size, 6))  // Show up to 6 labels at a time
//            setLabelCount(labels.size)
            setCenterAxisLabels(true)
            labelRotationAngle = 45f
            textSize = 10f
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(true)
            isGranularityEnabled = true
//            axisMinimum = 0.5f
//            axisMaximum = data.size.toFloat()
            spaceMin = 0.5f
            spaceMax = 0.5f
            setAvoidFirstLastClipping(true)
            textColor = Color.BLACK  // Ensure text color is visible
        }

//        barChart.description.text = "Enrollment Counts by Course"
        barChart.animateY(1000)
        barChart.extraBottomOffset = 20f  // Ensure there is space for rotated labels
        barChart.setVisibleXRangeMaximum(5f)  // You can limit visible count to enhance readability
        barChart.invalidate()  // Refresh the chart
    }

    private fun loadMonthlyActivityData(lineChart: LineChart, professorId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(applicationContext)
            val monthlyActivityCounts = database.appDao().getActivityCountsByMonth(professorId)

            // Log each monthly activity count to debug the data
            monthlyActivityCounts.forEach { activityCount ->
                Log.d("MonthlyActivityData", "Month: ${activityCount.month}, Count: ${activityCount.count}")
            }

            if (monthlyActivityCounts.isEmpty()) {
                Log.d("MonthlyActivityData", "No data available for this month.")
            } else {
                updateLineChartWithMonthlyData(lineChart, monthlyActivityCounts)
            }
        }
    }


    private fun updateLineChartWithMonthlyData(lineChart: LineChart, data: List<MonthlyActivityCount>) {
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        data.forEachIndexed { index, monthlyActivityCount ->
            entries.add(Entry(index.toFloat(), monthlyActivityCount.count.toFloat()))
            monthlyActivityCount.month?.let { labels.add(it) }  // Assuming 'month' is a string like "01", "02", etc.
        }

        val dataSet = LineDataSet(entries, "Activity Count per Month")
        dataSet.color = resources.getColor(R.color.purple, null)
        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(true)
            labelRotationAngle = 45f
            granularity = 1f
            isGranularityEnabled = true
            setLabelCount(labels.size)
        }

//        lineChart.description.text = "Monthly Activity Counts"
        lineChart.animateX(1000)
        lineChart.invalidate() // Refresh the chart
    }



}
