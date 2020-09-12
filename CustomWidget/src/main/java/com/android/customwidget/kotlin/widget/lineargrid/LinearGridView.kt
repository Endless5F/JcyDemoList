package com.android.customwidget.kotlin.widget.lineargrid

import android.content.Context
import android.graphics.Typeface
import androidx.recyclerview.widget.GridLayoutManager
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.android.customwidget.R
import com.android.customwidget.kotlin.ext.dp
import kotlinx.android.synthetic.main.linear_gird_item.view.*

/**
 * 使用方法示例：
 * LinearGridView(context).apply {
 *     val layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
 *     layoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
 *     this.layoutParams = layoutParams
 *
 *     spanCount = 4 // 一行显示4个
 *     itemWidth = 45.dp // 单个item的宽(高同宽)
 *     itemTopMargin = 15.dp // 每行之间的间距
 *     // HotPartView 整体最大宽度 = 屏幕宽 - 左外边距宽 - 右外边距宽 - 左侧menu列表布局宽
 *     viewMaxWidth = resources.displayMetrics.widthPixels - leftMargin - rightMargin - context.resources.getDimension(R.dimen.main_menu_width).toInt()
 * }
 *
 */
class LinearGridView(context: Context) : LinearLayout(context) {

    var spanCount = 4
    var itemWidth = 45.dp
    var itemTopMargin = 15.dp
    var viewMaxWidth = resources.displayMetrics.widthPixels

    private val titleText: TextView by lazy {
        createTitleView()
    }

    private fun createTitleView(): TextView {
        val textView = TextView(context)
        textView.text = "测试项目"
        textView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.dp_14))
        return textView
    }

    private val hotPartAdapter: GridItemAdapter by lazy {
        GridItemAdapter(context)
    }

    init {
        orientation = VERTICAL

//        LayoutInflater.from(context).inflate(R.layout.linear_grid_layout, this)
//        rv_hot_part.layoutManager = CustomGridLayoutManager(context, spanCount)
//        rv_hot_part.adapter = hotPartAdapter
//        rv_hot_part.addItemDecoration(GridDividerItemDecoration(context, dividerWidth, false))
    }

    fun setData(data: Any?) {
        when (data) {
            is Data -> {
//                hotPartAdapter.setData(data.hotParts)
                addItems(data.list)
            }
            else -> return
        }
    }

    private fun addItems(dataList: ArrayList<DataEntity>) {
        removeAllViews()
        addView(titleText)

        // 计算item之间 间距的宽度
        val dividerWidth = (viewMaxWidth - itemWidth * spanCount) / (spanCount - 1)
        var rowView: LinearLayout? = null

        dataList.forEachIndexed { index, data ->
            val params = LayoutParams(itemWidth, LayoutParams.WRAP_CONTENT)
            if (index % spanCount == 0) {
                rowView = createRowLinearLayout()
                addView(rowView)
            } else {
                params.marginStart = dividerWidth
            }
            val view = LayoutInflater.from(context).inflate(R.layout.linear_gird_item, null)
            view.tv_hot_part_item.text = data.display_name

            view.iv_hot_part_item.layoutParams.apply {
                width = itemWidth
                height = itemWidth
            }
            rowView!!.addView(view, params)
        }
    }

    /**
     * 创建新的一行View
     */
    private fun createRowLinearLayout(): LinearLayout {
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = HORIZONTAL
        val layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(0, itemTopMargin, 0, 0)
        linearLayout.layoutParams = layoutParams
        return linearLayout
    }
}

/**
 * 不可滑动
 */
class CustomGridLayoutManager(context: Context?, spanCount: Int) : androidx.recyclerview.widget.GridLayoutManager(context, spanCount) {
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