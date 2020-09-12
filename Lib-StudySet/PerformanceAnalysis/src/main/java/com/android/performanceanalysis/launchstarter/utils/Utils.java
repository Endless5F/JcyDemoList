package com.android.performanceanalysis.launchstarter.utils;

import android.app.ActivityManager;
import android.content.Context;

public class Utils {
    /**
     * 获取当前进程名
     */
    private static String getCurrentProcessName(Context sContext) {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) sContext
                .getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
                if (process.pid == pid) {
                    processName = process.processName;
                }
            }
        }
        return processName;
    }

    /**
     * 判断当前进程是否为主进程
     */
    public static boolean isMainProcess(Context sContext) {
        return sContext.getApplicationContext().getPackageName().equals(getCurrentProcessName(sContext));
    }

}
