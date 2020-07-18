package com.android.customwidget.kotlin.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout
import com.android.customwidget.R
import com.android.customwidget.kotlin.ext.dp

/**
 * 圆角FrameLayout,可以设置外圆圈
 */
open class RoundFrameLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private val roundPaint: Paint
    private val imagePaint: Paint
    private var mBorderWidth = 0f
    private var mBorderColor = 0
    private var mRadius = 10.dp.toFloat()
    override fun dispatchDraw(canvas: Canvas) {
        canvas.saveLayer(RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat()), imagePaint, Canvas.ALL_SAVE_FLAG)
        super.dispatchDraw(canvas)
        //绘制外圆环边框圆环
        drawBorder(canvas)
        drawTopLeft(canvas)
        drawTopRight(canvas)
        drawBottomLeft(canvas)
        drawBottomRight(canvas)
        canvas.restore()
    }

    private fun drawBorder(canvas: Canvas) {
        if (mBorderWidth != 0f && width == height && mRadius == width / 2.toFloat()) {
            val paint = Paint()
            paint.isAntiAlias = true
            paint.strokeWidth = mBorderWidth
            paint.color = mBorderColor
            paint.style = Paint.Style.STROKE
            canvas.drawCircle(width / 2.toFloat(), height / 2.toFloat(), width / 2.toFloat(), paint)
        }
    }

    private fun drawTopLeft(canvas: Canvas) {
        if (mRadius > 0) {
            val path = Path()
            path.moveTo(0f, mRadius)
            path.lineTo(0f, 0f)
            path.lineTo(mRadius, 0f)
            path.arcTo(RectF(0f, 0f, mRadius * 2, mRadius * 2), -90f, -90f)
            path.close()
            canvas.drawPath(path, roundPaint)
        }
    }

    private fun drawTopRight(canvas: Canvas) {
        if (mRadius > 0) {
            val width = width
            val path = Path()
            path.moveTo(width - mRadius, 0f)
            path.lineTo(width.toFloat(), 0f)
            path.lineTo(width.toFloat(), mRadius)
            path.arcTo(RectF(width - 2 * mRadius, 0f, width.toFloat(),
                    mRadius * 2), 0f, -90f)
            path.close()
            canvas.drawPath(path, roundPaint)
        }
    }

    private fun drawBottomLeft(canvas: Canvas) {
        if (mRadius > 0) {
            val height = height
            val path = Path()
            path.moveTo(0f, height - mRadius)
            path.lineTo(0f, height.toFloat())
            path.lineTo(mRadius, height.toFloat())
            path.arcTo(RectF(0f, height - 2 * mRadius,
                    mRadius * 2, height.toFloat()), 90f, 90f)
            path.close()
            canvas.drawPath(path, roundPaint)
        }
    }

    private fun drawBottomRight(canvas: Canvas) {
        if (mRadius > 0) {
            val height = height
            val width = width
            val path = Path()
            path.moveTo(width - mRadius, height.toFloat())
            path.lineTo(width.toFloat(), height.toFloat())
            path.lineTo(width.toFloat(), height - mRadius)
            path.arcTo(RectF(width - 2 * mRadius, height - 2
                    * mRadius, width.toFloat(), height.toFloat()), 0f, 90f)
            path.close()
            canvas.drawPath(path, roundPaint)
        }
    }

    init {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.RoundFrameLayout)
            mRadius = ta.getDimension(R.styleable.RoundFrameLayout_radius, 0f)
            mBorderWidth = ta.getDimension(R.styleable.RoundFrameLayout_borderwidth, 0f)
            mBorderColor = ta.getColor(R.styleable.RoundFrameLayout_borderColor, Color.WHITE)
            ta.recycle()
        }
        roundPaint = Paint()
        roundPaint.color = Color.WHITE
        roundPaint.isAntiAlias = true
        roundPaint.style = Paint.Style.FILL
        roundPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        imagePaint = Paint()
        imagePaint.xfermode = null
    }
}