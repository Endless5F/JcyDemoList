package com.android.httpserver.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * Android之让代码跑在主线程(无context上下文)的封装
 * */
public class MainHandlerUtils extends Handler {

    private static volatile MainHandlerUtils mInstance;

    private MainHandlerUtils() {
        super(Looper.getMainLooper());
    }

    public static MainHandlerUtils getInstance() {
        if (mInstance == null) {
            synchronized (MainHandlerUtils.class) {
                if (mInstance == null) {
                    mInstance = new MainHandlerUtils();
                }
            }
        }
        return mInstance;
    }
}

