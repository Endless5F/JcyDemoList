package com.android.baselibrary.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * Android之让代码跑在主线程(无context上下文)的封装
 * */
public class MainHandlerUtil extends Handler {

    private static volatile MainHandlerUtil mInstance;

    private MainHandlerUtil() {
        super(Looper.getMainLooper());
    }

    public static MainHandlerUtil getInstance() {
        if (mInstance == null) {
            synchronized (MainHandlerUtil.class) {
                if (mInstance == null) {
                    mInstance = new MainHandlerUtil();
                }
            }
        }
        return mInstance;
    }
}

