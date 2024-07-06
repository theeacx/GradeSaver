package com.example.gradesaver.decorators

import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class CustomTextSizeSpan(private val size: Float, private val isBold: Boolean) : MetricAffectingSpan() {

    override fun updateMeasureState(paint: TextPaint) {
        apply(paint)
    }

    override fun updateDrawState(tp: TextPaint) {
        apply(tp)
    }

    private fun apply(paint: TextPaint) {
        paint.textSize = size
        paint.isFakeBoldText = isBold
    }
}
