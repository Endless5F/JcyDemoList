package com.android.customwidget.kotlin.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.LinearLayout
import android.view.ViewConfiguration
import kotlin.math.abs

/**
 * Created by Jcy on 2019/7/12.
 */
class ThouchLinearLayout @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null
            , defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        // 空闲状态
        const val SCROLL_STATE_IDLE = 0
        // 滑动状态
        const val SCROLL_STATE_DRAGGING = 1
        // 滑动后自然沉降的状态
        const val SCROLL_STATE_SETTLING = 2

        // 无效手指，防止多指触控时，第一支手指抬起时造成的滑动晃动
        const val INVALID_POINTER = -1
    }


    private var initX = 0
    private var initY = 0
    private var interceptX = 0f
    private var interceptY = 0f
    private var isIntercept = false
    private var mScrollState = SCROLL_STATE_IDLE
    // 指定当前移动遵循的是哪一个手指
    private var mScrollPointerId = INVALID_POINTER
    private var touchRecyclerView: RecyclerView? = null

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop


    fun setDragRecycleView(recyclerView: RecyclerView) {
        touchRecyclerView = recyclerView
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        Log.d("MotionEvent", "MotionEvent ${event.action}")
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                interceptX = event.x
                interceptY = event.y
                isIntercept = false
            }

            MotionEvent.ACTION_MOVE -> {
                val currX = event.x
                val currY = event.y
                val dx = currX - interceptX
                val dy = currY - interceptY
                isIntercept = abs(dx) > touchSlop && abs(dx) > abs(dy)
                Log.d("ACTION_MOVE", "ACTION_MOVE $isIntercept")
            }

            MotionEvent.ACTION_UP -> {
            }
        }
        return isIntercept || super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d("MotionEvent", "MotionEvent ${event.action}")

        val action = event.actionMasked // 与 getAction() 类似，多点触控需要使用这个方法获取事件类型
        val actionIndex = event.actionIndex // 获取该事件是哪个指针(手指)产生的
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                initX = (event.x + 0.5).toInt()
                initY = (event.y + 0.5).toInt()
                setScrollState(SCROLL_STATE_IDLE)
                mScrollPointerId = event.getPointerId(0)
            }

            MotionEvent.ACTION_POINTER_DOWN -> { // 新落下的手指
//                mScrollPointerId = event.getPointerId(actionIndex)
//                // 重置初始位置为最新落下的手指位置
//                initX = (event.getX(actionIndex) + 0.5).toInt()
            }

            MotionEvent.ACTION_MOVE -> {
                val currX = (event.x + 0.5).toInt()
                val currY = (event.y + 0.5).toInt()
                val dx = currX - initX
                val dy = currY - initY
//                isIntercept = abs(dx) > touchSlop && abs(dx) > abs(dy)

                if (mScrollState != SCROLL_STATE_DRAGGING && abs(dx) > touchSlop) {
                    setScrollState(SCROLL_STATE_DRAGGING)
                }
                if (mScrollState == SCROLL_STATE_DRAGGING) {
                    initX = currX
                    touchRecyclerView?.scrollBy(-dx, 0)
                }
            }

            MotionEvent.ACTION_UP -> {
            }

            MotionEvent.ACTION_POINTER_UP -> {
//                if (mScrollPointerId == event.getPointerId(actionIndex)) {
//                    Log.e("jcy", "ACTION_POINTER_UP")
//                    val newIndex = if (actionIndex === 0) 1 else 0
//                    mScrollPointerId = event.getPointerId(newIndex)
//                    // 重置初始位置为剩下的手指位置
//                    initX = (event.getX(actionIndex) + 0.5).toInt()
//                }
            }
        }

        return super.onTouchEvent(event)
    }

    private fun setScrollState(state: Int) {
        mScrollState = state
    }
}