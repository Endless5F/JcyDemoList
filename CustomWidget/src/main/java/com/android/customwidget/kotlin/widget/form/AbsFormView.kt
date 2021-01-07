package com.android.customwidget.kotlin.widget.form

import android.content.Context
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
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
     * 水平靠右
     */
    protected val horizontalRight = 2

    /**
     * 散开模式，即靠边居中或者说左右两边的View分别紧靠左右，中间的View居中
     */
    protected val spreadOutCenter = 3

    init {
        orientation = VERTICAL
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
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
        return if (getOrientationType() == vertical) {
            R.layout.form_item_layout_vertical
        } else R.layout.form_item_layout_horizontal
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
        return spreadOutCenter
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

        // 为了适配文本信息比图片宽的情况
        // 由于不一定只有一行显示，因此需要遍历所有item的文本信息，获取最大值
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

        val itemWidth = if (isVerticalSpreadOutCenter()) getItemMaxWidth(data) else 0
        // 计算item之间 间距的宽度
        val dividerWidth = if (!isVerticalSpreadOutCenter()) 0 else (getViewMaxWidth() - itemWidth * spanCount) / (spanCount - 1)

        var rowView: LinearLayout? = null
        data.forEachIndexed { index, _ ->
            val contentWidth = when {
                isVerticalSpreadOutCenter() -> itemWidth
                else -> LayoutParams.WRAP_CONTENT
            }
            val params = LayoutParams(contentWidth, LayoutParams.WRAP_CONTENT)
            if (index % spanCount == 0) {
                rowView = createRowLinearLayout(index < spanCount)
                // 设置靠右显示逻辑
                if (getGravityType() == horizontalRight) rowView?.gravity = Gravity.END
                addView(rowView)
            } else {
                // 设置垂直并且两边对齐居中逻辑
                if (isVerticalSpreadOutCenter()) {
                    // 若dividerWidth < 0，则可能出现item之间重叠的情况
                    params.marginStart = dividerWidth
                }
            }
            // 设置居中逻辑和靠左、靠右显示间距
            when (getGravityType()) {
                horizontalCenter -> params.weight = 1f
                horizontalLeft, horizontalRight -> params.marginStart = getColumnSpaceSize(index % spanCount == 0)
            }
            val view = LayoutInflater.from(context).inflate(getLayoutId(), null)

            // 设置水平并且两边对齐居中逻辑
            if (isHorizontalSpreadOutCenter()) {
                params.weight = 1f
                params.gravity = Gravity.CENTER
                when (index % spanCount) {
                    0 -> { // 第一列
                        when (view) {
                            is LinearLayout -> view.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                            is RelativeLayout -> view.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                        }
                    }
                    spanCount - 1 -> { // 最后一列
                        when (view) {
                            is LinearLayout -> view.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                            is RelativeLayout -> view.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                        }
                    }
                    else -> {
                        when (view) {
                            is LinearLayout -> view.gravity = Gravity.CENTER
                            is RelativeLayout -> view.gravity = Gravity.CENTER
                        }
                    }
                }
            }

            view.tv_item.apply {
                text = getCurrentText(index)
                setTextSize(COMPLEX_UNIT_PX, getItemTextSize())
            }

            view.iv_item.layoutParams.apply {
                width = if (isVerticalSpreadOutCenter()) itemWidth else getIconWidth()
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
     * 是否为垂直并且散开模式
     */
    private fun isVerticalSpreadOutCenter(): Boolean {
        return getOrientationType() == vertical && getGravityType() == spreadOutCenter
    }

    /**
     * 是否为水平并且散开模式
     */
    private fun isHorizontalSpreadOutCenter(): Boolean {
        return getOrientationType() == horizontal && getGravityType() == spreadOutCenter
    }
}