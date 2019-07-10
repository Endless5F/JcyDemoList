package com.android.customwidget.kotlin.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager

import com.android.customwidget.R
import com.android.customwidget.kotlin.adapter.TimeRvAdapter
import com.android.customwidget.kotlin.bean.Program
import com.android.customwidget.kotlin.utils.FormatUtil
import kotlinx.android.synthetic.main.activity_scroll_recycle_view.*

class ScrollRecycleViewActivity : AppCompatActivity() {

    private var timeList = arrayListOf<String>()
    private val contentList = arrayListOf<Program>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scroll_recycle_view)

        val linearManager = LinearLayoutManager(this)
        linearManager.orientation = LinearLayoutManager.HORIZONTAL
        rv_time.layoutManager = linearManager

        val linearManager2 = LinearLayoutManager(this)
        linearManager2.orientation = LinearLayoutManager.VERTICAL
        rv_content.layoutManager = linearManager2

        initData()
        initRecycle()
    }

    private fun initData() {
        (1..100).forEach {
            timeList.add(FormatUtil.stampToDate(System.currentTimeMillis() + it * 1000))
        }

        (1..50).forEach {
            contentList.add(Program("name$it",it,"The Program name is name$it , num is $it"))
        }
    }

    private fun initRecycle() {
        rv_time.adapter = TimeRvAdapter(this, timeList)
    }


}
