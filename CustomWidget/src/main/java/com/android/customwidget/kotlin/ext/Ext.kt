package com.android.customwidget.kotlin.ext

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * 获取asstes文件夹下文件内容
 */
fun Context.getAssetsFileJson(fileName: String): String {
    val stringBuilder = StringBuilder()

    try {
        //获取assets资源管理器
        val assetManager = this.assets
        //通过管理器打开文件并读取
        val bf = BufferedReader(InputStreamReader(assetManager.open(fileName)));
        var line: String?
        while (bf.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
    } catch (e: IOException) {
        e.printStackTrace();
    }

    return stringBuilder.toString();
}