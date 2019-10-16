package com.android.performanceanalysis.utils;

import android.util.Log;

import java.util.Locale;

/**
 * 文件名 LogUtils.java
 * 描述 日志工具类
 */
public class LogUtils {
    //获取项目状态
    public static boolean DEBUG = true;
    //public static boolean DEBUG = BuildConfig.DEBUG;
    /**
     * 该参数需要根据实际情况来设置才能准确获取期望的调用信息，比如：
     * 在Java中，该参数应该为3
     * 在一般Android中，该参数为4
     * 你需要自己打印的看看，调用showAllElementsInfo()就可以了。
     */
    private static final int INDEX = 4;

    private static String getPrefix() {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[INDEX];
        String className = stackTraceElement.getClassName();
        int classNameStartIndex = className.lastIndexOf(".") + 1;
        className = className.substring(classNameStartIndex);
        String methodName = stackTraceElement.getMethodName();
        int methodLine = stackTraceElement.getLineNumber();
        String format = "%s-%s(L:%d)";
        return String.format(Locale.CHINESE, format, className, methodName, methodLine);
    }

    public static void v(String content) {
        if (DEBUG) Log.v(getPrefix(), content);
    }

    public static void v(String content, Throwable tr) {
        if (DEBUG) Log.v(getPrefix(), content, tr);
    }

    public static void v(String tag, String content) {
        if (DEBUG) Log.v(tag, content);
    }

    public static void d(String content) {
        if (DEBUG) Log.d(getPrefix(), content);
    }

    public static void d(String content, Throwable tr) {
        if (DEBUG) Log.d(getPrefix(), content, tr);
    }

    public static void d(String tag, String content) {
        if (DEBUG) Log.d(tag, content);
    }

    public static void i(String content) {
        if (DEBUG) Log.i(getPrefix(), "logcat_i ==> "+content);
    }

    public static void i(String content, Throwable tr) {
        if (DEBUG) Log.i(getPrefix(), content, tr);
    }

    public static void i(String tag, String content) {
        if (DEBUG) Log.i(tag, content);
    }

    public static void w(String content) {
        if (DEBUG) Log.e(getPrefix(), content);
    }

    public static void w(String content, Throwable tr) {
        if (DEBUG) Log.w(getPrefix(), content, tr);
    }

    public static void w(String tag, String content) {
        if (DEBUG) Log.w(tag, content);
    }

    public static void e(String content) {
        if (DEBUG) Log.e(getPrefix(), content);
    }

    public static void e(String content, Throwable tr) {
        if (DEBUG) Log.e(getPrefix(), content, tr);
    }

    public static void e(String tag, String content) {
        if (DEBUG) Log.e(tag, content);
    }

    public static String showAllElementsInfo() {
        StringBuilder print = new StringBuilder();
        int count = 0;
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            count++;
            print.append(String.format(Locale.getDefault(), "ClassName:%s " +
                            "\nMethodName:%s " +
                            "\nMethodLine:%d " +
                            "\n当前是第%d个 " +
                            "\n---------------------------- " +
                            "\n ",
                    stackTraceElement.getClassName(),
                    stackTraceElement.getMethodName(),
                    stackTraceElement.getLineNumber(),
                    count));
        }
        return print.toString();
    }
}
