package com.android.performanceanalysis;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.multidex.MultiDex;
import android.support.v4.os.TraceCompat;
import android.util.Log;
import android.widget.ImageView;

import com.android.performanceanalysis.hook.ImageHook;
import com.android.performanceanalysis.hook.ThreadMethodHook;
import com.android.performanceanalysis.launchstarter.TaskDispatcher;
import com.android.performanceanalysis.task.InitJPushTask;
import com.android.performanceanalysis.task.InitWeexTask;
import com.android.performanceanalysis.utils.LaunchTimerUtil;
import com.android.performanceanalysis.utils.LogUtils;
import com.squareup.leakcanary.LeakCanary;
import com.taobao.android.dexposed.DexposedBridge;
import com.taobao.android.dexposed.XC_MethodHook;

import dalvik.system.DexFile;

public class LaunchApplication extends Application {

    private static final String TAG = "LaunchApplication";
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

        epicHook();

        virtualOperating();
        initVirtualOperating(this);
    }

    private void epicHook() {
        // hook构造函数  监控ImageView加载的图片大小和ImageView的大小是否合适，过大则发出警告
        DexposedBridge.findAndHookMethod(ImageView.class, "setImageBitmap", Bitmap.class, new ImageHook());


        // 拦截Thread 类以及 Thread 类所有子类的 run方法，在 run 方法开始执行和退出的时候进行拦截，
        // 就可以知道进程内部所有Java线程创建和销毁的时机；更进一步，你可以结合Systrace等工具，来生成整个过程的执行流程图
        DexposedBridge.hookAllConstructors(Thread.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Thread thread = (Thread) param.thisObject;
                Class<?> clazz = thread.getClass();
                if (clazz != Thread.class) {
                    Log.d(TAG, "found class extend Thread:" + clazz);
                    DexposedBridge.findAndHookMethod(clazz, "run", new ThreadMethodHook());
                }
                Log.d(TAG, "Thread: " + thread.getName() + " class:" + thread.getClass() + " is " + "created.");
            }
        });
        DexposedBridge.findAndHookMethod(Thread.class, "run", new ThreadMethodHook());

        // 监控dex文件的加载
        DexposedBridge.findAndHookMethod(DexFile.class, "loadDex", String.class, String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                String dex = (String) param.args[0];
                String odex = (String) param.args[1];
                Log.i(TAG, "load dex, input:" + dex + ", output:" + odex);
            }
        });
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
