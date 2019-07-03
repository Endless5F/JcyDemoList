package com.android.baselibrary.common;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

/**
 * @author jcy
 * @date 2018/8/2
 * descrption:
 */
public class GlobalActivityLifecycle implements Application.ActivityLifecycleCallbacks {
    private final String TAG = "GlobalActivityLifecycle";
    private static int mResumed = 0;
    private static int mPaused = 0;
    private static Activity newActivity;
    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if(activity.getParent()!=null){
            newActivity = activity.getParent();
        }else{
            newActivity = activity;
        }
        newActivity = activity;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d("background", "onstarted:" + activity.getLocalClassName());
        if(activity.getParent()!=null){
            newActivity = activity.getParent();
        }else{
            newActivity = activity;
        }
        newActivity = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if(activity.getParent()!=null){
            newActivity = activity.getParent();
        }else{
            newActivity = activity;
        }
        newActivity = activity;
        mResumed++;
        Log.d("background", "onresumed:" + activity.getLocalClassName());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        mPaused++;
        Log.d("background", "onpaused:" + activity.getLocalClassName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d("background", "onstoped:" + activity.getLocalClassName());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public static Activity getNewActivity(){
        return newActivity;
    }

    public static boolean isApplicationInForeground() {
        // 当所有 Activity 的状态中处于 resumed mP的大于 paused 状态的，即可认为有Activity处于前台状态中
        return mResumed > mPaused;
    }
}
