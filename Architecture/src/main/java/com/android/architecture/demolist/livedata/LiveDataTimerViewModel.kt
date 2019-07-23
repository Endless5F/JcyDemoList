package com.android.architecture.demolist.livedata

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.SystemClock

import java.util.Timer
import java.util.TimerTask

class LiveDataTimerViewModel : ViewModel() {

    // 所有数据都交由LiveData处理
    private val mElapsedTime = MutableLiveData<Long>()

    private val mInitialTime: Long

    val elapsedTime: LiveData<Long>
        get() = mElapsedTime

    public fun getElapsedTime() : MutableLiveData<Long> {
        return mElapsedTime
    }

    init {
        // 返回系统启动到现在的时间
        mInitialTime = SystemClock.elapsedRealtime()
        val timer = Timer()

        // 主线程直接设置
        mElapsedTime.value = 0
        // Update the elapsed time every second.
        // 以固定利率计划：delay时间后开始运行任务。并每隔period时间调用任务一次。
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val newValue = (SystemClock.elapsedRealtime() - mInitialTime) / 1000
                // 子线程使用post设置
                mElapsedTime.postValue(newValue)
            }
        }, ONE_SECOND.toLong(), ONE_SECOND.toLong())
    }

    companion object {
        private val ONE_SECOND = 1000
    }
}