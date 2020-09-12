package com.android.performanceanalysis.task;

import com.android.performanceanalysis.LaunchApplication;
import com.android.performanceanalysis.launchstarter.task.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * 子线程执行
 * 需要在getDeviceId之后执行
 */
public class InitJPushTask extends Task {

    // 实现此方法意味着当前任务取决于GetDeviceIdTask任务，
    // 即会在 GetDeviceIdTask 任务执行完后再执行当前任务
    @Override
    public List<Class<? extends Task>> dependsOn() {
        List<Class<? extends Task>> task = new ArrayList<>();
//        task.add(GetDeviceIdTask.class);
        return task;
    }

    @Override
    public void run() {
        // TODO 推送初始化
//        JPushInterface.init(mContext);
//        LaunchApplication app = (LaunchApplication) mContext;
//        JPushInterface.setAlias(mContext, 0, app.getDeviceId());
    }
}