package com.android.customwidget.kotlin.widget

import android.content.Context
import android.graphics.Rect
import android.support.v4.view.MotionEventCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.widget.LinearLayout
import android.view.ViewConfiguration
import android.view.animation.Interpolator
import android.widget.OverScroller
import org.jetbrains.anko.find
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

        //f(x) = (x-1)^5 + 1 RecycleView源码中处理惯性滑动的插值器
        private val sQuinticInterpolator = Interpolator { t ->
            var t = t
            t -= 1.0f
            t * t * t * t * t + 1.0f
        }
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

    private val mViewFlinger = ViewFlinger()
    private var velocityTracker : VelocityTracker? = null

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val mMaximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
    private val mMinimumVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();




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

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }

        var eventAddedToVelocityTracker = false
        val vtev = MotionEvent.obtain(event)
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
                var dx = currX - initX
                val dy = currY - initY
//                isIntercept = abs(dx) > touchSlop && abs(dx) > abs(dy)
                if (mScrollState != SCROLL_STATE_DRAGGING && abs(dx) > touchSlop) {
                    setScrollState(SCROLL_STATE_DRAGGING)
                }
                Log.d("MotionEvent", "MotionEvent currX== $currX")
                if (mScrollState == SCROLL_STATE_DRAGGING/* && isScroll()*/) {
                    initX = currX
                    touchRecyclerView?.scrollBy(-dx, 0)
                }
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
            MotionEvent.ACTION_UP -> {
                velocityTracker?.addMovement(vtev)
                eventAddedToVelocityTracker = true
                // 计算当前速度， 1000表示每秒像素数等
                velocityTracker?.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                // 获取横向速度
                var xVelocity = velocityTracker?.getXVelocity()?.toInt()

                if (xVelocity == null || abs(xVelocity) <= mMinimumVelocity) {
                    xVelocity = 0
                }
                if (xVelocity != 0) {
                    mViewFlinger.fling(xVelocity.toInt())
                } else {
                    setScrollState(CustomScrollView.SCROLL_STATE_IDLE)
                }
                resetTouch()
            }
            MotionEvent.ACTION_CANCEL -> {
                resetTouch()
            }
        }

        if (!eventAddedToVelocityTracker) {
            velocityTracker!!.addMovement(vtev)
        }
        vtev.recycle()

        return true
    }


    private fun resetTouch() {
        if (velocityTracker != null) {
            velocityTracker!!.clear()
        }
    }

    private fun setScrollState(state: Int) {
        if (state == mScrollState) {
            return
        }
        mScrollState = state
        if (state != CustomScrollView.SCROLL_STATE_SETTLING) {
            mViewFlinger.stop()
        }
    }

    private inner class ViewFlinger : Runnable {

        private var mLastFlingX = 0
        private val mScroller: OverScroller
        private var mEatRunOnAnimationRequest = false
        private var mReSchedulePostAnimationCallback = false

        init {
            mScroller = OverScroller(context, sQuinticInterpolator)
        }

        override fun run() {
            disableRunOnAnimationRequests()
            val scroller = mScroller
            /**
             * 官方解释：当您想知道新位置时，请调用此方法。如果返回true，则动画尚未完成。
             * 个人解释：该方法配合mScroller.fling方法保存的需要滑动的距离和位置，
             *  通过Runnale不断轮询，根据滑动动画的插值器sQuinticInterpolator，获取每次轮询需要移动的距离，来返回当前的位置
             * */
            if (scroller.computeScrollOffset()) {
                val x = scroller.currX
                val dx = x - mLastFlingX
                mLastFlingX = x
                touchRecyclerView?.scrollBy(-dx, 0)
                postOnAnimation()
            }
            enableRunOnAnimationRequests()
        }

        fun fling(velocityX: Int) {
            mLastFlingX = 0
            setScrollState(CustomScrollView.SCROLL_STATE_SETTLING)
            // 官方解释：基于投掷手势开始滚动。行进的距离将取决于投掷的初始速度。
            // 个人理解：基于开始位置，以及横/纵瞬时速度，计算出横/纵最终滑动的距离以及位置并保存
            mScroller.fling(0, 0, velocityX, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)
            postOnAnimation()
        }

        fun stop() {
            removeCallbacks(this)
            // 停止动画
            mScroller.abortAnimation()
        }

        private fun disableRunOnAnimationRequests() {
            mReSchedulePostAnimationCallback = false
            mEatRunOnAnimationRequest = true
        }

        private fun enableRunOnAnimationRequests() {
            mEatRunOnAnimationRequest = false
            if (mReSchedulePostAnimationCallback) {
                postOnAnimation()
            }
        }

        internal fun postOnAnimation() {
            if (mEatRunOnAnimationRequest) {
                mReSchedulePostAnimationCallback = true
            } else {
                removeCallbacks(this)
                ViewCompat.postOnAnimation(this@ThouchLinearLayout, this)
            }
        }
    }

    /**
     * 是否可以滑动：主要用于显示此时 时间轴处于初始位置，并且往左滑动时，应取消
     * （即时间轴处于初始位置时向左滑动是不可以的） 同理：若时间轴处于结尾位置也是不可以向右滑动的
     * @param direction 滑动的方向， direction > 0 为左滑  < 0 为右滑  -- 此值不准确
     * @return 是否可以滑动
     * @
     * */
    @Deprecated("由于Move过程中无法正确判断是否左滑还是右滑，导致此方法无法正确判断是否滑动")
    private fun isScroll(direction : Int) : Boolean{
        var isScroll = true
        val touchManager = touchRecyclerView?.layoutManager as LinearLayoutManager
        val positionFirst = touchManager?.findFirstCompletelyVisibleItemPosition()
        val positionLast = touchManager?.findLastCompletelyVisibleItemPosition()
        if (direction < 0 && positionFirst != null && positionFirst == 0) isScroll = false
        if (direction > 0 && positionLast != null && positionLast == 49) isScroll = false
        return isScroll
    }

}