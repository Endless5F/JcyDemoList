package com.android.customwidget.kotlin.widget

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.android.customwidget.R
import com.android.customwidget.kotlin.ext.dp
import com.android.customwidget.kotlin.ext.measureTextWidth
import kotlinx.android.synthetic.main.linear_gird_item.view.*

/**
 * 线性靠边GridView
 * 左右两边的View分别紧靠左右，中间的View居中
 * @author jiaochengyun
 */
abstract class FormView2<T>(context: Context) : LinearLayout(context) {

    var spanCount = 4
    var itemWidth = 44.dp
    var viewMaxWidth = resources.displayMetrics.widthPixels
    var itemTextSize = resources.getDimension(R.dimen.dp_10)

    private val titleText: TextView by lazy {
        createTitleView()
    }

    init {
        orientation = VERTICAL
    }

    private fun createTitleView(): TextView {
        val textView = TextView(context)
        textView.text = context.getString(R.string.app_name)
        textView.setTextColor(context.resources.getColor(R.color.comm_main_color))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.dp_16))
        return textView
    }

    /**
     * 获取数据源中每一个位置的图片信息
     */
    abstract fun getCurrentIcon(index: Int): String

    /**
     * 获取数据源中每一个位置的文本信息
     */
    abstract fun getCurrentText(index: Int): String

    /**
     * 行间距
     * @param isFirstRow 是否是第一行
     */
    abstract fun getRowSpaceSize(isFirstRow: Boolean): Int

    /**
     * item的点击事件
     */
    abstract fun onItemClickListener(index: Int)

    /**
     * 创建新的一行View
     */
    private fun createRowLinearLayout(isFirstRow: Boolean): LinearLayout {
        val itemTopMargin = getRowSpaceSize(isFirstRow)

        val linearLayout = LinearLayout(context)
        linearLayout.orientation = HORIZONTAL
        val layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(0, itemTopMargin, 0, 0)
        linearLayout.layoutParams = layoutParams
        return linearLayout
    }

    protected open fun setItemsData(data: ArrayList<T>) {
        addItems(data)
    }

    private fun addItems(data: List<T>) {
        removeAllViews()

        addView(titleText)

        var textMax = ""
        data.forEachIndexed { index, _ ->
            val text = getCurrentText(index)
            textMax = if (text.length > textMax.length) text else textMax
        }
        val itemWidth = itemWidth.coerceAtLeast(textMax.measureTextWidth(itemTextSize))
        // 计算item之间 间距的宽度
        val dividerWidth = (viewMaxWidth - itemWidth * spanCount) / (spanCount - 1)

        var rowView: LinearLayout? = null
        data.forEachIndexed { index, _ ->
            val params = LayoutParams(itemWidth, LayoutParams.WRAP_CONTENT)
            if (index % spanCount == 0) {
                rowView = createRowLinearLayout(index < spanCount)
                addView(rowView)
            } else {
                params.marginStart = dividerWidth
            }
            val view = LayoutInflater.from(context).inflate(R.layout.linear_gird_item, null)

            // 设置文本
            val textData = getCurrentText(index)
            var textWidth = 0
            if (textData.length > 3) {
                textWidth = view.tv_item.measureTextWidth(textData) + 1
            }
            view.tv_item.apply {
                layoutParams.apply {
                    width = itemWidth.coerceAtLeast(textWidth)
                    height = width
                }
                text = textData
                layoutParams.width = itemWidth.coerceAtLeast(textWidth)
            }

            // 设置图片
            val icon = getCurrentIcon(index)
            val drawable = context.resources.getDrawable(R.color.comm_main_color)
            view.iv_item.setImageResource(R.mipmap.ic_launcher)

            view.setOnClickListener {
                onItemClickListener(index)
            }

            rowView!!.addView(view, params)
        }
    }
}