package com.android.customwidget.kotlin.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.LinearLayout
import android.view.ViewConfiguration
import kotlin.math.abs


class ThouchLinearLayout @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null
            , defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    var touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private var initX = 0f
    private var initY = 0f
    private var isIntercept = false
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        Log.d("MotionEvent", "MotionEvent ${event.action}")
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initX = event.x
                initY = event.y
                isIntercept = false
            }

            MotionEvent.ACTION_MOVE -> {
                val currX = event.x
                val currY = event.y
                val dx = currX - initX
                val dy = currY - initY
                isIntercept = abs(dx) > touchSlop && abs(dx) > abs(dy)
                Log.d("ACTION_MOVE", "ACTION_MOVE $isIntercept")
            }

            MotionEvent.ACTION_UP -> {
            }
        }
        return isIntercept || super.onInterceptTouchEvent(event)
    }
}