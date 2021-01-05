package com.android.customwidget.kotlin.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.android.customwidget.R
import com.android.customwidget.kotlin.ext.dp
import kotlinx.android.synthetic.main.type_item_layout.view.*

class DescInfo {
    var name: String? = null
    var color: String? = null

    constructor(name: String?, color: String?) {
        this.name = name
        this.color = color
    }
}

class RightGridView(context: Context?, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    var rowMaxNum = 4

    init {
        orientation = VERTICAL
        gravity = Gravity.END
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun addData(dataList: List<DescInfo>) {
        var rowView: LinearLayout? = null
        dataList.forEachIndexed { index, descInfo ->
            val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            if (index % rowMaxNum == 0) {
                rowView = createRowLinearLayout()
                addView(rowView)
            } else {
                params.marginStart = 10.dp
            }
            val view = LayoutInflater.from(context).inflate(R.layout.type_item_layout, null)
            view.item_point.setColor(descInfo.color ?: "#00ff00")
            view.item_name.text = descInfo.name
            rowView!!.addView(view, params)
        }
    }

    /**
     * 创建新的一行View
     */
    private fun createRowLinearLayout(): LinearLayout {
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = HORIZONTAL
        val layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(0, 6.dp, 0, 0)
        linearLayout.layoutParams = layoutParams
        return linearLayout
    }
}