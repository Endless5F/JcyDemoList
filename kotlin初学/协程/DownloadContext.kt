package main.kotlin.协程

import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Administrator on 2018/4/28.
 * 为了携带数据,url
 */
class DownloadContext(val url:String):AbstractCoroutineContextElement(Key){
    companion object Key:CoroutineContext.Key<DownloadContext>
}