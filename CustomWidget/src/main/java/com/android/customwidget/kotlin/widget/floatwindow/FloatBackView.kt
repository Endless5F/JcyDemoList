package com.android.customwidget.kotlin.widget.floatwindow

import android.content.Context
import android.text.TextUtils
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.android.customwidget.R
import com.android.customwidget.util.ScreenUtils

class FloatBackView(context: Context) : LinearLayout(context), GestureDetector.OnGestureListener {

    /** y坐标  */
    var offsetY = 0f

    /** 初始Y坐标   */
    var sTopStartY = 574f

    /** 头部限制高度  */
    val TOP_HEIGHT = 0f

    /** 底部限制高度  */
    val BOTTOM_HEIGHT = 0f

    /** mTouchStartY   */
    var mTouchStartY = 0f

    /** 监测手势  */
    var gestureDetector: GestureDetector? = null

    /** 关闭按钮  */
    var mCloseImage: ImageView? = null

    /** 显示文案  */
    var mTvBackView: TextView? = null

    /** 监听点击  */
    var mViewClickListener: OnBackViewClickListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.float_back_layout, this)
        gestureDetector = GestureDetector(context, this)
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

    /**
     * 设置点击监听
     *
     * @param viewClickListener
     */
    open fun setOnBackViewClickListener(viewClickListener: OnBackViewClickListener?) {
        mViewClickListener = viewClickListener
    }

    /**
     * 设置返回按钮显示文案
     *
     * @param wordText
     */
    open fun setWordText(wordText: String?) {
        if (!TextUtils.isEmpty(wordText) && mTvBackView != null) {
            mTvBackView!!.text = wordText
        }
    }

    /**
     * 动态更新位置
     */
    private fun updateViewPosition() {
        sTopStartY = offsetY - mTouchStartY
        setY(sTopStartY)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        offsetY = event.rawY
        if (y >= TOP_HEIGHT && offsetY <= ScreenUtils.getScreenHeight() - BOTTOM_HEIGHT) {
            gestureDetector!!.onTouchEvent(event)
        }
        return true
    }

    override fun onDown(event: MotionEvent): Boolean {
        offsetY = event.rawY
        mTouchStartY = event.y
        return false
    }

    override fun onShowPress(e: MotionEvent?) {}

    override fun onLongPress(e: MotionEvent?) {}

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
//        if (mViewClickListener != null) {
//            mViewClickListener!!.onBackViewClick()
//        }
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        updateViewPosition()
        return false
    }


    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    interface OnBackViewClickListener {
        /**
         * 点击关闭按钮
         */
        fun onCloseBtnClick()

        /**
         * 点击返回按钮自身区域
         */
        fun onBackViewClick()
    }
}