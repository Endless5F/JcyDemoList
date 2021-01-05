package com.android.customwidget.kotlin.widget.floatwindow

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.widget.FrameLayout
import android.widget.Toast
import com.android.customwidget.BuildConfig
import com.android.customwidget.util.AppUtils

class FloatBackManager private constructor() {

    companion object {
        @Volatile
        private var sInstance: FloatBackManager? = null

        /**
         * 单例获取方法
         * 因为要频繁检查是否展示并且要维护单一BackView变量所以设置为单例模式
         *
         * @return HomeSchemeBackViewManager单例类
         */
        val instance: FloatBackManager?
            get() {
                if (sInstance == null) {
                    synchronized(FloatBackManager::class.java) {
                        if (sInstance == null) {
                            sInstance = FloatBackManager()
                        }
                    }
                }
                return sInstance
            }
    }

    /**
     * 需要返回的scheme或者url
     */
    var schemeBackFrom: String? = null

    /**
     * 当前悬浮View
     */
    private var mFloatBackView: FloatBackView? = null

    /**
     * 不可使用单例，会造成内存泄露
     */
    fun addFloatBackView(activity: Activity?, parent: FrameLayout?) {
        if (mFloatBackView == null) {
            mFloatBackView = activity?.let { FloatBackView(it) }
        }
        mFloatBackView?.apply {
            setOnViewClickListener(object : BaseFloatWindow.OnViewClickListener {
                override fun onCloseBtnClick() {
                    removeFloatBackView(parent)
                }

                override fun onBackViewClick() {
                    removeFloatBackView(parent)
                    // 跳转项目pk页
                    openOtherActivityWithScheme(schemeBackFrom)
                }
            })
            if (mFloatBackView != null) {
                if (mFloatBackView!!.parent != null) { // 防止重复add造成crash
                    (mFloatBackView!!.parent as FrameLayout).removeView(mFloatBackView)
                }
                parent?.addView(mFloatBackView)
            }
        }
    }

    fun removeFloatBackView(parent: FrameLayout?) {
        if (parent != null && mFloatBackView != null && mFloatBackView!!.parent === parent) {
            parent.removeView(mFloatBackView)
        }
    }

    @SuppressLint("ShowToast")
    fun openOtherActivityWithScheme(scheme: String?) {
        if (TextUtils.isEmpty(scheme)) {
            return
        }
        val uri = Uri.parse(scheme)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            AppUtils.getApp().startActivity(intent)
            schemeBackFrom = null
        } catch (e: Exception) { // Uri 不合法
            if (BuildConfig.DEBUG) {
                Toast.makeText(AppUtils.getApp(), "Uri 不合法: =$uri", Toast.LENGTH_LONG).show()
            }
        }
    }
}