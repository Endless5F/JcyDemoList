package com.android.architecture.demolist.workmanager.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * 讲解示例
 */
class WorkerB(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        return Result.success()
    }
}