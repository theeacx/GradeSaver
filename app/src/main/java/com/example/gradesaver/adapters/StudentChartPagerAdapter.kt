package com.example.gradesaver.adapters

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.gradesaver.fragments.StudentBarChartFragment
import com.example.gradesaver.fragments.StudentLineChartFragment
import com.example.gradesaver.fragments.StudentPieChartFragment

class StudentChartPagerAdapter(activity: FragmentActivity, private val userId: Int) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return 3 // Number of chart fragments
    }

    override fun createFragment(position: Int): androidx.fragment.app.Fragment {
        return when (position) {
            0 -> StudentBarChartFragment.newInstance(userId)
            1 -> StudentLineChartFragment.newInstance(userId)
            2 -> StudentPieChartFragment.newInstance(userId)
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}
