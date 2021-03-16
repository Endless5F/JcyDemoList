package com.android.customwidget.kotlin.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import com.android.customwidget.R
import com.android.customwidget.kotlin.ext.dp
import kotlin.math.max

/**
 * 双行排列的TextView容器
 * 外层嵌套 HorizontalScrollView，可成为可横滑的双行显示TextView
 */
class DoubleRowTextView : ViewGroup {
    companion object {
        const val DEFAULT_LINES = 2
    }

    var textSize = 13.dp

    private var lines = DEFAULT_LINES

    private var mData = mutableListOf<String>()

    constructor(context: Context) : super(context)
    constructor(context: Context, attributes: AttributeSet?) : super(context, attributes)
    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attributes,
            defStyleAttr
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (lines > childCount) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        var firstTempW = 0
        var firstTempH = 0
        var secondTempW = 0
        var secondTempH = 0
        for (i in 0 until childCount) {
            val children = getChildAt(i)
            measureChild(children, widthMeasureSpec, heightMeasureSpec)
            val cWidth = children.measuredWidth
            val cHeight = children.measuredHeight
            val cParams = getViewMarginParams(children.layoutParams)
            if (i % 2 == 0) {
                firstTempW += cWidth + cParams.leftMargin + cParams.rightMargin
                if (firstTempH < (cHeight + cParams.topMargin + cParams.bottomMargin)) {
                    firstTempH = cHeight + cParams.topMargin + cParams.bottomMargin
                }
            } else {
                secondTempW += cWidth + cParams.leftMargin + cParams.rightMargin
                if (secondTempH < (cHeight + cParams.topMargin + cParams.bottomMargin)) {
                    secondTempH = cHeight + cParams.topMargin + cParams.bottomMargin
                }
            }
        }
        setMeasuredDimension(
                max(firstTempW, secondTempW) + paddingLeft + paddingRight,
                firstTempH + secondTempH + paddingTop + paddingBottom
        )
    }

    private fun getViewMarginParams(layoutParams: LayoutParams?): MarginLayoutParams {

        return if (layoutParams is MarginLayoutParams) {
            layoutParams
        } else {
            MarginLayoutParams(layoutParams)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (lines > childCount) {
            super.layout(l, t, r, b)
            return
        }
        var cFirstLeft = paddingLeft
        var cSecondLeft = paddingLeft
        var cFirstRight = 0
        var cSecondRight = 0
        var cFirstTop = 0
        var cSecondTop = 0
        var cFirstBottom = 0
        var cSecondBottom = 0
        for (i in 0 until childCount) {
            val children = getChildAt(i)
            val cParams = getViewMarginParams(children.layoutParams)
            if (i % 2 == 0) {
                cFirstLeft += cParams.leftMargin
                cFirstRight = cFirstLeft + children.measuredWidth
                cFirstTop = paddingTop + cParams.topMargin
                cFirstBottom = children.measuredHeight + cFirstTop
                children.layout(cFirstLeft, cFirstTop, cFirstRight, cFirstBottom)
                cFirstLeft += children.measuredWidth + cParams.rightMargin
                cSecondTop = cFirstBottom + cParams.bottomMargin
            } else {
                cSecondLeft += cParams.leftMargin
                cSecondRight = cSecondLeft + children.measuredWidth
                cSecondTop += cParams.topMargin
                cSecondBottom = children.measuredHeight + cSecondTop
                children.layout(cSecondLeft, cSecondTop, cSecondRight, cSecondBottom)
                cSecondLeft += children.measuredWidth + cParams.rightMargin
            }
        }
    }

    fun setDataList(data: List<String>) {
        if (data.isNotEmpty()) {
            mData.clear()
            removeAllViews()
            mData.addAll(data)
            data.forEach {
                val view = TextView(context).apply {
                    layoutParams = MarginLayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 6.dp, 8.dp)
                    }
                    text = it
                    setPadding(11.dp, 7.dp, 11.dp, 7.dp)
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                    setTextColor(Color.parseColor("#222222"))
                    setBackgroundResource(R.drawable.common_bg_rect)
                }
                addView(view)
            }
        }
    }

    fun setListener(listener: OnClickListener) {
        for (i in 0 until childCount) {
            getChildAt(i).setOnClickListener {
                mData.elementAtOrNull(i)?.let {
                    listener.onChildClickListener(i, it)
                }
            }
        }
    }

    interface OnClickListener {
        fun onChildClickListener(position: Int, data: String?)
    }
}