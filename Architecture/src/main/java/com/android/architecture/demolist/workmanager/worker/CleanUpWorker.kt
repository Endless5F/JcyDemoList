package com.android.architecture.demolist.workmanager.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.architecture.constant.Constant.OUTPUT_PATH
import com.android.architecture.demolist.workmanager.util.makeStatusNotification
import java.io.File


/**
 * 清理临时文件的Worker
 */
class CleanUpWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    private val TAG by lazy {
        this::class.java.simpleName
    }

    override fun doWork(): Result {
        // 在工作开始时发出通知并减慢工作速度以便即使在模拟设备上也更容易看到每个WorkRequest启动
        makeStatusNotification("Cleaning up old temporary files", applicationContext)
        //sleep()

        return try {
            // 删除逻辑
            val outputDirectory = File(applicationContext.filesDir, OUTPUT_PATH)
            if (outputDirectory.exists()) {
                val entries = outputDirectory.listFiles()
                if (entries != null) {
                    for (entry in entries) {
                        val name = entry.name
                        if (name.isNotEmpty() && name.endsWith(".png")) {
                            val deleted = entry.delete()
                            Log.i(TAG, String.format("Deleted %s - %s", name, deleted))
                        }
                    }
                }
            }
            // 成功时返回
            Result.success()
        } catch (exception: Exception) {
            Log.e(TAG, "Error cleaning up", exception)
            // 失败时返回
            Result.failure()
        }
    }
}