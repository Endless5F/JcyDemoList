package main.kotlin.协程

import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext

/**
 * Created by Administrator on 2018/4/28.
 * Continuation 延续
 */
class ContextContinuation(override val context:CoroutineContext
                          = EmptyCoroutineContext): Continuation<Unit> {
    override fun resume(value: Unit) {

    }
    override fun resumeWithException(exception: Throwable) {

    }
}