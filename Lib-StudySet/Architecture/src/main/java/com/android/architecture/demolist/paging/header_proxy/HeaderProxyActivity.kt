package com.android.architecture.demolist.paging.header_proxy

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.architecture.R
import com.android.architecture.demolist.paging.viewmodel.CommonViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_header_proxy.*
import java.util.concurrent.TimeUnit

class HeaderProxyActivity : AppCompatActivity() {

    private lateinit var mAdapter: HeaderProxyAdapter

    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T = CommonViewModel(application) as T
        }).get(CommonViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_header_proxy)

        mAdapter = HeaderProxyAdapter()
        recyclerView.adapter = mAdapter

        binds()

        viewModel.getRefreshLiveData().observe(this, Observer { mAdapter.submitList(it) })
    }

    private fun binds() {
        // 模拟下拉刷新
        mSwipeRefreshLayout.setOnRefreshListener {
            mSwipeRefreshLayout.isRefreshing = true
            Observable.just(0)
                    .delay(2, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        mSwipeRefreshLayout.isRefreshing = false
                        mAdapter.submitList(null)
                        viewModel.getRefreshLiveData()
                                .observe(this, Observer { mAdapter.submitList(it) })
                    }
                    .subscribe()
        }
    }
}
