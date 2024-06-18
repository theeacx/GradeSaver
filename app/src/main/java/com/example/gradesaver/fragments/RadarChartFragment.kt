package com.example.gradesaver.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.R
import com.example.gradesaver.database.AppDatabase
import com.example.gradesaver.database.entities.Course
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

class RadarChartFragment : Fragment() {
    private var userId: Int? = null
    private lateinit var radarChart: RadarChart
    private lateinit var courseSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_radar_chart, container, false)
        radarChart = view.findViewById(R.id.radarChart)
        courseSpinner = view.findViewById(R.id.courseSpinner)
        userId = arguments?.getInt(USER_ID_KEY)
        userId?.let { loadCoursesAndSetupSpinner(it) }
        return view
    }

    private fun loadCoursesAndSetupSpinner(professorId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(requireContext().applicationContext)
            val courses = database.appDao().getCoursesByProfessor(professorId)
            setupCourseSpinner(courses)
        }
    }

    private fun setupCourseSpinner(courses: List<Course>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courses.map { it.courseName })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        courseSpinner.adapter = adapter

        courseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedCourseId = courses[position].courseId
                loadRadarChartData(selectedCourseId)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadRadarChartData(courseId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(requireContext().applicationContext)
            val selectedCourseActivities = database.appDao().getActivityDeadlinesByDay(courseId)
            val otherActivities = database.appDao().getAllActivityDeadlinesByDayExceptCourse(courseId)

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
            color = resources.getColor(R.color.purple, null)
            fillColor = resources.getColor(R.color.purple, null)
            setDrawFilled(true)
            fillAlpha = 180
        }
        val dataSet2 = RadarDataSet(otherCourseEntries, "Other Activities").apply {
            color = resources.getColor(R.color.teal, null)
            fillColor = resources.getColor(R.color.teal, null)
            setDrawFilled(true)
            fillAlpha = 180
        }

        val radarData = RadarData(dataSet1, dataSet2)
        radarChart.data = radarData
        radarChart.description.isEnabled = false

        radarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        radarChart.yAxis.axisMinimum = 0f
        radarChart.invalidate()
    }

    companion object {
        private const val USER_ID_KEY = "userId"

        fun newInstance(userId: Int): RadarChartFragment {
            val fragment = RadarChartFragment()
            val args = Bundle()
            args.putInt(USER_ID_KEY, userId)
            fragment.arguments = args
            return fragment
        }
    }
}
