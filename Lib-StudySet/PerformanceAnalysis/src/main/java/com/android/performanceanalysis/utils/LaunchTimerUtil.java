package com.android.performanceanalysis.utils;

import android.util.Log;

/**
 * 启动时间测量：
 * 启动时埋点，启动结束埋点，二者差值
 * 启动位置放置在Application的attachBaseContext中，结束位置放置在MainActivity中
 * <p>
 * 特点：精确，可带到线上，推荐使用
 * 误区：onWindowFocusChange只是首帧时间
 * 正解：真实数据展示，首页列表第一条数据展示
 */
public class LaunchTimerUtil {
    private static long sTime;

    public static void startRecord() {
        sTime = System.currentTimeMillis();
    }

    public static void endRecord() {
        endRecord("");
    }

    public static void endRecord(String msg) {
        long cost = System.currentTimeMillis() - sTime;
        Log.i("LaunchTimerUtil", msg + "cost " + cost);
    }
}
