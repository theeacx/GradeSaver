package com.example.gradesaver.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.R
import com.example.gradesaver.dataClasses.ActivityCount
import com.example.gradesaver.database.AppDatabase
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.launch

class PieChartFragment : Fragment() {
    private var userId: Int? = null
    private lateinit var pieChart: PieChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pie_chart, container, false)
        pieChart = view.findViewById(R.id.pieChart)
        userId = arguments?.getInt(USER_ID_KEY)
        userId?.let { loadActivityTypeData(it) }
        return view
    }

    private fun loadActivityTypeData(professorId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(requireContext().applicationContext)
            val activityCounts = database.appDao().getActivityCountsByType(professorId)
            updatePieChart(activityCounts)
        }
    }

    private fun updatePieChart(data: List<ActivityCount>) {
        val entries = ArrayList<PieEntry>()

        data.forEach {
            entries.add(PieEntry(it.activityCount.toFloat(), it.activityType))
        }

        val dataSet = PieDataSet(entries, "Activity Types")
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
        pieData.setValueFormatter(PercentFormatter(pieChart))
        pieData.setValueTextSize(12f)
        pieData.setValueTextColor(Color.WHITE)

        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.invalidate()
    }

    companion object {
        private const val USER_ID_KEY = "userId"

        fun newInstance(userId: Int): PieChartFragment {
            val fragment = PieChartFragment()
            val args = Bundle()
            args.putInt(USER_ID_KEY, userId)
            fragment.arguments = args
            return fragment
        }
    }
}
