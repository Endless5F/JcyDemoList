package com.android.performanceanalysis.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.performanceanalysis.R
import com.android.performanceanalysis.aop.SingleClick
import kotlinx.android.synthetic.main.activity_aop_demo.*

class AopDemoActivity : AppCompatActivity() {
    var nornalSum = 0
    var singleSum = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aop_demo)
        aop_button.setOnClickListener {
            normal()
            single()
        }
    }

    //普通的方法
    fun normal() {
        normal_text.text = "点击次数:${nornalSum++}次"
    }

    //使用@SingleClick注解表示该方法防止抖动
    @SingleClick
    fun single() {
        aop_text.text = "防止多次点击:${singleSum++}次"
    }
}
