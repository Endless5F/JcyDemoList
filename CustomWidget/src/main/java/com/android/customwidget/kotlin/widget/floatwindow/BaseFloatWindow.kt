package com.android.customwidget.kotlin.widget.floatwindow

import android.content.Context
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.LinearLayout

abstract class BaseFloatWindow(context: Context, layoutId: Int) : LinearLayout(context), GestureDetector.OnGestureListener {
    /** y坐标  */
    var offsetY = 0f

    /** 初始Y坐标   */
    var sTopStartY = 1400f

    /** 头部限制高度  */
    val TOP_HEIGHT = 0f

    /** 底部限制高度  */
    val BOTTOM_HEIGHT = 0f

    /** mTouchStartY   */
    var mTouchStartY = 0f

    /** 监测手势  */
    var gestureDetector: GestureDetector? = null

    /** 监听点击  */
    var mViewClickListener: OnViewClickListener? = null

    val screenWidth = resources.displayMetrics.widthPixels
    val screenHeight = resources.displayMetrics.heightPixels

    init {
        LayoutInflater.from(context).inflate(layoutId, this)
        gestureDetector = GestureDetector(context, this)
        offsetY = sTopStartY
        y = offsetY
    }

    /**
     * 设置点击监听
     *
     * @param viewClickListener
     */
    open fun setOnViewClickListener(viewClickListener: OnViewClickListener?) {
        mViewClickListener = viewClickListener
    }

    /**
     * 动态更新位置
     */
    private fun updateViewPosition() {
        sTopStartY = offsetY - mTouchStartY
        y = sTopStartY
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        offsetY = event.rawY
        if (y >= TOP_HEIGHT && offsetY <= screenHeight - BOTTOM_HEIGHT) {
            gestureDetector!!.onTouchEvent(event)
        }
        return super.dispatchTouchEvent(event)
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

    interface OnViewClickListener {
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