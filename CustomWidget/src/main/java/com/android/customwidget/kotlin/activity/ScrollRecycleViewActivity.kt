package com.android.customwidget.kotlin.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager

import com.android.customwidget.R
import com.android.customwidget.kotlin.adapter.ContentRvAdapter
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
        initListener()
//        toast("Hello, World")
    }

    private fun initData() {
        (1..50).forEach {
            timeList.add(FormatUtil.stampToDate(System.currentTimeMillis() + it * 1000))
        }

        (1..50).forEach {
            contentList.add(Program("name$it",it,"The Program name is name$it , num is $it"))
        }
    }

    private fun initRecycle() {
        val tras = supportFragmentManager.beginTransaction()
        tras.commit()
        rv_time.adapter = TimeRvAdapter(this, timeList)
        rv_content.adapter = ContentRvAdapter(this, contentList)
    }

    private fun initListener() {
        ll_touch.setDragRecycleView(rv_time)
//        val clazz = rv_time::class.java.javaClass
//        val method = clazz.getDeclaredMethod("setScrollState", Int::class.javaPrimitiveType)
//        method.isAccessible = true
//        val declaredConstructor = clazz.getConstructor(Context::class.javaPrimitiveType)
//        method.invoke(declaredConstructor.newInstance(this),1)
//        rv_content.setOnTouchListener { _, event ->
//            rv_time.isLayoutFrozen = false
//            val state = rv_time.scrollState
//            Log.d("scrollState", "scrollState $state")
//            rv_time.onTouchEvent(event)
////            rv_content.onTouchEvent(event)
//            true
//        }

//        ll_touch.setOnTouchListener { _, event ->
//            rv_time.isLayoutFrozen = false
//            val state = rv_time.scrollState
//            Log.d("scrollState", "scrollState $state")
//            rv_time.onTouchEvent(event)
//            true
//        }
    }

}
