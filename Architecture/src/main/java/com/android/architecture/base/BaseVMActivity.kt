//package com.android.architecture.base
//import android.arch.lifecycle.LifecycleObserver
//import android.arch.lifecycle.ViewModelProviders
//import android.os.Bundle
//import com.android.architecture.base.BaseActivity
//import com.android.architecture.base.BaseViewModel
//
///**
// * Created by luyao
// * on 2019/5/31 16:16
// */
//abstract class BaseVMActivity<VM : BaseViewModel> : BaseActivity(), LifecycleObserver {
//
//    lateinit var mViewModel: VM
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        initVM()
//        super.onCreate(savedInstanceState)
//        startObserve()
//    }
//
//    private fun initVM() {
//        providerVMClass()?.let {
//            mViewModel = ViewModelProviders.of(this).get(it)
//            mViewModel.let(lifecycle::addObserver)
//        }
//    }
//
//    open fun providerVMClass(): Class<VM>? = null
//
//
//    open fun startObserve() {}
//
//    override fun onDestroy() {
//        mViewModel.let {
//            lifecycle.removeObserver(it)
//        }
//        super.onDestroy()
//    }
//}