package com.android.customwidget.kotlin.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.android.customwidget.kotlin.ext.dp

class PointView(context: Context?, attrs: AttributeSet? = null) : View(context, attrs) {

    private var paint: Paint = Paint()

    private var strokeWidth = 2.dp.toFloat()

    init {
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        paint.strokeWidth = strokeWidth
        paint.strokeCap = Paint.Cap.ROUND
    }

    fun setSize(size: Float) {
        strokeWidth = size
        paint.strokeWidth = size
    }

    fun setColor(color: String) {
        paint.color = Color.parseColor(color)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(strokeWidth.toInt(), strokeWidth.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPoint(strokeWidth / 2, strokeWidth / 2, paint)
    }
}