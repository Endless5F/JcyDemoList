package com.android.customwidget.kotlin.widget

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager

/**
 * 不可滑动
 */
class NoScrollGridLayoutManager(context: Context?, spanCount: Int) : GridLayoutManager(context, spanCount) {
    private var isScrollEnabled = false
    fun setScrollEnabled(flag: Boolean) {
        isScrollEnabled = flag
    }

    /**
     * 禁止滑动
     * canScrollHorizontally（禁止横向滑动）
     *
     * @return
     */
    override fun canScrollHorizontally(): Boolean {
        return isScrollEnabled && super.canScrollVertically()
    }

    /**
     * 禁止滑动
     * canScrollVertically（禁止竖向滑动）
     *
     * @return
     */
    override fun canScrollVertically(): Boolean {
        return isScrollEnabled && super.canScrollVertically()
    }
}