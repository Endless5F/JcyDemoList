package com.android.architecture.demolist.paging.basic

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.architecture.R
import com.android.architecture.demolist.paging.viewmodel.CommonViewModel
import kotlinx.android.synthetic.main.activity_basic_usage.*

class BasicUsageActivity : AppCompatActivity() {

    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T = CommonViewModel(application) as T
        }).get(CommonViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic_usage)

        val adapter = BasicStudentAdapter()
        recyclerView.adapter = adapter

        // adapter.submitList(it) 设置要展示的新列表
        viewModel.getRefreshLiveData().observe(this, Observer { adapter.submitList(it) })
    }
}
