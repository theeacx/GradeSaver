package com.example.gradesaver.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.R
import com.example.gradesaver.dataClasses.ActivityReminders
import com.example.gradesaver.database.AppDatabase
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

class StudentBarChartFragment : Fragment() {
    private var userId: Int? = null
    private lateinit var barChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_bar_chart, container, false)
        barChart = view.findViewById(R.id.barChart)
        userId = arguments?.getInt(USER_ID_KEY)
        userId?.let { loadActivityRemindersByType(it) }
        return view
    }

    private fun loadActivityRemindersByType(studentId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(requireContext().applicationContext)
            val reminderCounts = database.appDao().getReminderCountByActivityType(studentId)
            updateBarChart(reminderCounts)
        }
    }

    private fun updateBarChart(data: List<ActivityReminders>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        data.forEachIndexed { index, activityReminder ->
            entries.add(BarEntry(index.toFloat(), activityReminder.numberOfReminders.toFloat()))
            labels.add(activityReminder.activityType)
        }

        val dataSet = BarDataSet(entries, "Number of Reminders by Activity Type")
        dataSet.color = Color.parseColor("#8692f7")
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 10f

        val barData = BarData(dataSet)
        barChart.data = barData

        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawAxisLine(true)
            granularity = 1f
            labelCount = labels.size
            labelRotationAngle = 15f
            textSize = 10f
        }

        barChart.axisLeft.apply {
            axisMinimum = 0f
            granularity = 1f
        }

        barChart.axisRight.isEnabled = false
        barChart.description.text = "Reminders by Activity Type"
        barChart.description.isEnabled = true

        barChart.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            yOffset = 12f
        }

        barChart.setExtraOffsets(5f, 10f, 5f, 30f)
        barChart.animateY(1000)
        barChart.invalidate()
    }

    companion object {
        private const val USER_ID_KEY = "userId"

        fun newInstance(userId: Int): StudentBarChartFragment {
            val fragment = StudentBarChartFragment()
            val args = Bundle()
            args.putInt(USER_ID_KEY, userId)
            fragment.arguments = args
            return fragment
        }
    }
}
