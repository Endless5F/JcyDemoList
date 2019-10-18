package com.android.performanceanalysis;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.v4.os.TraceCompat;

import com.android.performanceanalysis.launchstarter.TaskDispatcher;
import com.android.performanceanalysis.task.InitJPushTask;
import com.android.performanceanalysis.task.InitWeexTask;
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

        // 启动器的使用
//        TaskDispatcher.init(LaunchApplication.this);
//        TaskDispatcher dispatcher = TaskDispatcher.createInstance();
//        dispatcher.addTask(new InitAMapTask())
//                .addTask(new InitStethoTask())
//                .addTask(new InitWeexTask())
//                .addTask(new InitBuglyTask())
//                .addTask(new InitFrescoTask())
//                .addTask(new InitJPushTask())
//                .addTask(new InitUmengTask())
//                .addTask(new GetDeviceIdTask())
//                .start();
//        dispatcher.await();

        virtualOperating();
        initVirtualOperating(this);
    }

    public static LaunchApplication getInstance() {
        return app;
    }

    // 虚拟操作
    public void virtualOperating() {
        int key = 0;
        for (int i = 0; i < 10000; i++) {
            key += i;
        }
        LogUtils.d("" + key);
    }

    // 初始化虚拟操作
    public void initVirtualOperating(LaunchApplication launchApplication) {
        int key = 0;
        for (int i = 0; i < 10000; i++) {
            key += i;
        }
        LogUtils.d("" + key);
    }
}
