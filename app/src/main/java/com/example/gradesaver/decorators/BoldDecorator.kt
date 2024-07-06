package com.example.gradesaver.decorators

import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class BoldDecorator(private val textSize: Float, private val isBold: Boolean) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return true // Decorate all days
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(CustomTextSizeSpan(textSize, isBold))
    }
}
