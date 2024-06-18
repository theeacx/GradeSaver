package com.example.gradesaver.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.R
import com.example.gradesaver.dataClasses.EnrollmentCountByCourse
import com.example.gradesaver.database.AppDatabase
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.launch

class BarChartFragment : Fragment() {
    private var userId: Int? = null
    private lateinit var barChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bar_chart, container, false)
        barChart = view.findViewById(R.id.barChart)
        userId = arguments?.getInt(USER_ID_KEY)
        userId?.let { loadEnrollmentData(it) }
        return view
    }

    private fun loadEnrollmentData(professorId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(requireContext().applicationContext)
            val enrollmentCounts = database.appDao().getEnrollmentCountsByCourse(professorId)
            updateBarChart(enrollmentCounts)
        }
    }

    private fun updateBarChart(data: List<EnrollmentCountByCourse>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        data.forEachIndexed { index, enrollmentCount ->
            entries.add(BarEntry(index.toFloat(), enrollmentCount.enrolledCount.toFloat()))
            labels.add(enrollmentCount.courseName)
        }

        val dataSet = BarDataSet(entries, "No of enrollments")
        dataSet.color = resources.getColor(R.color.purple, null)
        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.invalidate()
    }

    companion object {
        private const val USER_ID_KEY = "userId"

        fun newInstance(userId: Int): BarChartFragment {
            val fragment = BarChartFragment()
            val args = Bundle()
            args.putInt(USER_ID_KEY, userId)
            fragment.arguments = args
            return fragment
        }
    }
}
