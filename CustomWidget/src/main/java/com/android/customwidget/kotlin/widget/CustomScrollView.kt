package com.android.customwidget.kotlin.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import androidx.core.view.MotionEventCompat
import androidx.core.view.VelocityTrackerCompat
import androidx.core.view.ViewCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.OverScroller
import android.widget.TextView

/**
 * Created by zhufeng on 2017/7/26.
 * https://www.jianshu.com/p/57ce979b23e8
 */

class CustomScrollView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ViewGroup(context, attrs, defStyleAttr) {
    private var mContext: Context? = null
    private var SCREEN_WIDTH = 0
    private var SCREEN_HEIGHT = 0
    private var mWidth = 0
    private var mHeight = 0

    private var mScrollState = SCROLL_STATE_IDLE
    private var mScrollPointerId = INVALID_POINTER
    private var mVelocityTracker: VelocityTracker? = null
    private var mLastTouchY: Int = 0
    private var mTouchSlop: Int = 0
    private var mMinFlingVelocity: Int = 0
    private var mMaxFlingVelocity: Int = 0
    private val mViewFlinger = ViewFlinger()

    init {

        init(context)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var top = 0
        for (i in 0..19) {
            val width = SCREEN_WIDTH
            val height = SCREEN_HEIGHT / 2
            val left = 0
            val right = left + width
            val bottom = top + height

            //撑大边界
            if (bottom > mHeight) {
                mHeight = bottom
            }
            if (right > mWidth) {
                mWidth = right
            }

            val textView = TextView(mContext)
            if (i % 2 == 0) {
                textView.setBackgroundColor(Color.CYAN)
            } else {
                textView.setBackgroundColor(Color.GREEN)
            }
            textView.text = "item:$i"
            addView(textView)
            textView.layout(left, top, right, bottom)
            top += height
            top += 20
        }
    }

