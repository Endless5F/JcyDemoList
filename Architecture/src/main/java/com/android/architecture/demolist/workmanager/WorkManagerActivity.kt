package com.android.architecture.demolist.workmanager

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.architecture.R
import com.android.architecture.constant.Constant.KEY_IMAGE_URI
import com.android.architecture.demolist.workmanager.model.WorkManagerModel
import com.android.architecture.toast
import kotlinx.android.synthetic.main.activity_work_manager.*


/**
 * WorkManger中重要的几个类：
 *  Worker：需要继承Worker，并复写doWork()方法，在doWork()方法中放入你需要在后台执行的代码。
 *  WorkRequest：指后台工作的请求，你可以在后台工作的请求中添加约束条件
 *      WorkRequest可以分为两类：（详情请看WorkManagerModel）
 *          PeriodicWorkRequest：多次、定时执行的任务请求，不支持任务链
 *          OneTimeWorkRequest：只执行一次的任务请求，支持任务链
 *  WorkManager：真正让Worker在后台执行的类
 * */

class WorkManagerActivity : AppCompatActivity() {

    private val TAG by lazy { WorkManagerActivity::class.java.simpleName }

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(WorkManagerModel::class.java)
    }

    // 选择图片的标识
    private val REQUEST_CODE_IMAGE = 100
    // 加载框
    private val sweetAlertDialog: SweetAlertDialog by lazy {
        SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText("头像")
                .setContentText("更新中...")
        /*
        .setCancelButton("取消") {
            model.cancelWork()
            sweetAlertDialog.dismiss()
        }*/
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_manager)

        iv_head.setOnClickListener {
            // 选择处理的图片
            val chooseIntent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(chooseIntent, REQUEST_CODE_IMAGE)
        }

        taskObservation()
    }

    // WorkManager 任务状态的观测
    private fun taskObservation() {
        // 配合LiveData 实现观测后台执行的后台任务
        viewModel.outPutWorkInfos.observe(this, Observer {
            if (it.isNullOrEmpty()) return@Observer

            val state = it[0]
            if (state.state.isFinished) {
                // 更新头像
                val outputImageUri = state.outputData.getString(KEY_IMAGE_URI)
                if (!outputImageUri.isNullOrEmpty()) {
                    toast(outputImageUri)
                    iv_head.setImageURI(Uri.parse(outputImageUri))
                } else {
                    toast("outputImageUri is null")
                }
                sweetAlertDialog.dismiss()
            }
        })
    }

    /**
     * 图片选择完成的回调
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_IMAGE -> data?.let { handleImageRequestResult(data) }
                else -> Log.d(TAG, "Unknown request code.")
            }
        } else {
            Log.e(TAG, String.format("Unexpected Result code %s", resultCode))
        }
    }

    /**
     * 图片选择完成的处理
     */
    private fun handleImageRequestResult(intent: Intent) {
        // 如果clipdata可用，我们使用它，否则我们使用data
        val imageUri: Uri? = intent.clipData?.let {
            it.getItemAt(0).uri
        } ?: intent.data

        if (imageUri == null) {
            Log.e(TAG, "Invalid input image Uri.")
            return
        }

        sweetAlertDialog.show()
        // 图片模糊处理
        viewModel.setImageUri(imageUri.toString())
        viewModel.applyBlur(3)
    }
}
