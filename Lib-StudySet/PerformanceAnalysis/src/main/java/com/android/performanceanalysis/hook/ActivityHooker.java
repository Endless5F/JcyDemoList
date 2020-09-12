package com.android.performanceanalysis.hook;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.android.performanceanalysis.utils.LogUtils;
import com.android.performanceanalysis.utils.WakeLockUtils;

import me.ele.lancet.base.Origin;
import me.ele.lancet.base.Scope;
import me.ele.lancet.base.annotations.Insert;
import me.ele.lancet.base.annotations.Proxy;
import me.ele.lancet.base.annotations.TargetClass;

public class ActivityHooker {

    public static ActivityRecord sActivityRecord;

    static {
        sActivityRecord = new ActivityRecord();
    }

    /**
     * Hook的是AppCompatActivity.onCreate方法
     * Scope.ALL 代表：AppCompatActivity以及其子类的onCreate方法都会被hook
     * mayCreateSuper = true 代表：当目标函数不存在时，通过mayCreateSuper创建
     */
    @Insert(value = "onCreate", mayCreateSuper = true)
    @TargetClass(value = "android.support.v7.app.AppCompatActivity", scope = Scope.ALL)
    protected void onCreate(Bundle savedInstanceState) {
        sActivityRecord.mOnCreateTime = System.currentTimeMillis();
        Origin.callVoid();
    }

    @Insert(value = "onWindowFocusChanged", mayCreateSuper = true)
    @TargetClass(value = "android.support.v7.app.AppCompatActivity", scope = Scope.ALL)
    public void onWindowFocusChanged(boolean hasFocus) {
        sActivityRecord.mOnWindowsFocusChangedTime = System.currentTimeMillis();
        LogUtils.i("onWindowFocusChanged cost " + (sActivityRecord.mOnWindowsFocusChangedTime - sActivityRecord.mOnCreateTime));
        Origin.callVoid();
    }

    public static long runTime = 0;


    // 线程运行时长：超过阀值预警(运行时间过长可以停止)
    @Insert(value = "run")
    @TargetClass(value = "java.lang.Runnable", scope = Scope.ALL)
    public void run() {
        runTime = System.currentTimeMillis();
        Origin.callVoid();
        LogUtils.i("runTime " + (System.currentTimeMillis() - runTime));
    }

    // Hook的是Log.i方法
    @Proxy("i")
    @TargetClass("android.util.Log")
    public static int i(String tag, String msg) {
        msg = msg + "";
        return (int) Origin.call();
    }


    public static String trace;

    public static long sStartTime = 0;

    @Insert(value = "acquire")
    @TargetClass(value = "com.android.performanceanalysis.utils.WakeLockUtils",scope = Scope.SELF)
    public static void acquire(Context context){
        trace = Log.getStackTraceString(new Throwable());
        sStartTime = System.currentTimeMillis();
        Origin.callVoid();
        //兜底策略
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                WakeLockUtils.release();
            }
        },1000);
    }

    @Insert(value = "release")
    @TargetClass(value = "com.android.performanceanalysis.utils.WakeLockUtils",scope = Scope.SELF)
    public static void release(){
        LogUtils.i("PowerManager "+(System.currentTimeMillis() - sStartTime)+"/n"+trace);
        Origin.callVoid();
    }

}