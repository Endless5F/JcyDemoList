package com.android.performanceanalysis.task;

import android.app.Application;

import com.android.performanceanalysis.launchstarter.task.MainTask;

/**
 * 主线程执行的task
 */
public class InitWeexTask extends MainTask {
    // 该任务是否必须在application初始化完成前就必须完成初始化，是则返回true
    @Override
    public boolean needWait() {
        return true;
    }

    @Override
    public void run() {
        // TODO 初始化微信
//        InitConfig config = new InitConfig.Builder().build();
//        WXSDKEngine.initialize((Application) mContext, config);
    }
}
