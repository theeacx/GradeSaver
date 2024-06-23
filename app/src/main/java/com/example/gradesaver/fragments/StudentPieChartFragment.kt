package com.example.gradesaver.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gradesaver.R
import com.example.gradesaver.dataClasses.CourseActivityCount
import com.example.gradesaver.database.AppDatabase
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch

class StudentPieChartFragment : Fragment() {
    private var userId: Int? = null
    private lateinit var pieChart: PieChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_pie_chart, container, false)
        pieChart = view.findViewById(R.id.pieChart)
        userId = arguments?.getInt(USER_ID_KEY)
        userId?.let { loadActivityCountByCourse(it) }
        return view
    }

    private fun loadActivityCountByCourse(studentId: Int) {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(requireContext().applicationContext)
            val courseActivityCounts = database.appDao().getActivityCountByCourse(studentId)
            updatePieChart(courseActivityCounts)
        }
    }

    private fun updatePieChart(data: List<CourseActivityCount>) {
        val entries = ArrayList<PieEntry>()

        data.forEach {
            entries.add(PieEntry(it.numberOfActivities.toFloat(), it.courseName))
        }

        val dataSet = PieDataSet(entries, "Activities by Course")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.description.text = "Distribution of Activities by Course"
        pieChart.isDrawHoleEnabled = false

        pieChart.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            yOffset = 20f
        }

        pieChart.animateY(1400)
        pieChart.invalidate()
    }

    companion object {
        private const val USER_ID_KEY = "userId"

        fun newInstance(userId: Int): StudentPieChartFragment {
            val fragment = StudentPieChartFragment()
            val args = Bundle()
            args.putInt(USER_ID_KEY, userId)
            fragment.arguments = args
            return fragment
        }
    }
}
