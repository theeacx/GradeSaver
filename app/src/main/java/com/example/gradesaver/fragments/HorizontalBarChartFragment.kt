package com.example.gradesaver.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.R
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.Course
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.launch

class HorizontalBarChartFragment : Fragment() {
    private var userId: Int? = null
    private lateinit var horizontalBarChart: HorizontalBarChart
    private lateinit var reminderSpinner: Spinner
    private var labels = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_horizontal_bar_chart, container, false)
        horizontalBarChart = view.findViewById(R.id.horizontalBarChart)
        reminderSpinner = view.findViewById(R.id.reminderSpinner)
        userId = arguments?.getInt(USER_ID_KEY)
        userId?.let { loadCoursesAndSetupSpinner(it) }
        return view
    }

    private fun loadCoursesAndSetupSpinner(professorId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(requireContext().applicationContext)
            val courses = database.appDao().getCoursesByProfessor(professorId)
            setupReminderCourseSpinner(courses)
        }
    }

    private fun setupReminderCourseSpinner(courses: List<Course>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courses.map { it.courseName })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reminderSpinner.adapter = adapter

        reminderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedCourseId = courses[position].courseId
                loadHorizontalBarChartData(selectedCourseId)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadHorizontalBarChartData(courseId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(requireContext().applicationContext)
            val reminderCounts = database.appDao().getRemindersCountByActivity(courseId)

            val entries = ArrayList<BarEntry>()
            labels.clear()
            reminderCounts.forEachIndexed { index, reminderCount ->
                entries.add(BarEntry(index.toFloat(), reminderCount.reminderCount.toFloat()))
                labels.add(reminderCount.activityName)
            }

            val dataSet = BarDataSet(entries, "Reminder Counts").apply {
                color = resources.getColor(R.color.purple, null)
                valueTextColor = resources.getColor(R.color.white, null)
                valueTextSize = 10f
            }

            val data = BarData(dataSet).apply {
                barWidth = 0.4f
            }

            horizontalBarChart.data = data
            horizontalBarChart.setFitBars(true)
            horizontalBarChart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                granularity = 1f
                labelCount = labels.size
                textSize = 12f
                labelRotationAngle = -45f
                setDrawLabels(false) // Disable x-axis labels
            }
            horizontalBarChart.axisLeft.axisMinimum = 0f
            horizontalBarChart.axisRight.isEnabled = true
            horizontalBarChart.description.isEnabled = false

            horizontalBarChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                    e?.let {
                        val index = it.x.toInt()
                        if (index >= 0 && index < labels.size) {
                            val label = labels[index]
                            Toast.makeText(context, "Activity: $label\nReminders: ${it.y.toInt()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onNothingSelected() {}
            })

            horizontalBarChart.invalidate()
        }
    }

    companion object {
        private const val USER_ID_KEY = "userId"

        fun newInstance(userId: Int): HorizontalBarChartFragment {
            val fragment = HorizontalBarChartFragment()
            val args = Bundle()
            args.putInt(USER_ID_KEY, userId)
            fragment.arguments = args
            return fragment
        }
    }
}
