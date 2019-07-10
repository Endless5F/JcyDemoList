package com.android.customwidget.kotlin.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


object FormatUtil {

    /*
     * 将时间转换为时间戳
     */
    @Throws(ParseException::class)
    fun dateToStamp(s: String): String {
        val res: String
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = simpleDateFormat.parse(s)
        val ts = date.getTime()
        res = ts.toString()
        return res
    }

    /*
     * 将时间戳转换为时间
     */
    fun stampToDate(time: Long): String {
        val res: String
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = Date(time)
        res = simpleDateFormat.format(date)
        return res
    }
}