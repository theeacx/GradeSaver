package com.example.gradesaver

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.gradesaver.adapters.ChartPagerAdapter
import com.example.gradesaver.database.entities.User
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DashboardActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var fabLeft: FloatingActionButton
    private lateinit var fabRight: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashbord)

        viewPager = findViewById(R.id.viewPager)
        fabLeft = findViewById(R.id.fab_left)
        fabRight = findViewById(R.id.fab_right)

        // Retrieve the user ID passed from the previous activity
        val userId = (intent.getSerializableExtra("USER_DETAILS") as? User)?.userId

        if (userId != null && userId != -1) {
            setupViewPager(userId)
        } else {
            // Handle error case where userId isn't passed correctly
            finish()
        }

        fabLeft.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem > 0) {
                viewPager.setCurrentItem(currentItem - 1, true)
            } else {
                viewPager.setCurrentItem(viewPager.adapter?.itemCount ?: 0 - 1, false)
            }
        }

        fabRight.setOnClickListener {
            val currentItem = viewPager.currentItem
            val itemCount = viewPager.adapter?.itemCount ?: 0
            if (currentItem < itemCount - 1) {
                viewPager.setCurrentItem(currentItem + 1, true)
            } else {
                viewPager.setCurrentItem(0, false)
            }
        }

    }

    private fun setupViewPager(userId: Int) {
        val adapter = ChartPagerAdapter(this, userId)
        viewPager.adapter = adapter
    }
}
