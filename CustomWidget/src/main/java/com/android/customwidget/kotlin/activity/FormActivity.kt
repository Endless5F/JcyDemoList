package com.android.customwidget.kotlin.activity

import android.os.Bundle
import com.android.customwidget.BaseActivity
import com.android.customwidget.R
import com.android.customwidget.kotlin.widget.form.FormItemEntity
import com.android.customwidget.kotlin.widget.form.VerticalFormView
import kotlinx.android.synthetic.main.activity_form.*

class FormActivity : BaseActivity() {

    var formData = ArrayList<FormItemEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)
        val formView = VerticalFormView(this)
        formData.apply {
            add(FormItemEntity("数据1", ""))
            add(FormItemEntity("数据2", ""))
            add(FormItemEntity("数据3", ""))
            add(FormItemEntity("数据4", ""))
            add(FormItemEntity("数据5", ""))
            add(FormItemEntity("数据6", ""))
            add(FormItemEntity("数据7", ""))
            add(FormItemEntity("数据8", ""))
        }
        formView.apply {
            setItemsData(formData)
        }

        rootView.addView(formView)
    }
}
