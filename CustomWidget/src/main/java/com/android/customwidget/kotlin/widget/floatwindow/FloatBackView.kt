package com.android.customwidget.kotlin.widget.floatwindow

import android.content.Context
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.android.customwidget.R
import com.android.customwidget.util.ScreenUtils

class FloatBackView(context: Context) : BaseFloatWindow(context, R.layout.float_back_layout) {

    /** 关闭按钮  */
    var mCloseImage: ImageView? = null

    /** 显示文案  */
    var mTvBackView: TextView? = null

    init {
        mCloseImage = findViewById(R.id.iv_close)
        mTvBackView = findViewById(R.id.text_title)
        mTvBackView?.setOnClickListener { mViewClickListener?.onBackViewClick() }
        mCloseImage?.setOnClickListener { mViewClickListener?.onCloseBtnClick() }
        val layoutParams = FrameLayout.LayoutParams(ScreenUtils.dp2px(100f), ScreenUtils.dp2px(50f))
        setLayoutParams(layoutParams)
        offsetY = sTopStartY
        y = offsetY
        x = (ScreenUtils.getScreenWidth() - ScreenUtils.dp2px(100f)).toFloat()
        setBackgroundResource(R.color.comm_main_color)
    }
}