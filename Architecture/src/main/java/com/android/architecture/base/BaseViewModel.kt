//package com.android.architecture.base
//
//import android.arch.lifecycle.LifecycleObserver
//import android.arch.lifecycle.MutableLiveData
//import android.arch.lifecycle.ViewModel
//import kotlinx.coroutines.*
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.experimental.CoroutineScope
//import java.util.concurrent.CancellationException
//
///**
// * Created by luyao
// * on 2019/5/31 16:06
// */
//open class BaseViewModel : ViewModel(), LifecycleObserver {
//
//    private val mException: MutableLiveData<Exception> = MutableLiveData()
//
//
//    private fun launchOnUI(block: suspend CoroutineScope.() -> Unit) {
//
//        viewModelScope.launch { block() }
//
//    }
//
//    suspend fun <T> launchIO(block: suspend CoroutineScope.() -> T) {
//        withContext(Dispatchers.IO) {
//            block
//        }
//    }
//
//    fun launch(tryBlock: suspend CoroutineScope.() -> Unit) {
//        launchOnUI {
//            tryCatch(tryBlock, {}, {}, true)
//        }
//    }
//
//
//    fun launchOnUITryCatch(tryBlock: suspend CoroutineScope.() -> Unit,
//                           catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
//                           finallyBlock: suspend CoroutineScope.() -> Unit,
//                           handleCancellationExceptionManually: Boolean
//    ) {
//        launchOnUI {
//            tryCatch(tryBlock, catchBlock, finallyBlock, handleCancellationExceptionManually)
//        }
//    }
//
//    fun launchOnUITryCatch(tryBlock: suspend CoroutineScope.() -> Unit,
//                           handleCancellationExceptionManually: Boolean = false
//    ) {
//        launchOnUI {
//            tryCatch(tryBlock, {}, {}, handleCancellationExceptionManually)
//        }
//    }
//
//
//    private suspend fun tryCatch(
//        tryBlock: suspend CoroutineScope.() -> Unit,
//        catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
//        finallyBlock: suspend CoroutineScope.() -> Unit,
//        handleCancellationExceptionManually: Boolean = false) {
//        coroutineScope {
//            try {
//                tryBlock()
//            } catch (e: Exception) {
//                if (e !is CancellationException || handleCancellationExceptionManually) {
//                    mException.value = e
//                    catchBlock(e)
//                } else {
//                    throw e
//                }
//            } finally {
//                finallyBlock()
//            }
//        }
//    }
//}