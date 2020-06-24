package com.android.customwidget.kotlin.ext

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.jetbrains.anko.doAsync
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory

/**
 * 后台工作调度工具类
 */

/**
 * 默认串行执行队列
 */
private val backgroundSerialExecutor by lazy { Executors.newSingleThreadExecutor(TaskThreadFactory()) }
/**
 * 主线程Handler
 */
private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

/**
 * 加入到异步线程串行工作队列中，并执行（支持delay）
 *
 * @param task Kotlin闭包
 * @param delayed 延迟时间，默认0ms
 */
fun dispatchSerialWork(task: (() -> Unit), delayed: Long = 0): CommonTask {
    val commonTask = CommonTask()
    val runnable = dispatchMainLoopWork({
        val future = backgroundSerialExecutor.submit {
            task()
        }
        commonTask.future = future
    }, delayed).runnable
    commonTask.runnable = runnable
    return commonTask
}

/**
 * 异步线程串行执行（立即执行，不delay）
 *
 * @param task Kotlin闭包
 */
fun dispatchSerialWork(task: (() -> Unit)): CommonTask {
    val future = backgroundSerialExecutor.submit {
        task()
    }
    return CommonTask(future = future)
}

/**
 * 并行执行任务（使用Kotlin协程）
 *
 * @param task Kotlin闭包
 */
fun Context.dispatchConcurrentWork(task: (() -> Unit)): CommonTask {
    val future = this.doAsync {
        task()
    }
    return CommonTask(future = future)
}

/**
 * 加入到主线程中，并执行（支持Delay）
 *
 * @param task Kotlin闭包
 * @param delayed 延迟时间，默认0ms
 */
fun dispatchMainLoopWork(task: (() -> Unit), delayed: Long = 0): CommonTask {
    val runnable = Runnable {
        task()
    }
    mainHandler.postDelayed(runnable, delayed)
    return CommonTask(runnable = runnable)
}

/**
 * 加入到主线程中，并立即执行（不支持Delay和取消）
 *
 * @param task Kotlin闭包
 */
fun dispatchMainLoopWork(task: (() -> Unit)) {
    mainHandler.postDelayed(task, 0L)
}

/**
 * 取消任务
 *
 * @param task 由以上几个方法返回的CommonTask对象
 * @param force 是否强行取消，默认false
 */
fun cancelTask(task: CommonTask?, force: Boolean = false) {
    task ?: return
    task.future?.cancel(force)
    task.runnable?.let {
        mainHandler.removeCallbacks(it)
    }
}

private const val WORK_THREAD_NAME = "异步工作队列"

private class TaskThreadFactory : ThreadFactory {

    override fun newThread(r: Runnable): Thread {
        return Thread(r, WORK_THREAD_NAME)
    }
}

data class CommonTask(
        var runnable: Runnable? = null,
        var future: Future<*>? = null
)