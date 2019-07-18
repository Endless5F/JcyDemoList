package main.kotlin.协程

import javax.swing.SwingUtilities
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext


/**
 * Created by Administrator on 2018/4/28.
 * ui协程包装类,用来切换线程到ui线程
 */
class UiContinuationWrapper<T>(val continuation: Continuation<T>):Continuation<T>{
    override val context=continuation.context

    override fun resume(value: T) {
        SwingUtilities.invokeLater { continuation.resume(value) }
    }

    override fun resumeWithException(exception: Throwable) {
        SwingUtilities.invokeLater { continuation.resumeWithException(exception) }
    }

}

