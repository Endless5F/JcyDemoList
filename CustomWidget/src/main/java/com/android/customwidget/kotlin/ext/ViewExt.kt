package com.android.customwidget.kotlin.ext

import android.graphics.Paint
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.customwidget.R
import com.android.customwidget.util.ScreenUtils
import com.scwang.smart.refresh.layout.SmartRefreshLayout

/**
 * 设置状态栏间距
 */
fun setPaddingStatusBarHeight(view: View?) {
    if (view != null) {
        val statusBarHeight = ScreenUtils.getStatusBarHeight()
        val paddingLeft = view.paddingLeft
        val paddingTop = view.paddingTop + statusBarHeight
        val paddingRight = view.paddingRight
        val paddingBottom = view.paddingBottom
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
    }
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

/**
 * 添加下拉刷新View-SmartRefreshLayout
 */
fun addRefresh(view: View?): SmartRefreshLayout? {
    view?.apply {
        val parent = view.parent as ViewGroup
        parent.removeView(view)

        val createRefreshView = SmartRefreshLayout(context)
        createRefreshView.id = R.id.refresh_view
        createRefreshView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        createRefreshView.addView(view)
        parent.addView(createRefreshView)
        return createRefreshView
    }
    return null
}