package com.android.architecture.demolist.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

/**
 * ViewModel同样具有生命周期意识的处理跟UI相关的数据，并且，当设备的一些配置信息改变（例如屏幕旋转）它的数据不会消失。
 * ViewModel的另一个特点就是同一个Activity的Fragment之间可以使用ViewModel实现共享数据。
 * ViewModel相当于MVP架构的P层
 * */
class NumberViewModelFactory(val num: Int = 0) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NumberViewModel(num) as T
    }
}
