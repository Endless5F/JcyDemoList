package com.android.performanceanalysis;

import android.app.Application;
import android.content.Context;
import android.support.v4.os.TraceCompat;

import com.android.performanceanalysis.utils.LaunchTimerUtil;
import com.squareup.leakcanary.LeakCanary;

public class LaunchApplication extends Application {

    private static LaunchApplication app;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // 启动时间测量：开始记录
        LaunchTimerUtil.startRecord();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TraceCompat.beginSection("AppOnCreate");
        // TODO 一系列操作

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);

        TraceCompat.endSection();
    }

    public static LaunchApplication getInstance() {
        return app;
    }
}
