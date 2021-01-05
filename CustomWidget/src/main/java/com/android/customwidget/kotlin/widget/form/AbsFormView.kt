package com.android.customwidget.kotlin.widget.form

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.android.customwidget.R
import com.android.customwidget.kotlin.ext.dp
import com.android.customwidget.kotlin.ext.measureTextWidth
import kotlinx.android.synthetic.main.form_item_layout_vertical.view.*

/**
 * 表格View
 *
 * @author jiaochengyun
 */
abstract class AbsFormView<T>(context: Context) : LinearLayout(context) {

    var spanCount = 4
    private var itemWidth = 45.dp
    var itemTextSize = resources.getDimension(R.dimen.dp_10)
    private var viewMaxWidth = resources.displayMetrics.widthPixels

    protected var dataList = arrayListOf<T>()

    /**
     * 内容(图片和文本)排列方向
     */
    protected val horizontal = 0
    protected val vertical = 1

    /**
     * 水平靠左
     */
    protected val horizontalLeft = 0
    /**
     * 水平居中
     */
    protected val horizontalCenter = 1

    /**
     * 靠边中心，即左右两边的View分别紧靠左右，中间的View居中
     */
    protected val onEachSideCenter = 2

    init {
        orientation = VERTICAL
    }

    /**
     * 获取图片宽度，宽高一致
     */
    abstract fun getIconWidth(): Int

    /**
     * 获取数据源中每一个位置的图片信息
     */
    abstract fun getCurrentIcon(index: Int): String

    /**
     * 获取数据源中每一个位置的文本信息
     */
    abstract fun getCurrentText(index: Int): String

    /**
     * item的点击事件
     */
    abstract fun onItemClickListener(index: Int)

    protected open fun getLayoutId(): Int {
        return R.layout.form_item_layout_vertical
    }

    /**
     * 行间距
     * @param isFirstRow 是否是第一行
     */
    protected open fun getRowSpaceSize(isFirstRow: Boolean): Int {
        return if (isFirstRow) 12.dp else 8.dp
    }

    /**
     * 获取方向类型
     * @return vertical：图片和文本垂直排列，horizontal：图片和文本水平排列
     */
    protected open fun getOrientationType(): Int {
        return vertical
    }

    /**
     * 对齐方式，
     */
    protected open fun getGravityType(): Int {
        return onEachSideCenter
    }

    protected open fun getIconAndTextSpace(): Int {
        return 5.dp
    }

    /**
     * item的点击事件
     */
    protected open fun initItemWidth(data: ArrayList<T>): Int {
        val iconWidth = getIconWidth()
        var textMax = ""
        data.forEachIndexed { index, _ ->
            val text = getCurrentText(index)
            textMax = if (text.length > textMax.length) text else textMax
        }
        return iconWidth.coerceAtLeast(textMax.measureTextWidth(itemTextSize))
    }

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

    fun setItemsData(data: ArrayList<T>) {
        dataList.clear()
        dataList.addAll(data)

        addItems(data)
    }

    private fun addItems(data: List<T>) {
        removeAllViews()
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
            val view = LayoutInflater.from(context).inflate(getLayoutId(), null)

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