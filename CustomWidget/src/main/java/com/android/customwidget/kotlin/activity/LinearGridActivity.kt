package com.android.customwidget.kotlin.activity

import android.os.Bundle
import com.android.customwidget.BaseActivity
import com.android.customwidget.R
import com.android.customwidget.kotlin.widget.lineargrid.Data
import com.android.customwidget.kotlin.widget.lineargrid.DataEntity
import com.android.customwidget.kotlin.widget.lineargrid.LinearGridView
import kotlinx.android.synthetic.main.activity_linear_grid.*

class LinearGridActivity : BaseActivity() {

    var hotParts = ArrayList<DataEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linear_grid)
        val gridView = LinearGridView(this)
        hotParts.apply {
            add(DataEntity("数据1", ""))
            add(DataEntity("数据2", ""))
            add(DataEntity("数据3", ""))
            add(DataEntity("数据4", ""))
            add(DataEntity("数据5", ""))
            add(DataEntity("数据6", ""))
            add(DataEntity("数据7", ""))
            add(DataEntity("数据8", ""))
        }
        gridView.apply {
            setData(Data(hotParts))
        }
        rootView.addView(gridView)
    }
}
