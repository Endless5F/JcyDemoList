package com.android.performanceanalysis.activity

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Choreographer
import androidx.appcompat.app.AppCompatActivity
import com.android.performanceanalysis.R

/**
 *
 * Android系统每隔16ms重绘UI界面，16ms是因为Android系统规定UI绘图的刷新频率60FPS。Android系统每隔16ms，发送一个系统级别信号VSYNC唤起重绘操作。1秒内绘制UI界面60次。每16ms为一个UI界面绘制周期。
 * 平常所说的“丢帧”情况，并不是真的把绘图的帧给“丢失”了，也而是UI绘图的操作没有和系统16ms的绘图更新频率步调一致，开发者代码在绘图中绘制操作太多，导致操作的时间超过16ms
 * ，在Android系统需要在16ms时需要重绘的时刻由于UI线程被阻塞而绘制失败。如果丢的帧数量是一两帧，用户在视觉上没有明显感觉，但是如果超过3帧，用户就有视觉上的感知。
 * 丢帧数如果再持续增多，在视觉上就是所谓的“卡顿”。丢帧是引起卡顿的重要原因。在Android中可以通过Choreographer检测Android系统的丢帧情况。
 * 如果我们在这个16ms间隔内，没有准备好画面（view），那么这一次绘制，就不会展示在屏幕上，就相当于少绘制了一帧，画面就会出现卡顿，断断续续。
 * Choreographer周期性的在UI重绘时候触发，在代码中记录上一次和下一次绘制的时间间隔，如果超过16ms，就意味着一次UI线程重绘的“丢帧”。丢帧的数量为间隔时间除以16，如果超过3，就开始有卡顿的感知。
 */
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
