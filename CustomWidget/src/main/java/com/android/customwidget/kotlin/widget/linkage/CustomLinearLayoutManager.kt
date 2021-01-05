package com.android.customwidget.kotlin.widget.linkage

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.AttributeSet


/**
 * 开启或关闭滑动
 */
class CustomLinearLayoutManager : LinearLayoutManager {
    private var isScrollEnabled = true

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, isScrollEnabled: Boolean) : super(context) {
        this.isScrollEnabled = isScrollEnabled
    }

    constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {}

    fun setScrollEnabled(flag: Boolean) {
        isScrollEnabled = flag
    }

    override fun canScrollVertically(): Boolean {
        val orientation = orientation
        return if (orientation == VERTICAL) {
            isScrollEnabled && super.canScrollVertically()
        } else super.canScrollVertically()
    }

    override fun canScrollHorizontally(): Boolean {
        val orientation = orientation
        return if (orientation == HORIZONTAL) {
            isScrollEnabled && super.canScrollHorizontally()
        } else super.canScrollHorizontally()
    }
}
