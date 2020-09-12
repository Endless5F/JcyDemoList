package main.kotlin.协程

import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Created by Administrator on 2018/4/28.
 * 异步类,线程池
 */
private val pool by lazy {
    Executors.newCachedThreadPool()
}

class AsyncTask(val block:()->Unit){
    fun execute()= pool.execute(block)
}