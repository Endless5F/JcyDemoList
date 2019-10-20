package com.android.performanceanalysis.activity

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.support.v4.view.AsyncLayoutInflater
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Choreographer
import com.android.performanceanalysis.R


class LayoutActivity : AppCompatActivity() {

    private var mStartFrameTime: Long = 0

    private var mFrameCount = 0
    private val MONITOR_INTERVAL = 160L //单次计算FPS使用160毫秒
    private val MONITOR_INTERVAL_NANOS = MONITOR_INTERVAL * 1000L * 1000L
    private val MAX_INTERVAL = 1000L //设置计算fps的单位时间间隔1000ms,即fps/s;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout)
        getFPS()
    }

    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun getFPS() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return
        }
        Choreographer.getInstance().postFrameCallback(object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (mStartFrameTime == 0L) {
                    mStartFrameTime = frameTimeNanos
                }
                val interval = frameTimeNanos - mStartFrameTime
                if (interval > MONITOR_INTERVAL_NANOS) {
                    val fps = (mFrameCount.toLong() * 1000L * 1000L).toDouble() / interval * MAX_INTERVAL
                    Log.e("fps", "fps = $fps")
                    mFrameCount = 0
                    mStartFrameTime = 0
                } else {
                    ++mFrameCount
                }

                Choreographer.getInstance().postFrameCallback(this)
            }
        })
    }
}
