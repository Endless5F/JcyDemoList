package com.android.customwidget.kotlin.widget.form

import android.content.Context
import com.android.customwidget.kotlin.ext.dp

class HorizontalFormView(context: Context) : AbsFormView<FormItemEntity>(context) {

    override fun getSpanCount(): Int {
        return 3
    }

    override fun getOrientationType(): Int {
        return horizontal
    }

    override fun getGravityType(): Int {
        return spreadOutCenter
    }

    override fun getIconWidth(): Int {
        return 45.dp
    }

    override fun getCurrentIcon(index: Int): String {
        return dataList[index].icon
    }

    override fun getCurrentText(index: Int): String {
        return dataList[index].display_name
    }

    override fun onItemClickListener(index: Int) {

    }
}