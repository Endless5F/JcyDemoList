package com.android.customwidget.kotlin.widget.form

import android.content.Context
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.android.customwidget.R
import com.android.customwidget.kotlin.ext.dp
import com.android.customwidget.kotlin.ext.measureTextWidth
import kotlinx.android.synthetic.main.form_item_layout_vertical.view.*

/**
 * 表格View，适用于数据较少的情况
 *
 * @author jiaochengyun
 */
abstract class AbsFormView<T>(context: Context) : LinearLayout(context) {

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
     * 获取文本字体大小
     */
    protected open fun getItemTextSize(): Float {
        return 10.dp.toFloat()
    }

    /**
     * 获取View最大宽度
     */
    protected open fun getViewMaxWidth(): Int {
        return resources.displayMetrics.widthPixels
    }

    /**
     * 一行最大个数
     */
    protected open fun getSpanCount(): Int {
        return 4
    }

    /**
     * 行间距
     * @param isFirstRow 是否是第一行
     */
    protected open fun getRowSpaceSize(isFirstRow: Boolean): Int {
        return if (isFirstRow) 12.dp else 8.dp
    }

    /**
     * 获取item间距(列间距)，仅 getGravityType() == horizontalLeft时有效
     */
    protected open fun getColumnSpaceSize(isFirstColumn: Boolean): Int {
        return if (isFirstColumn) 0.dp else 8.dp
    }

    /**
     * 获取方向类型
     * @return vertical：图片和文本垂直排列，horizontal：图片和文本水平排列
     */
    protected open fun getOrientationType(): Int {
        return vertical
    }

    /**
     * 对齐方式
     */
    protected open fun getGravityType(): Int {
        return onEachSideCenter
    }

    /**
     * 图片和文本间距
     */
    protected open fun getIconAndTextSpace(): Int {
        return 5.dp
    }

    /**
     * 获取当前item宽度
     */
    protected open fun getItemWidth(index: Int): Int {
        val iconWidth = getIconWidth()
        val text = getCurrentText(index)
        val textWidth = text.measureTextWidth(getItemTextSize()) + 1
        return if (getOrientationType() == vertical) {
            iconWidth.coerceAtLeast(textWidth)
        } else {
            iconWidth + getIconAndTextSpace() + textWidth
        }
    }

    /**
     * 获取item最大宽度
     */
    protected open fun getItemMaxWidth(data: ArrayList<T>): Int {
        val iconWidth = getIconWidth()
        var textMaxWidth = 0

        data.forEachIndexed { index, _ ->
            val text = getCurrentText(index)
            val textWidth = text.measureTextWidth(getItemTextSize()) + 1
            textMaxWidth = if (textMaxWidth > textWidth) textMaxWidth else textWidth
        }

        return if (getOrientationType() == vertical) {
            iconWidth.coerceAtLeast(textMaxWidth)
        } else {
            iconWidth + getIconAndTextSpace() + textMaxWidth
        }
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

    private fun addItems(data: ArrayList<T>) {
        removeAllViews()
        val spanCount = getSpanCount()

        val itemWidth = if (isVerticalOnEachSideCenter()) getItemMaxWidth(data) else 0
        // 计算item之间 间距的宽度
        val dividerWidth = if (!isVerticalOnEachSideCenter()) 0 else (getViewMaxWidth() - itemWidth * spanCount) / (spanCount - 1)

        var rowView: LinearLayout? = null
        data.forEachIndexed { index, _ ->
            val contentWidth = if (isVerticalOnEachSideCenter()) itemWidth else LayoutParams.WRAP_CONTENT
            val params = LayoutParams(contentWidth, LayoutParams.WRAP_CONTENT)
            if (index % spanCount == 0) {
                rowView = createRowLinearLayout(index < spanCount)
                addView(rowView)
            } else {
                if (isVerticalOnEachSideCenter()) {
                    // 若dividerWidth < 0，则可能出现item之间重叠的情况
                    params.marginStart = dividerWidth
                }
            }
            when(getGravityType()) {
                horizontalLeft -> params.marginStart = getColumnSpaceSize(index % spanCount == 0)
                horizontalCenter -> params.weight = 1f
            }
            val view = LayoutInflater.from(context).inflate(getLayoutId(), null)

            view.tv_item.apply {
                text = getCurrentText(index)
                setTextSize(COMPLEX_UNIT_PX, getItemTextSize())
            }

            view.iv_item.layoutParams.apply {
                width = if (isVerticalOnEachSideCenter()) itemWidth else getIconWidth()
                height = width
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

    /**
     * 是否为垂直并且两边对齐居中
     */
    private fun isVerticalOnEachSideCenter(): Boolean {
        return getOrientationType() == vertical && getGravityType() == onEachSideCenter
    }
}