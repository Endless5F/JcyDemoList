package com.android.framework.launch;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.baselibrary.app.AppGlobal;
import com.android.baselibrary.common.GlobalActivityLifecycle;
import com.android.baselibrary.utils.AppUtils;
import com.android.baselibrary.utils.CrashHandler;
import com.android.framework.CustomEventBusIndex;
import com.didichuxing.doraemonkit.DoraemonKit;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

public class App extends Application {
    public static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "Create app start");
        //多进程会导致应用application被多次初始化
        if (TextUtils.equals(getCurrentProcessName(this), getPackageName())) {
            initApp();//判断成功后才执行初始化代码
        }
        Log.w(TAG, "Create app end");
        AppGlobal.init(this)
                // wanandroid开放api
                .withApiHost("https://www.wanandroid.com")
                .withWeChatAppId("你的微信AppKey")
                .withWeChatAppSecret("你的微信AppSecret")
                .withJavascriptInterface("latte")
                .configure();

        DoraemonKit.install(this);

        // EventBus安装默认事件总线
        EventBus.builder().addIndex(new CustomEventBusIndex()).installDefaultEventBus();
    }

    private void initApp() {
        Log.w(TAG, " initApp begin");
        //AppUtil 初始化（方便全局获取ApplicationContext,RefWatcher）
        AppUtils.init(this);
        registerActivityLifecycleCallbacks(new GlobalActivityLifecycle());
        //CrashHandler 初始化（保存崩溃日志到本地,以及是否崩溃重启）
        //保存CPU数据时若不关闭进程，就算退出了所有Activity也会继续保存CPU数据，因此必须在退出后结束进程
        CrashHandler.getInstance().init(this, true, false);
    }

    private String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess
                : Objects.requireNonNull(mActivityManager).getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // Android 5.0以下分包必加
//        MultiDex.install(this);
    }
}