package com.example.gradesaver.decorators

import android.text.style.ForegroundColorSpan
import android.util.Log
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class ActivityDecorator(private val color: Int, private val dates: Collection<CalendarDay>) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        val shouldDecorate = dates.contains(day)
        Log.d("ActivityDecorator", "Should decorate $day: $shouldDecorate")
        return shouldDecorate
    }

    override fun decorate(view: DayViewFacade) {
        Log.d("ActivityDecorator", "Decorating with color: $color")
        view.addSpan(ForegroundColorSpan(color))
    }
}
