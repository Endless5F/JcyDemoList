package com.android.kotlinlearn.kotlinlib.coroutine

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.suspendCoroutine

fun main() {

    // 方法一，使用 runBlocking 顶层函数
    runBlocking {

    }

    // 方法二，使用 GlobalScope 单例对象
    //            👇 可以直接调用 launch 开启协程
    GlobalScope.launch {
        val image = withContext(Dispatchers.IO) {  // 👈 切换到 IO 线程，并在执行完成后切回 UI 线程
            // getImage(imageId)                        // 👈 将会运行在 IO 线程
        }
    }

    // 创建一个Job，并用这个Job来管理CoroutineScope的所有子协程
    val job = Job()
    val coroutineContext: CoroutineContext = Dispatchers.Main + job
    // 方法三，自行通过 CoroutineContext 创建一个 CoroutineScope 对象
    //                                    👇 需要一个类型为 CoroutineContext 的参数
    val coroutineScope = CoroutineScope(coroutineContext)
    coroutineScope.launch {
        suspendCoroutine {

        }
    }
    // 结束所有子协程
    job.cancel()
}