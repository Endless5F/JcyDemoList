package com.android.architecture.data

import java.util.ArrayList

/**
 * Created by jcy on 2018/8/8.
 */

object HomeData {
    var addDevTotalRes: MutableList<ItemView> = ArrayList()

    init {
        addDeviceItem("", "架构组件 demo")
        addDeviceItem("&#xe620;", "Jetpack(数据绑定部分)之Lifecycles")
        addDeviceItem("&#xe620;", "Jetpack(数据绑定部分)之ViewModel")
        addDeviceItem("&#xe620;", "Jetpack(数据绑定部分)之LiveData")
        addDeviceItem("&#xe620;", "Jetpack(数据绑定部分)之DataBinding")
        addDeviceItem("&#xe620;", "Jetpack(数据绑定部分)之Paging")
        addDeviceItem("&#xe620;", "Jetpack(数据绑定部分)之Room")
        addDeviceItem("&#xe620;", "Jetpack(数据绑定部分)之Navigation")
        addDeviceItem("&#xe620;", "Jetpack(数据绑定部分)之WorkManager")
    }

    private fun addDeviceItem(icon: String, desc: String) {
        addDevTotalRes.add(ItemView(icon, desc))
    }

    class ItemView(var icon: String, var desc: String)
}
