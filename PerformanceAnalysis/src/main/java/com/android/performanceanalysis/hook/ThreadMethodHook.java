package com.android.performanceanalysis.hook;

import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;

/**
 * 监控Java线程的创建和销毁
 */
public class ThreadMethodHook extends XC_MethodHook {

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        Thread t = (Thread) param.thisObject;
        Log.i("ThreadMethodHook", "thread:" + t + ", started..");
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
        Thread t = (Thread) param.thisObject;
        Log.i("ThreadMethodHook", "thread:" + t + ", exit..");
    }
}