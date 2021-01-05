package com.android.customwidget.kotlin.widget.lineargrid

import android.content.Context
import com.android.customwidget.kotlin.ext.dp
import com.android.customwidget.kotlin.widget.FormView
import com.android.customwidget.kotlin.widget.FormView2

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
class LinearGridView2(context: Context) : FormView2<DataEntity>(context) {

    val data: Data? = null

    private val hotPartAdapter: GridItemAdapter by lazy {
        GridItemAdapter(context)
    }

    init {
//        LayoutInflater.from(context).inflate(R.layout.linear_grid_layout, this)
//        rv_hot_part.layoutManager = NoScrollGridLayoutManager(context, spanCount)
//        rv_hot_part.adapter = hotPartAdapter
//        rv_hot_part.addItemDecoration(GridDividerItemDecoration(context, dividerWidth, false))
    }

    fun setData(data: Data) {
        setItemsData(data.list)
    }

    override fun getCurrentIcon(index: Int): String {
        return ""
    }

    override fun getCurrentText(index: Int): String {
        return data?.list!![index].display_name ?: ""
    }

    override fun onItemClickListener(index: Int) {

    }

    override fun getRowSpaceSize(isFirstRow: Boolean): Int {
        return if (isFirstRow) 12.dp else 16.dp
    }
}