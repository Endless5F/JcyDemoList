package com.android.customwidget.kotlin.ext

import android.content.Context
import android.graphics.Paint
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.customwidget.R
import com.scwang.smart.refresh.layout.SmartRefreshLayout

fun addRefresh(view: View?): SmartRefreshLayout? {
    view?.apply {
        val parent = view.parent as ViewGroup
        parent.removeView(view)

        val createRefreshView = createRefreshView(view.context)
        createRefreshView.addView(view)
        parent.addView(createRefreshView)
        return createRefreshView
    }
    return null
}

private fun createRefreshView(context:Context): SmartRefreshLayout {
    val refreshView = SmartRefreshLayout(context)
    refreshView.id = R.id.refresh_view
    refreshView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    return refreshView
}

/**
 * 获取View的原始宽高
 */
fun getUnDisplayViewWidth(view: View): Int {
    val width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    val height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    view.measure(width, height)
    return view.measuredWidth
}

/**
 * 测量文本宽度
 */
fun TextView.measureTextWidth(data: String): Int {
    if (TextUtils.isEmpty(data)) return 0
    return (paint.measureText(data) + 0.5).toInt()
}

/**
 * 测量文本宽度
 */
fun String.measureTextWidth(textSize: Float = 10f): Int {
    if (TextUtils.isEmpty(this)) return 0
    val paint = Paint()
    paint.textSize = textSize
    return (paint.measureText(this) + 0.5).toInt()
}