package com.android.customwidget.kotlin.widget.lineargrid

import android.content.Context
import android.graphics.Typeface
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.android.customwidget.R
import com.android.customwidget.kotlin.ext.dp
import kotlinx.android.synthetic.main.linear_gird_item.view.*

class HotPartView : LinearLayout {

    private val spanCount = 4
    private val dividerWidth = 24.dp
    private var leftMargin = 0
    private var leftPadding = 10.dp
    private val rightMargin = 15.dp
    private val itemTopMargin = 15.dp
    private val screenWidth = resources.displayMetrics.widthPixels

    private val hotPartAdapter: GridItemAdapter by lazy {
        GridItemAdapter(context)
    }

    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context)
    }

    private fun initialize(context: Context) {
        orientation = VERTICAL

//        LayoutInflater.from(context).inflate(R.layout.linear_grid_layout, this)
//        rv_hot_part.layoutManager = CustomGridLayoutManager(context, spanCount)
//        rv_hot_part.adapter = hotPartAdapter
//        rv_hot_part.addItemDecoration(GridDividerItemDecoration(context, dividerWidth, false))
    }

    fun setData(data: Any?) {
        when (data) {
            is Data -> {
                visibility = View.VISIBLE
//                hotPartAdapter.setData(data.hotParts)
                addItems(data.list)
            }
            else -> return
        }
    }

    private fun addItems(dataList: ArrayList<DataEntity>) {
        removeAllViews()
        createTitleView()

        val viewMaxWidth = screenWidth
        val itemWidth = (viewMaxWidth - dividerWidth * (spanCount - 1)) / spanCount
        Log.e("addItems", "itemWidth  $viewMaxWidth $itemWidth")
        var rowView: LinearLayout? = null
        dataList.forEachIndexed { index, data ->
            val params = LayoutParams(itemWidth, LayoutParams.WRAP_CONTENT)
            if (index % spanCount == 0) {
                rowView = createRowLinearLayout()
                addView(rowView)
            } else {
                params.marginStart = dividerWidth
            }
            params.weight = 1f
            val view = LayoutInflater.from(context).inflate(R.layout.linear_gird_item, null)
            view.tv_hot_part_item.text = data.display_name

            rowView!!.addView(view, params)
        }
    }

    private fun createTitleView() {
        var topMargin = 12.dp
        val textView = TextView(context)
        textView.text = "测试项目"
        textView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.dp_14))
        val layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(leftPadding, topMargin, 0, 0)
        textView.layoutParams = layoutParams
        addView(textView)
    }

    private fun createRowLinearLayout(): LinearLayout {
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = HORIZONTAL
        val layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.weight = 1f
        layoutParams.setMargins(leftPadding, itemTopMargin, rightMargin, 0)
        linearLayout.layoutParams = layoutParams
        return linearLayout
    }
}


/**
 * 不可滑动
 */
class CustomGridLayoutManager(context: Context?, spanCount: Int) : GridLayoutManager(context, spanCount) {
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