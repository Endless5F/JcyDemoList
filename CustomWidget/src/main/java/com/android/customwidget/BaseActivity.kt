package com.android.customwidget

import android.annotation.SuppressLint
import android.text.TextUtils
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.android.customwidget.kotlin.widget.floatwindow.FloatBackManager

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        if (!TextUtils.isEmpty(FloatBackManager.instance?.schemeBackFrom)) {
            FloatBackManager.instance?.addFloatBackView(this, window.decorView as FrameLayout)
        }
    }

    override fun onPause() {
        super.onPause()
        // 触发
        FloatBackManager.instance?.schemeBackFrom = touchFloatBackEvent()
    }

    open fun touchFloatBackEvent(): String? {
        return null
    }
}