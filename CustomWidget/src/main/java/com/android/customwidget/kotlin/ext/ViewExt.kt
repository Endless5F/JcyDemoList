package com.android.customwidget.kotlin.ext

import android.content.Context
import android.view.View
import android.view.ViewGroup
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

fun getUnDisplayViewWidth(view: View): Int {
    val width = View.MeasureSpec.makeMeasureSpec(0,
            View.MeasureSpec.UNSPECIFIED)
    val height = View.MeasureSpec.makeMeasureSpec(0,
            View.MeasureSpec.UNSPECIFIED)
    view.measure(width, height)
    return view.measuredWidth
}