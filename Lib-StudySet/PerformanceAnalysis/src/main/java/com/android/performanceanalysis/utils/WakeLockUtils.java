package com.android.performanceanalysis.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;

/**
 * 让系统保持“清醒”
 * 当手机灭屏状态下保持一段时间后，系统会进入休眠，一些后台任务比如网络下载，播放音乐会得不到正常的执行。
 * WakeLock API可以确保应用程序中关键代码的正确执行，使应用程序有能力控制AP的休眠状态。
 *
 * <!--WakeLock需要的权限-->
 * <uses-permission android:name="android.permission.WAKE_LOCK"/>
 *
 * 防止灭屏：官方建议使用，不需要申请WakeLock和权限
 * getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 *
 * PARTIAL_WAKE_LOCK: 灭屏，关闭键盘背光的情况下，CPU依然保持运行。
 *
 * PROXIMITY_SCREEN_OFF_WAKE_LOCK： 基于距离感应器熄灭屏幕。最典型的运用场景是我们贴近耳朵打电话时，屏幕会自动熄灭。
 */
public class WakeLockUtils {

    private static PowerManager.WakeLock sWakeLock;

    public static void acquire(Context context) {
        if (sWakeLock == null) {
            sWakeLock = createWakeLock(context);
        }
        if (sWakeLock != null && !sWakeLock.isHeld()) {
            sWakeLock.acquire();
            sWakeLock.acquire(1000);
        }
    }

    public static void release() {
        // 一些逻辑
        try {

        } catch (Exception e) {

        } finally {
            // 为了演示正确的使用方式
            if (sWakeLock != null && sWakeLock.isHeld()) {
                sWakeLock.release();
                sWakeLock = null;
            }
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    private static PowerManager.WakeLock createWakeLock(Context context) {
        PowerManager pm =
                (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            // levelAndFlags ：灭屏，关闭键盘背光的情况下，CPU依然保持运行。
            return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        }
        return null;
    }

}
