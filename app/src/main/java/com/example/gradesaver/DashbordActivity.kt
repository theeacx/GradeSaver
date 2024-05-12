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
import com.github.mikephil.charting.charts.ScatterChart
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
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
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
        val pieChart: PieChart = findViewById(R.id.pieChart)

        // Load enrollment data if userId is valid
        if (userId != -1) {
            if (userId != null) {
                loadEnrollmentData(barChart, userId)
                loadMonthlyActivityData(lineChart, userId)
                loadActivityTypeData(pieChart, userId)
                loadCoursesAndSetupSpinner(userId)
                loadCoursesAndSetupSpinners2(userId)
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
            setupCourseSpinner(courses, professorId)
        }
    }

    private fun setupCourseSpinner(courses: List<Course>, professorId: Int) {
        val spinner: Spinner = findViewById(R.id.mySpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses.map { it.courseName })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedCourseId = courses[position].courseId
                loadScatterChartData(selectedCourseId, professorId)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: Handle the case where no item is selected if necessary
            }
        }
    }

    private fun loadScatterChartData(courseId: Int, professorId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(applicationContext)
            val selectedCourseActivities = database.appDao().getActivityDeadlinesByDayAndMonth(courseId, professorId)
            val otherActivities = database.appDao().getAllActivitiesExceptSelectedCourse(courseId)

            val selectedCourseEntries = selectedCourseActivities.map { Entry(it.month.toFloat(), it.day.toFloat()) }
            val otherCourseEntries = otherActivities.map { Entry(it.month.toFloat(), it.day.toFloat()) }

            updateScatterChart(selectedCourseEntries, otherCourseEntries)
        }
    }

    private fun updateScatterChart(selectedCourseEntries: List<Entry>, otherCourseEntries: List<Entry>) {
        val dataSet1 = ScatterDataSet(selectedCourseEntries, "Selected Course Activities").apply {
            color = Color.BLUE
            setScatterShape(ScatterChart.ScatterShape.CIRCLE)
            scatterShapeSize = 10f
        }
        val dataSet2 = ScatterDataSet(otherCourseEntries, "Other Activities").apply {
            color = Color.RED
            setScatterShape(ScatterChart.ScatterShape.CIRCLE)
            scatterShapeSize = 10f
        }

        val scatterChart: ScatterChart = findViewById(R.id.scatterChart)
        scatterChart.data = ScatterData(dataSet1, dataSet2)
        scatterChart.description.text = "Activity Deadlines by Month and Day"

        // Set properties for the X Axis
        scatterChart.xAxis.axisMinimum = 0f
        scatterChart.xAxis.axisMaximum = 12f
        scatterChart.xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"))

        // Set properties for the Left Y Axis
        scatterChart.axisLeft.axisMinimum = 1f
        scatterChart.axisLeft.axisMaximum = 31f

        // Generally, you might want to disable the right Y axis if it is not used
        scatterChart.axisRight.isEnabled = false

        scatterChart.invalidate() // Refresh the chart
    }

    private fun loadCoursesAndSetupSpinners2(professorId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(applicationContext)
            val courses = database.appDao().getCoursesByProfessor(professorId)
            setupSpinner2(findViewById(R.id.courseSpinner), courses)
        }
    }

    private fun setupSpinner2(spinner: Spinner, courses: List<Course>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses.map { it.courseName })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedCourseId = courses[position].courseId
                if (spinner.id == R.id.courseSpinner) {
                    loadReminderDistributionData(selectedCourseId)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional handling for no selection
            }
        }
    }

    private fun loadReminderDistributionData(courseId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(applicationContext)
            val reminderCounts = database.appDao().getRemindersCountByActivity(courseId);

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

            val barChart: HorizontalBarChart = findViewById(R.id.horizontalBarChart)
            barChart.data = data
            barChart.setFitBars(true) // Make the x-axis fit exactly all bars

            barChart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM_INSIDE
                setDrawGridLines(false)
                setDrawAxisLine(false)
                granularity = 1f
                labelCount = labels.size
                textSize = 10f // Adjust text size for better fit
                labelRotationAngle = -45f // Rotate labels to avoid overlap
            }

            barChart.axisLeft.apply {
                axisMinimum = 0f // Start at zero
                granularity = 1f // Interval of 1
            }

            barChart.axisRight.isEnabled = true // Enable right axis to balance the view

            barChart.description.isEnabled = false // Disable the description
            barChart.legend.isEnabled = false // Disable the legend if not needed

            barChart.animateY(1000)
            barChart.invalidate() // Refresh the chart
        }
    }


}
