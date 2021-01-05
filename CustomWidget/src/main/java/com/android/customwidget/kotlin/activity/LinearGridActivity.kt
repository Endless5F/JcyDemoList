package com.android.customwidget.kotlin.activity

import android.os.Bundle
import com.android.customwidget.BaseActivity
import com.android.customwidget.R
import com.android.customwidget.kotlin.widget.DescInfo
import com.android.customwidget.kotlin.widget.RightGridView
import com.android.customwidget.kotlin.widget.lineargrid.Data
import com.android.customwidget.kotlin.widget.lineargrid.DataEntity
import com.android.customwidget.kotlin.widget.lineargrid.LinearGridView2
import kotlinx.android.synthetic.main.activity_linear_grid.*

class LinearGridActivity : BaseActivity() {

    var hotParts = ArrayList<DataEntity>()
    var descInfos = ArrayList<DescInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linear_grid)
        val gridView = LinearGridView2(this)
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

        val rightGridView = RightGridView(this)
        for (i in 0..5) {
            descInfos.add(DescInfo("我是$i", "#0000ff"))
        }
        rightGridView.addData(descInfos)
        rootView.addView(rightGridView)
    }
}
