package com.example.gradesaver.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.R
import com.example.gradesaver.dataClasses.MonthlyActivityCount
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

class LineChartFragment : Fragment() {
    private var userId: Int? = null
    private lateinit var lineChart: LineChart
    private var labels = ArrayList<String>()
    private var monthlyActivityCounts: List<MonthlyActivityCount> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_line_chart, container, false)
        lineChart = view.findViewById(R.id.lineChart)
        userId = arguments?.getInt(USER_ID_KEY)
        userId?.let { loadMonthlyActivityData(it) }
        return view
    }

    private fun loadMonthlyActivityData(professorId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(requireContext().applicationContext)
            monthlyActivityCounts = database.appDao().getActivityCountsByMonth(professorId)
            updateLineChartWithMonthlyData(monthlyActivityCounts)
        }
    }

    private fun updateLineChartWithMonthlyData(data: List<MonthlyActivityCount>) {
        val entries = ArrayList<Entry>()
        labels.clear()

        data.forEachIndexed { index, monthlyActivityCount ->
            entries.add(Entry(index.toFloat(), monthlyActivityCount.count.toFloat()))
            monthlyActivityCount.month?.let { labels.add(it) }
        }

        val dataSet = LineDataSet(entries, "Activity Count per Month").apply {
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
            labelRotationAngle = 45f
            granularity = 1f
            isGranularityEnabled = true
            setLabelCount(labels.size)
        }

        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.x?.toInt()?.let {
                    val month = labels[it]
                    val count = monthlyActivityCounts[it].count
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

        fun newInstance(userId: Int): LineChartFragment {
            val fragment = LineChartFragment()
            val args = Bundle()
            args.putInt(USER_ID_KEY, userId)
            fragment.arguments = args
            return fragment
        }
    }
}
