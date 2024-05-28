package com.example.gradesaver

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.dataClasses.ActivityCount
import com.example.gradesaver.dataClasses.EnrollmentCountByCourse
import com.example.gradesaver.dataClasses.MonthlyActivityCount
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.Course
import com.example.gradesaver.database.entities.User
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.RadarChart
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
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var radarChart: RadarChart
    private lateinit var horizontalBarChart: HorizontalBarChart
    private lateinit var courseSpinner: Spinner
    private lateinit var reminderSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashbord)

        // Retrieve the user ID passed from the previous activity
        val userId = (intent.getSerializableExtra("USER_DETAILS") as? User)?.userId
        // Initialize the BarChart
        val barChart: BarChart = findViewById(R.id.barChart)
        val lineChart: LineChart = findViewById(R.id.lineChart)
        val pieChart: PieChart = findViewById(R.id.pieChart)
        radarChart = findViewById(R.id.radarChart)
        horizontalBarChart = findViewById(R.id.horizontalBarChart)
        courseSpinner = findViewById(R.id.courseSpinner)
        reminderSpinner = findViewById(R.id.reminderSpinner)

        // Load enrollment data if userId is valid
        if (userId != -1) {
            if (userId != null) {
                loadEnrollmentData(barChart, userId)
                loadMonthlyActivityData(lineChart, userId)
                loadActivityTypeData(pieChart, userId)
                loadCoursesAndSetupSpinner(userId)
                loadReminderCoursesAndSetupSpinner(userId)
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

        val dataSet = BarDataSet(entries, "No of enrollments")
        dataSet.color = resources.getColor(R.color.purple, null)

        val barData = BarData(dataSet)
        barChart.data = barData
        // Adjust offsets to provide more space, particularly at the bottom
        barChart.setExtraOffsets(5f, 30f, 5f, 70f)  // Left, top, right, bottom increases

        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            granularity = 1f
            isGranularityEnabled = true
            setLabelCount(minOf(labels.size, 6))  // Show up to 6 labels at a time
            setCenterAxisLabels(true)
            labelRotationAngle = 45f
            textSize = 10f
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(true)
            spaceMin = 0.5f
            spaceMax = 0.5f
            setAvoidFirstLastClipping(true)
            textColor = Color.BLACK  // Ensure text color is visible
        }

        barChart.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            yOffset = 12f  // Adjust the Y offset to give more space
        }

        barChart.description.text = "Enrollment Counts by Course"
        barChart.animateY(1000)
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

    private fun loadActivityTypeData(pieChart: PieChart, professorId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(applicationContext)
            val activityCounts = database.appDao().getActivityCountsByType(professorId)
            updatePieChart(pieChart, activityCounts)
        }
    }

    private fun updatePieChart(pieChart: PieChart, data: List<ActivityCount>) {
        val entries = ArrayList<PieEntry>()

        // Create entries for the pie chart
        data.forEach {
            entries.add(PieEntry(it.activityCount.toFloat(), it.activityType))
        }

        val dataSet = PieDataSet(entries, "Activity Types")
        // Define a custom set of colors
        val colors = arrayListOf(
            Color.parseColor("#FF6347"), // Tomato for Test
            Color.parseColor("#FFD700"), // Gold for Exam
            Color.parseColor("#4682B4"), // Steel Blue for Midterm Exam
            Color.parseColor("#32CD32"), // Lime Green for Project
            Color.parseColor("#FFA500"), // Orange for Presentation
            Color.parseColor("#6A5ACD")  // Slate Blue for the additional type
        )
        dataSet.colors = colors

        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(pieChart)) // Format as percentage
        pieData.setValueTextSize(12f) // Set the text size for values
        pieData.setValueTextColor(Color.WHITE) // Set the text color for values

        pieChart.data = pieData
        pieChart.description.isEnabled = false  // Disable the description label
        pieChart.isDrawHoleEnabled = false  // Create a donut-style chart
        pieChart.setUsePercentValues(true) // Enable percentage display
        pieChart.setEntryLabelColor(Color.BLACK) // Set entry labels color
        pieChart.setEntryLabelTextSize(12f) // Entry label text size
        pieChart.animateY(1400)  // Animate the chart
        pieChart.invalidate()  // Refresh the chart
    }

    private fun loadCoursesAndSetupSpinner(professorId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(applicationContext)
            val courses = database.appDao().getCoursesByProfessor(professorId)
            setupCourseSpinner(courses)
        }
    }

    private fun setupCourseSpinner(courses: List<Course>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses.map { it.courseName })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        courseSpinner.adapter = adapter

        courseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedCourseId = courses[position].courseId
                loadRadarChartData(selectedCourseId)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: Handle the case where no item is selected if necessary
            }
        }
    }

    private fun loadRadarChartData(courseId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(applicationContext)
            val selectedCourseActivities = database.appDao().getActivityDeadlinesByDay(courseId)
            val otherActivities = database.appDao().getAllActivityDeadlinesByDayExceptCourse(courseId)

            // Ensure data for all days from 1 to 31 is present
            val allDays = (1..31).map { it.toString() }

            val selectedCourseMap = selectedCourseActivities.associateBy { it.day.toString() }
            val otherActivitiesMap = otherActivities.associateBy { it.day.toString() }

            val selectedCourseEntries = allDays.map { day ->
                RadarEntry(selectedCourseMap[day]?.count?.toFloat() ?: 0f)
            }

            val otherCourseEntries = allDays.map { day ->
                RadarEntry(otherActivitiesMap[day]?.count?.toFloat() ?: 0f)
            }

            updateRadarChart(selectedCourseEntries, otherCourseEntries, allDays)
        }
    }

    private fun updateRadarChart(selectedCourseEntries: List<RadarEntry>, otherCourseEntries: List<RadarEntry>, labels: List<String>) {
        val dataSet1 = RadarDataSet(selectedCourseEntries, "Selected Course Activities").apply {
            color = Color.BLUE
            fillColor = Color.BLUE
            setDrawFilled(true)
            fillAlpha = 180
        }
        val dataSet2 = RadarDataSet(otherCourseEntries, "Other Activities").apply {
            color = Color.RED
            fillColor = Color.RED
            setDrawFilled(true)
            fillAlpha = 180
        }

        val radarData = RadarData(dataSet1, dataSet2)
        radarChart.data = radarData
        radarChart.description.text = "Activity Deadlines by Day"

        val xAxis = radarChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.textSize = 9f

        radarChart.yAxis.axisMinimum = 0f
        radarChart.invalidate() // Refresh the chart
    }


    private fun loadReminderCoursesAndSetupSpinner(professorId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(applicationContext)
            val courses = database.appDao().getCoursesByProfessor(professorId)
            setupReminderCourseSpinner(courses)
        }
    }

    private fun setupReminderCourseSpinner(courses: List<Course>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses.map { it.courseName })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reminderSpinner.adapter = adapter

        reminderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedCourseId = courses[position].courseId
                loadHorizontalBarChartData(selectedCourseId)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: Handle the case where no item is selected if necessary
            }
        }
    }

    private fun loadHorizontalBarChartData(courseId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(applicationContext)
            val reminderCounts = database.appDao().getRemindersCountByActivity(courseId)

            val entries = ArrayList<BarEntry>()
            val labels = ArrayList<String>()
            reminderCounts.forEachIndexed { index, reminderCount ->
                entries.add(BarEntry(index.toFloat(), reminderCount.reminderCount.toFloat()))
                labels.add(reminderCount.activityName)
            }

            val dataSet = BarDataSet(entries, "Reminder Counts").apply {
                color = resources.getColor(R.color.purple, null) // Use the theme's purple color
                valueTextColor = Color.WHITE
                valueTextSize = 10f
            }

            val data = BarData(dataSet).apply {
                barWidth = 0.4f // Set bar width
            }

            horizontalBarChart.data = data
            horizontalBarChart.setFitBars(true) // Make the x-axis fit exactly all bars

            horizontalBarChart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM_INSIDE
                setDrawGridLines(false)
                setDrawAxisLine(false)
                granularity = 1f
                labelCount = labels.size
                textSize = 12f // Increased text size for better visibility
                labelRotationAngle = -45f // Rotate labels to avoid overlap
            }

            horizontalBarChart.axisLeft.apply {
                axisMinimum = 0f // Start at zero
                granularity = 1f // Interval of 1
            }

            horizontalBarChart.axisRight.isEnabled = true // Enable right axis to balance the view

            horizontalBarChart.description.isEnabled = false // Disable the description
            horizontalBarChart.legend.isEnabled = false // Disable the legend if not needed

            // Increase the bottom offset to give more space for the rotated labels
            horizontalBarChart.setExtraOffsets(80f, 10f, 5f, 30f) // Left, Top, Right, Bottom

            horizontalBarChart.animateY(1000)
            horizontalBarChart.invalidate() // Refresh the chart
        }
    }
}
