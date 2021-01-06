package com.android.customwidget

import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.android.customwidget.kotlin.widget.floatwindow.FloatBackManager

open class BaseActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        if (isImmersiveStatusBar()) {
            val window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
            val uiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or if (isLightStatusBar()) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.decorView.systemUiVisibility = uiVisibility
        }
        if (!TextUtils.isEmpty(FloatBackManager.instance?.schemeBackFrom)) {
            FloatBackManager.instance?.addFloatBackView(this, window.decorView as FrameLayout)
        }
    }

    override fun onPause() {
        super.onPause()
        // 触发
        FloatBackManager.instance?.schemeBackFrom = touchFloatBackEvent()
    }

    open fun isImmersiveStatusBar(): Boolean {
        return true
    }

    open fun isLightStatusBar(): Boolean {
        return true
    }

    open fun touchFloatBackEvent(): String? {
        return null
    }
}