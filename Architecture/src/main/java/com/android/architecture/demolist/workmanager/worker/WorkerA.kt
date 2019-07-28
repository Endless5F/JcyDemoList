package com.android.architecture.demolist.workmanager.worker

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * 讲解示例
 * 谷歌提供了四种Worker给我们使用，分别为：
 *  自动运行在后台线程的Worker、结合协程的CoroutineWorker、结合RxJava2的RxWorker和以上三个类的基类的ListenableWorker。
 *
 * 由于本文使用的是kotlin，因此简单介绍使用一下 CoroutineWorker ，请参考 JsonWorker
 */
class WorkerA(context: Context, params: WorkerParameters) : Worker(context, params) {

    /**
     * 任务逻辑
     * @return 任务的执行情况，成功，失败，还是需要重新执行
     */
    override fun doWork(): Result {
        /**
         * 任务的输入数据，有的时候可能需要我们传递参数进去，比如下载文件我们需要传递文件路径进去，
         * 在doWork()函数中通过getInputData()/kotlin中(inputData) 获取到我们传递进来的参数
         */
        inputData.getString("我是Key")

        // 是否有返回数据，默认没有
        val isResultData = false
        return if (isResultData) {
            Result.success()
        } else {
            /**
             * 设置我们任务输出结果，输出结果回调请参考 WorkManagerActivity 中的 taskObservation()方法
             * */
            Result.success(Data.Builder().putString("我是key","我是值").build())
        }

    }
}