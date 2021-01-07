package com.android.customwidget.kotlin.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.customwidget.BaseActivity
import com.android.customwidget.R
import com.android.customwidget.kotlin.adapter.TimeRvAdapter
import com.android.customwidget.kotlin.utils.FormatUtil
import kotlinx.android.synthetic.main.activity_xi_ding.*

/**
 * 吸顶效果：
 * 若AppBarLayout临近下方的RecyclerView内部需要嵌套RecycleView，则不会有效果或者效果不好，
 * 此时内部RecyclerView需要设置 rv.isNestedScrollingEnabled = false 属性，
 * 此属性设置后内部RecyclerView则失去view复用的能力，因此建议通过viewType使用单层RecyclerView
 */
class CeilingSuctionActivity : BaseActivity() {

    private var timeList = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("jcy", "getItemMaxWidth start ${System.currentTimeMillis()}")
        setContentView(R.layout.activity_xi_ding)
        Log.e("jcy", "getItemMaxWidth end ${System.currentTimeMillis()}")

        val linearManager = LinearLayoutManager(this)
        linearManager.orientation = LinearLayoutManager.VERTICAL
        rv.layoutManager = linearManager
//        rv.isNestedScrollingEnabled = false
        initData()
        initRecycle()
    }

    private fun initData() {
        (1..50).forEach {
            timeList.add(FormatUtil.stampToDate(System.currentTimeMillis() + it * 1000))
        }
    }

    private fun initRecycle() {
        rv.adapter = TimeRvAdapter(this, timeList)
    }
}
