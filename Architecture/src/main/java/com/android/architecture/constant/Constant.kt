package com.android.architecture.constant

import android.widget.Toast

/**
 * Constant
 */
object Constant {
    /**
     * Toast
     */
    @JvmField
    var showToast: Toast? = null

    // WorkManagerModel
    const val KEY_IMAGE_URI = "KEY_IMAGE_URI"

    // 用于后台工作的详细通知的通知通道的名称
    @JvmField
    val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence = "Verbose WorkManager Notifications"
    const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION = "Shows notifications whenever work starts"
    @JvmField
    val NOTIFICATION_TITLE: CharSequence = "WorkRequest Starting"
    const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
    const val NOTIFICATION_ID = 1

    // 图像处理工作的名称
    const val IMAGE_MANIPULATION_WORK_NAME = "image_manipulation_work"

    // Other keys
    const val OUTPUT_PATH = "blur_filter_outputs"
    const val TAG_OUTPUT = "OUTPUT"

    const val DELAY_TIME_MILLIS: Long = 3000
}