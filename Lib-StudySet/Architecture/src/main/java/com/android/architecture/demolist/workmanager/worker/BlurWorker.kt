package com.android.architecture.demolist.workmanager.worker

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.android.architecture.constant.Constant.KEY_IMAGE_URI
import com.android.architecture.demolist.workmanager.util.blurBitmap
import com.android.architecture.demolist.workmanager.util.makeStatusNotification
import com.android.architecture.demolist.workmanager.util.writeBitmapToFile

/**
 * 模糊处理的Worker
 */
class BlurWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private var TAG:String = this::class.java.simpleName

    override fun doWork(): Result {
        val context = applicationContext
        val resourceUri = inputData.getString(KEY_IMAGE_URI)

        // 通知开始处理图片
        makeStatusNotification("Blurring image", context)

        return try {
            // 图片处理逻辑
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri")
                throw IllegalArgumentException("Invalid input uri") as Throwable
            }

            val resolver = context.contentResolver
            val picture = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))
            // 创建Bitmap文件
            val output = blurBitmap(picture, context)
            // 存入路径
            val outputUri = writeBitmapToFile(context, output)

            // 输出路径
            val outPutData = workDataOf(KEY_IMAGE_URI to outputUri.toString())
            makeStatusNotification("Output is $outputUri", context)
            Result.success(outPutData)
        }catch (throwable: Throwable){
            Log.e(TAG, "Error applying blur", throwable)
            Result.failure()
        }
    }
}