package com.android.customwidget.kotlin.widget.form

import android.content.Context
import com.android.customwidget.R
import com.android.customwidget.kotlin.ext.dp

class HorizontalFormView(context: Context) : AbsFormView<FormItemEntity>(context) {

    override fun getLayoutId(): Int {
        return R.layout.form_item_layout_horizontal
    }

    override fun getOrientationType(): Int {
        return horizontal
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