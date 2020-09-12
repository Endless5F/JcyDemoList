package main.kotlin.协程

import kotlin.coroutines.experimental.*

/**
 * Created by Administrator on 2018/4/28.
 */
fun 我要开始协程啦(context: CoroutineContext=EmptyCoroutineContext, block:suspend ()->Unit) {
    block.startCoroutine(ContextContinuation(context+AsynContext()))
}

suspend fun <T> 我要开始耗时操作了(block: CoroutineContext.() -> T)= suspendCoroutine<T>{
    //执行耗时操作的地方
    continuation ->
    AsyncTask{
        try {
            continuation.resume(block(continuation.context))
        }catch (e:Exception){
            continuation.resumeWithException(e)
        }
    }.execute()
}

fun 我要开始加载图片啦(url:String):ByteArray {
    //联网逻辑代码
    /*val responseBody=HttpService.service.getLogo(url).execute()
    if (responseBody.isSuccessful) {
        responseBody.body()?.byteStream()?.readBytes()?.let{
            return it
        }
        throw HttpException(HttpError.HTTP_ERROR_UNKNOWN)
    }else{
        throw HttpException(responseBody.code())
    }*/
    return "防止报错,使用时删除此行".toByteArray()
}