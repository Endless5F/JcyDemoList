package com.android.customwidget.kotlin.widget.floatwindow

import android.animation.ObjectAnimator
import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout

/**
 * 可拖动，自动吸附两边
 */
abstract class BaseDragFloatWindow(context: Context, layoutId: Int) : LinearLayout(context), GestureDetector.OnGestureListener {

    /** 初始Y坐标   */
    var sTopStartY = 1400f

    /** 头部限制高度  */
    val TOP_HEIGHT = 0f

    /** 底部限制高度  */
    val BOTTOM_HEIGHT = 0f

    /** 监听点击  */
    var mViewClickListener: OnViewClickListener? = null

    private var parentHeight = 0
    private var parentWidth = 0

    private var lastX = 0
    private var lastY = 0

    private var isDrag = false

    init {
        isClickable = true
        LayoutInflater.from(context).inflate(layoutId, this)
        y = sTopStartY
    }

    /**
     * 设置点击监听
     *
     * @param viewClickListener
     */
    open fun setOnViewClickListener(viewClickListener: OnViewClickListener?) {
        mViewClickListener = viewClickListener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val rawX = event.rawX.toInt()
        val rawY = event.rawY.toInt()
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true // 显示按压效果(android:state_pressed)，同setEnabled、setSelected
                isDrag = false
                parent.requestDisallowInterceptTouchEvent(true)
                lastX = rawX
                lastY = rawY
                if (parent != null) {
                    val parent = parent as ViewGroup
                    parentHeight = parent.height
                    parentWidth = parent.width
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (parentHeight <= 0.2 || parentWidth <= 0.2) {
                    isDrag = false
                } else {
                    isDrag = true

                    this.alpha = 0.9f
                    val dx = rawX - lastX
                    val dy = rawY - lastY
                    // 这里修复一些华为手机无法触发点击事件
                    val distance = Math.sqrt(dx * dx + dy * dy.toDouble()).toInt()
                    if (distance == 0) {
                        isDrag = false
                    } else {
                        var x = x + dx
                        var y = y + dy
                        //检测是否到达边缘 左上右下
                        x = if (x < 0) 0f else if (x > parentWidth - width) (parentWidth - width).toFloat() else x
                        y = if (getY() < 0) 0f else if (getY() + height > parentHeight) (parentHeight - height).toFloat() else y
                        setX(x)
                        setY(y)
                        lastX = rawX
                        lastY = rawY
                        Log.i("aa", "isDrag=" + isDrag + "getX=" + getX() + ";getY=" + getY() + ";parentWidth=" + parentWidth)
                    }
                }
            }
            MotionEvent.ACTION_UP -> if (!isNotDrag()) {
                isPressed = false // 恢复按压效果
                // Log.i("getX="+getX()+"；screenWidthHalf="+screenWidthHalf);
                moveHide(rawX)
            }
        }
        //如果是拖拽则消s耗事件，否则正常传递即可。
        return !isNotDrag() || super.onTouchEvent(event)
    }

    open fun isNotDrag(): Boolean {
        return !isDrag && (x == 0f || x == parentWidth - width.toFloat())
    }

    open fun moveHide(rawX: Int) {
        if (rawX >= parentWidth / 2) {
            //靠右吸附
            animate().setInterpolator(DecelerateInterpolator())
                    .setDuration(500)
                    .xBy(parentWidth - width - x)
                    .start()
        } else {
            //靠左吸附
            val oa = ObjectAnimator.ofFloat(this, "x", x, 0f)
            oa.interpolator = DecelerateInterpolator()
            oa.duration = 500
            oa.start()
        }
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