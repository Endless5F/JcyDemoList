package com.android.customwidget

import android.app.Application
import com.android.customwidget.util.AppUtils

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppUtils.init(this)
    }
}