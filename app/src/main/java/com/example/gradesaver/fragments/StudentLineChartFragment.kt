package com.example.gradesaver.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.R
import com.example.gradesaver.dataClasses.MonthlyActivityDeadlines
import com.example.gradesaver.database.AppDatabase
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.launch

class StudentLineChartFragment : Fragment() {
    private var userId: Int? = null
    private lateinit var lineChart: LineChart
    private var labels = ArrayList<String>()
    private var monthlyActivityDeadlines: List<MonthlyActivityDeadlines> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_line_chart, container, false)
        lineChart = view.findViewById(R.id.lineChart)
        userId = arguments?.getInt(USER_ID_KEY)
        userId?.let { loadActivityDeadlinesByMonth(it) }
        return view
    }

    private fun loadActivityDeadlinesByMonth(studentId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(requireContext().applicationContext)
            monthlyActivityDeadlines = database.appDao().getActivityDeadlinesByMonth(studentId)
            updateLineChart(monthlyActivityDeadlines)
        }
    }

    private fun updateLineChart(data: List<MonthlyActivityDeadlines>) {
        val entries = ArrayList<Entry>()
        labels.clear()

        data.forEachIndexed { index, deadline ->
            entries.add(Entry(index.toFloat(), deadline.numberOfDeadlines.toFloat()))
            // Generate labels as "01", "02", "03", etc.
            labels.add(String.format("%02d", index + 1))
        }

        val dataSet = LineDataSet(entries, "Number of Deadlines by Month").apply {
            color = resources.getColor(R.color.purple, null)
            valueTextColor = resources.getColor(R.color.black, null) // Set the text color of the values
            valueTextSize = 12f // Increase the value text size
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(true)
            labelRotationAngle = 0f // Set rotation to 0 for better readability
            granularity = 1f
            isGranularityEnabled = true
            setLabelCount(labels.size)
            textSize = 14f // Increase the text size of x-axis labels
        }

        lineChart.axisLeft.apply {
            axisMinimum = 0f
            granularity = 1f
            textSize = 14f // Increase the text size of left axis labels
        }

        lineChart.axisRight.isEnabled = false

        lineChart.description.apply {
            text = "Monthly Deadlines"
            textSize = 16f // Increase description text size
            isEnabled = true
        }

        lineChart.legend.apply {
            textSize = 14f // Increase legend text size
        }

        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.x?.toInt()?.let {
                    val month = labels[it]
                    val count = monthlyActivityDeadlines[it].numberOfDeadlines
                    Toast.makeText(requireContext(), "$month: $count deadlines", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected() {}
        })

        lineChart.animateX(1000)
        lineChart.invalidate()
    }

    companion object {
        private const val USER_ID_KEY = "userId"

        fun newInstance(userId: Int): StudentLineChartFragment {
            val fragment = StudentLineChartFragment()
            val args = Bundle()
            args.putInt(USER_ID_KEY, userId)
            fragment.arguments = args
            return fragment
        }
    }
}
