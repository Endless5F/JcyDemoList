package main.kotlin.协程

import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.ContinuationInterceptor

/**
 * Created by Administrator on 2018/4/28.
 * 篡改continuation,返回自己的UiContinuationWrapper
 * 并给其它continuation篡改的机会
 */
class AsynContext:AbstractCoroutineContextElement(ContinuationInterceptor),ContinuationInterceptor{
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return UiContinuationWrapper(continuation.context.fold(continuation){
            continuation, element ->
            if (element != this && element is ContinuationInterceptor){
                element.interceptContinuation(continuation)
            }else continuation
        })
    }

}