    private fun init(context: Context) {
        this.mContext = context
        val vc = ViewConfiguration.get(context)
        mTouchSlop = vc.scaledTouchSlop
        mMinFlingVelocity = vc.scaledMinimumFlingVelocity
        mMaxFlingVelocity = vc.scaledMaximumFlingVelocity
        val metric = context.resources.displayMetrics
        SCREEN_WIDTH = metric.widthPixels
        SCREEN_HEIGHT = metric.heightPixels
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        var eventAddedToVelocityTracker = false
        val action = MotionEventCompat.getActionMasked(event)
        val actionIndex = MotionEventCompat.getActionIndex(event)
        val vtev = MotionEvent.obtain(event)

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                setScrollState(SCROLL_STATE_IDLE)
                mScrollPointerId = event.getPointerId(0)
                mLastTouchY = (event.y + 0.5f).toInt()
            }
            MotionEventCompat.ACTION_POINTER_DOWN -> {
                mScrollPointerId = event.getPointerId(actionIndex)
                mLastTouchY = (event.getY(actionIndex) + 0.5f).toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                val index = event.findPointerIndex(mScrollPointerId)
                if (index < 0) {
                    Log.e("zhufeng", "Error processing scroll; pointer index for id $mScrollPointerId not found. Did any MotionEvents get skipped?")
                    return false
                }

                val y = (event.getY(index) + 0.5f).toInt()
                var dy = mLastTouchY - y

                if (mScrollState != SCROLL_STATE_DRAGGING) {
                    var startScroll = false

                    if (Math.abs(dy) > mTouchSlop) {
                        if (dy > 0) {
                            dy -= mTouchSlop
                        } else {
                            dy += mTouchSlop
                        }
                        startScroll = true
                    }
                    if (startScroll) {
                        setScrollState(SCROLL_STATE_DRAGGING)
                    }
                }

                if (mScrollState == SCROLL_STATE_DRAGGING) {
                    mLastTouchY = y
                    constrainScrollBy(0, dy)
                }
            }
            MotionEventCompat.ACTION_POINTER_UP -> {
                if (event.getPointerId(actionIndex) == mScrollPointerId) {
                    // Pick a new pointer to pick up the slack.
                    val newIndex = if (actionIndex == 0) 1 else 0
                    mScrollPointerId = event.getPointerId(newIndex)
                    mLastTouchY = (event.getY(newIndex) + 0.5f).toInt()
                }
            }
            MotionEvent.ACTION_UP -> {
                mVelocityTracker!!.addMovement(vtev)
                eventAddedToVelocityTracker = true
                mVelocityTracker!!.computeCurrentVelocity(1000, mMaxFlingVelocity.toFloat())
                var yVelocity = -VelocityTrackerCompat.getYVelocity(mVelocityTracker!!, mScrollPointerId)
                Log.i("zhufeng", "速度取值：$yVelocity")
                if (Math.abs(yVelocity) < mMinFlingVelocity) {
                    yVelocity = 0f
                } else {
                    yVelocity = Math.max((-mMaxFlingVelocity).toFloat(), Math.min(yVelocity, mMaxFlingVelocity.toFloat()))
                }
                if (yVelocity != 0f) {
                    mViewFlinger.fling(yVelocity.toInt())
                } else {
                    setScrollState(SCROLL_STATE_IDLE)
                }
                resetTouch()
            }
            MotionEvent.ACTION_CANCEL -> {
                resetTouch()
            }
        }
        if (!eventAddedToVelocityTracker) {
            mVelocityTracker!!.addMovement(vtev)
        }
        vtev.recycle()
        return true

    }

    private fun resetTouch() {
        if (mVelocityTracker != null) {
            mVelocityTracker!!.clear()
        }
    }

    private fun setScrollState(state: Int) {
        if (state == mScrollState) {
            return
        }
        mScrollState = state
        if (state != SCROLL_STATE_SETTLING) {
            mViewFlinger.stop()
        }
    }

    private inner class ViewFlinger : Runnable {

        private var mLastFlingY = 0
        private val mScroller: OverScroller
        private var mEatRunOnAnimationRequest = false
        private var mReSchedulePostAnimationCallback = false

        init {
            mScroller = OverScroller(context, sQuinticInterpolator)
        }

        override fun run() {
            disableRunOnAnimationRequests()
            val scroller = mScroller
            if (scroller.computeScrollOffset()) {
                val y = scroller.currY
                val dy = y - mLastFlingY
                mLastFlingY = y
                constrainScrollBy(0, dy)
                postOnAnimation()
            }
            enableRunOnAnimationRequests()
        }

        fun fling(velocityY: Int) {
            mLastFlingY = 0
            setScrollState(SCROLL_STATE_SETTLING)
            mScroller.fling(0, 0, 0, velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)
            postOnAnimation()
        }

        fun stop() {
            removeCallbacks(this)
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
                ViewCompat.postOnAnimation(this@CustomScrollView, this)
            }
        }
    }

    private fun constrainScrollBy(dx: Int, dy: Int) {
        var dx = dx
        var dy = dy
        val viewport = Rect()
        getGlobalVisibleRect(viewport)
        val height = viewport.height()
        val width = viewport.width()

        val scrollX = scrollX
        val scrollY = scrollY

        //右边界
        if (mWidth - scrollX - dx < width) {
            dx = mWidth - scrollX - width
        }
        //左边界
        if (-scrollX - dx > 0) {
            dx = -scrollX
        }
        //下边界
        if (mHeight - scrollY - dy < height) {
            dy = mHeight - scrollY - height
        }
        //上边界
        if (scrollY + dy < 0) {
            dy = -scrollY
        }
        scrollBy(dx, dy)
    }

    companion object {
        private val INVALID_POINTER = -1
        val SCROLL_STATE_IDLE = 0
        val SCROLL_STATE_DRAGGING = 1
        val SCROLL_STATE_SETTLING = 2

        //f(x) = (x-1)^5 + 1
        private val sQuinticInterpolator = Interpolator { t ->
            var t = t
            t -= 1.0f
            t * t * t * t * t + 1.0f
        }
    }
}
