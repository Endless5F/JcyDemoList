package com.android.performanceanalysis;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.v4.os.TraceCompat;

import com.android.performanceanalysis.utils.LaunchTimerUtil;
import com.android.performanceanalysis.utils.LogUtils;
import com.squareup.leakcanary.LeakCanary;

public class LaunchApplication extends Application {

    private static LaunchApplication app;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // 启动时间测量：开始记录
        LaunchTimerUtil.startRecord();
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        TraceCompat.beginSection("AppOnCreate");
        // TODO 一系列操作

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);

        TraceCompat.endSection();

        initVirtualOperating(this);
    }

    public static LaunchApplication getInstance() {
        return app;
    }

    // 初始化虚拟操作
    public void initVirtualOperating(LaunchApplication launchApplication) {
        LogUtils.d("");
    }
}
