package com.example.gradesaver.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.R
import com.example.gradesaver.dataClasses.MonthlyActivityDeadlines
import com.example.gradesaver.database.AppDatabase
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

class StudentLineChartFragment : Fragment() {
    private var userId: Int? = null
    private lateinit var lineChart: LineChart

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
            val monthlyDeadlines = database.appDao().getActivityDeadlinesByMonth(studentId)
            updateLineChart(monthlyDeadlines)
        }
    }

    private fun updateLineChart(data: List<MonthlyActivityDeadlines>) {
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        data.forEachIndexed { index, deadline ->
            entries.add(Entry(index.toFloat(), deadline.numberOfDeadlines.toFloat()))
            labels.add(deadline.month)
        }

        val dataSet = LineDataSet(entries, "Number of Deadlines by Month")
        dataSet.color = Color.parseColor("#8692f7")
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 10f

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(true)
            granularity = 1f
            labelRotationAngle = -45f
            textSize = 10f
        }

        lineChart.axisLeft.apply {
            axisMinimum = 0f
            granularity = 1f
        }

        lineChart.axisRight.isEnabled = false
        lineChart.description.text = "Monthly Deadlines"
        lineChart.description.isEnabled = true

        lineChart.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            yOffset = 10f
        }

        lineChart.setExtraOffsets(5f, 10f, 5f, 30f)
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