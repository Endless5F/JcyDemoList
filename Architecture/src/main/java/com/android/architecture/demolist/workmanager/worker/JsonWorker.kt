package com.android.architecture.demolist.workmanager.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.architecture.demolist.workmanager.model.ShoesBean
import com.android.architecture.ext.logd
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader

class JsonWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    private val TAG by lazy {
        JsonWorker::class.java.simpleName
    }

    // 指定Dispatchers（调度）线程
    override val coroutineContext: CoroutineDispatcher
        get() = Dispatchers.IO

    override suspend fun doWork(): Result = coroutineScope {
        try {
            applicationContext.assets.open("shoes.json").use {
                val shoeList: ShoesBean = Gson().fromJson(BufferedReader(InputStreamReader(it) as Reader?), ShoesBean::class.java)

                shoeList.toString().logd("JsonWorker")

                Result.success()
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error seeding database", ex)
            Result.failure()
        }
    }
}
