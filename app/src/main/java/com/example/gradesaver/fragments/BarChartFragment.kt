package com.example.gradesaver.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.R
import com.example.gradesaver.dataClasses.EnrollmentCountByCourse
import com.example.gradesaver.database.AppDatabase
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.launch

class BarChartFragment : Fragment() {
    private var userId: Int? = null
    private lateinit var barChart: BarChart
    private lateinit var labels: List<String>

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
        labels = data.map { it.courseName }

        data.forEachIndexed { index, enrollmentCount ->
            entries.add(BarEntry(index.toFloat(), enrollmentCount.enrolledCount.toFloat()))
        }

        val dataSet = BarDataSet(entries, "No of enrollments")
        dataSet.color = resources.getColor(R.color.purple, null)
        val barData = BarData(dataSet)
        barData.barWidth = 0.9f  // set custom bar width
        barChart.data = barData

        // Setting up X-axis
        val xAxis = barChart.xAxis
        xAxis.setDrawLabels(false) // Disable drawing of X-axis labels
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true

        barChart.description.isEnabled = false
        barChart.setFitBars(true)  // make the x-axis fit exactly all bars
        barChart.invalidate()  // refresh

        // Set up value selected listener
        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                e?.let {
                    val index = it.x.toInt()
                    if (index >= 0 && index < labels.size) {
                        val label = labels[index]
                        Toast.makeText(context, "Course: $label\nEnrollments: ${it.y.toInt()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onNothingSelected() {}
        })
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
