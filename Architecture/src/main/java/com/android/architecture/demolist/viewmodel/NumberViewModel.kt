package com.android.architecture.demolist.viewmodel

import android.arch.lifecycle.ViewModel

/**
 * ViewModel同样具有生命周期意识的处理跟UI相关的数据，并且，当设备的一些配置信息改变（例如屏幕旋转）它的数据不会消失。
 * ViewModel的另一个特点就是同一个Activity的Fragment之间可以使用ViewModel实现共享数据。
 * ViewModel相当于MVP架构的P层
 *
 * 因为ViewModel的生命周期是和Activity分开的，所以在ViewModel中禁止引用任何View对象或者任何引用了Activity的Context的实例对象。
 * 如果ViewModel中需要Application的context可以继承AndroidViewModel类。
 * 用户主动按了返回Home键，主动销毁了这个Activity呢？这时候系统会调用ViewModel的onClear()方法 清除ViewModel中的数据。
 * ViewModel 生命周期是贯穿整个 activity 生命周期，包括 Activity 因旋转造成的重创建，直到 Activity 真正意义上销毁后才会结束。
 * 因此ViewModel相当于只有一个生命周期，即onClear()，该方法在Activity的onDestory()之后触发
 * */
class NumberViewModel(val num: Int = 0) : ViewModel() {
    var number = 0
}
