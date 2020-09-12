package com.android.architecture.demolist.workmanager.model

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import androidx.work.*
import com.android.architecture.constant.Constant.IMAGE_MANIPULATION_WORK_NAME
import com.android.architecture.constant.Constant.KEY_IMAGE_URI
import com.android.architecture.constant.Constant.TAG_OUTPUT
import com.android.architecture.demolist.workmanager.worker.*

/**
 * 选取一张图片，将图片做模糊处理，之后显示在我们的头像
 * */
class WorkManagerModel() : ViewModel() {
    internal var imageUri: Uri? = null
    internal val outPutWorkInfos: LiveData<List<WorkInfo>>
    private val workManager = WorkManager.getInstance()

    init {
        // 通过Tag获取观测的后台工作
        outPutWorkInfos = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT)
    }

    /**
     * 设置需要处理的图片的Uri
     */
    internal fun setImageUri(uri: String?) {
        imageUri = uriOrNull(uri)
    }

    /**
     * uri是否为空
     * */
    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else
            null
    }

    internal fun applyBlur(blurLevel: Int) {
        var continuation = workManager
                .beginUniqueWork(
                        IMAGE_MANIPULATION_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        OneTimeWorkRequest.from(CleanUpWorker::class.java)
                )

        for (i in 0 until blurLevel) {
            val builder = OneTimeWorkRequestBuilder<BlurWorker>()
            if (i == 0) {
                builder.setInputData(createInputDataForUri())
            }
            continuation = continuation.then(builder.build())
        }

        // 构建约束条件
        val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true) // 非电池低电量
                .setRequiredNetworkType(NetworkType.CONNECTED) // 网络连接的情况
                .setRequiresStorageNotLow(true) // 存储空间足
                .build()

        // 储存照片
        val save = OneTimeWorkRequestBuilder<SaveImageToFileWorker>()
                .setConstraints(constraints)
                .addTag(TAG_OUTPUT) // 添加Tag
                .build()
        continuation = continuation.then(save)

        // 需要注意的是队列里面任务（还在等待调度 未执行的那种）不能超过100个，不然会crash，这是workmanager代码的限制
        continuation.enqueue()
    }


    private fun createInputDataForUri(): Data {
        val builder = Data.Builder()
        imageUri?.let {
            builder.putString(KEY_IMAGE_URI, imageUri.toString())
        }
        return builder.build()
    }

    /**
     * 清除缓存的照片
     * */
    internal fun clearTempFile() {
        // 执行一个任务
        val request = OneTimeWorkRequest.from(CleanUpWorker::class.java)
        workManager.enqueue(request)
    }

    /**
     * 取消任务
     * */
    fun cancelWork() {
        // 可根据Tag取消任务
//        workManager.cancelAllWorkByTag("")
        workManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)
    }

    //----------------------------------------WorkManager 细致讲解---------------------------------------------

    /**
     * 执行一个任务的用法，Worker详细Api说明请参考 WorkerA
     * */
    private fun executeOneTask() {
        val request = OneTimeWorkRequest.from(WorkerA::class.java)
        workManager.enqueue(request)
    }

    /**
     * WorkManager 执行多任务的用法1 -- 多任务顺序执行
     * */
    private fun executeMultitasking1() {
        workManager.beginWith(OneTimeWorkRequest.from(WorkerA::class.java)) // 不传参
                // 传参
                .then(OneTimeWorkRequestBuilder<WorkerB>().setInputData(Data.Builder().putString("我是key", "我是值").build()).build())
                .then(OneTimeWorkRequestBuilder<WorkerC>().build())
                .enqueue()
    }

    /**
     * WorkManager 执行多任务的用法2 -- 多任务设置唯一工作序列
     *  唯一的工作序列，WorkManager一次只允许一个工作序列使用该名称，当我们创建一个新的唯一工作序列时，
     *      如果已经有一个未完成的序列具有相同的名称，则根据我们设置的“任务相同的执行策略”来处理
     * */
    private fun executeMultitasking2() {
        workManager.beginUniqueWork(
                IMAGE_MANIPULATION_WORK_NAME, // 任务名称
                /**
                 * 任务相同的执行策略：分为REPLACE，KEEP，APPEND
                 *  ExistingWorkPolicy.REPLACE：如果存在具有相同唯一名称的待处理（未完成）工作，则取消并删除它。然后，插入新指定的工作。
                 *  ExistingWorkPolicy.KEEP：如果存在具有相同唯一名称的待处理（未完成）工作，则不执行任何操作。 否则，插入新指定的工作。
                 *  ExistingWorkPolicy.APPEND：如果存在具有相同唯一名称的待处理（未完成）工作，则将*新指定的工作追加为该工作序列的所有叶子的子项。否则，插入新指定的工作作为新序列的开始。
                 * */
                ExistingWorkPolicy.REPLACE,
                mutableListOf(
                        OneTimeWorkRequest.from(WorkerA::class.java)
                ))
                .then(OneTimeWorkRequestBuilder<WorkerB>().setInputData(createInputDataForUri()).build())
                .then(OneTimeWorkRequestBuilder<WorkerC>().build())
                .enqueue()
    }

    /**
     * WorkManager 执行多任务的用法3 -- 多任务有几个同时执行，剩下的顺序执行
     * 说明：beginWith函数里面的workA, workB, workC三个任务是平行(同时)执行的，
     *  而且要等workA, workB, workC都执行完才能做下一步then里的任务。then(workD)里面的workD
     *  但是一定要等到workD执行完才能执行下一步的then里面的任务。
     * */
    private fun executeMultitasking3() {
        workManager.beginWith(
                mutableListOf(
                        OneTimeWorkRequest.from(WorkerA::class.java),
                        OneTimeWorkRequest.from(WorkerB::class.java),
                        OneTimeWorkRequest.from(WorkerC::class.java)
                ))
                .then(OneTimeWorkRequestBuilder<WorkerD>().setInputData(createInputDataForUri()).build())
                .then(OneTimeWorkRequestBuilder<WorkerE>().build())
                .enqueue()
    }

    /**
     * WorkManager 执行多任务的用法4 -- 多任务复制执行顺序（组合任务：使用任务链）
     * 说明：任务A B 是同时执行的，执行完A B 才会继续 同时执行C D，执行完C D最后才会执行任务 E
     * */
    @SuppressLint("EnqueueWork")
    private fun executeMultitasking4() {
        val requestA = OneTimeWorkRequest.Builder(WorkerA::class.java).build()
        val requestB = OneTimeWorkRequest.Builder(WorkerB::class.java).build()
        val requestC = OneTimeWorkRequest.Builder(WorkerC::class.java).build()
        val requestD = OneTimeWorkRequest.Builder(WorkerD::class.java).build()
        val requestE = OneTimeWorkRequest.Builder(WorkerE::class.java).build()
        //A,B任务链
        val continuationAB = WorkManager.getInstance().beginWith(requestA).then(requestB)
        //C,D任务链
        val continuationCD = WorkManager.getInstance().beginWith(requestC).then(requestD)
        //合并上面两个任务链，在接入requestE任务，入队执行
        WorkContinuation.combine(mutableListOf(continuationAB, continuationCD)).then(requestE).enqueue()
    }

    /**
     * Worker设置执行的约束条件，来构建请求任务
     * */
    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(Build.VERSION_CODES.M)
    private fun setConstraintsBuilderRequest() {
        val requestA = OneTimeWorkRequest.Builder(WorkerA::class.java).build();
        val requestB = OneTimeWorkRequest.Builder(WorkerB::class.java).build();
        /**
         * WorkContinuation 任务链（工作链）：
         *  beginWith方法和then方法返回值均为 WorkContinuation
         * 说明：可通过一个任务链或者多个任务链 任意组合，来达到上面讲解的多任务操作
         * */
        val continuation = WorkManager.getInstance().beginWith(requestA).then(requestB)

        // 构建约束条件
        val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true) // 非电池低电量
                /**
                 * 指定网络状态执行任务
                 * NetworkType.NOT_REQUIRED：对网络没有要求
                 * NetworkType.CONNECTED：网络连接的时候执行
                 * NetworkType.UNMETERED：不计费的网络比如WIFI下执行
                 * NetworkType.NOT_ROAMING：非漫游网络状态
                 * NetworkType.METERED：计费网络比如3G，4G下执行。
                 */
                .setRequiredNetworkType(NetworkType.CONNECTED) // 网络连接的情况
                .setRequiresStorageNotLow(true) // 存储空间足
                .setRequiresDeviceIdle(true) // 是否在设备空闲的时候执行
//                .addContentUriTrigger(Uri.parse(""), true) // 当Uri有更新的时候是否执行任务
                .build()

        val workerC = OneTimeWorkRequestBuilder<WorkerC>()
                .setConstraints(constraints)
                .addTag("设置任务Tag，可根据Tag使用workManager.cancelAllWorkByTag(\"Tag\")取消任务")
                .build()

        continuation.then(workerC).enqueue()

    }

}