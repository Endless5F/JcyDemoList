package com.android.architecture.demolist.livedata

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.android.architecture.R
import kotlinx.android.synthetic.main.activity_live_data.*

class LiveDataActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_data)
        val viewModel = ViewModelProviders.of(this).get(LiveDataTimerViewModel::class.java)
        // LiveData数据改变的监听
        viewModel.elapsedTime.observe(this, object : Observer<Long>{
            override fun onChanged(t: Long?) {
                textView3.setText(t?.toString())
            }
        })
    }
}
