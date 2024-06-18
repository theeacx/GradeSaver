package com.example.gradesaver.adapters

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.gradesaver.fragments.BarChartFragment
import com.example.gradesaver.fragments.HorizontalBarChartFragment
import com.example.gradesaver.fragments.LineChartFragment
import com.example.gradesaver.fragments.PieChartFragment
import com.example.gradesaver.fragments.RadarChartFragment

class ChartPagerAdapter(activity: FragmentActivity, private val userId: Int) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return 5 // Number of chart fragments
    }

    override fun createFragment(position: Int): androidx.fragment.app.Fragment {
        return when (position) {
            0 -> BarChartFragment.newInstance(userId)
            1 -> LineChartFragment.newInstance(userId)
            2 -> PieChartFragment.newInstance(userId)
            3 -> RadarChartFragment.newInstance(userId)
            4 -> HorizontalBarChartFragment.newInstance(userId)
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}
