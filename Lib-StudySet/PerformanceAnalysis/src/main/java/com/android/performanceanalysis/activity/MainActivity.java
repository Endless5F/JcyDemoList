package com.android.performanceanalysis.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.android.performanceanalysis.R;
import com.android.performanceanalysis.adapter.HomePageAdapter;
import com.android.performanceanalysis.data.HomeData;
import com.android.performanceanalysis.service.JobSchedulerService;
import com.android.performanceanalysis.utils.DateUtils;
import com.android.performanceanalysis.utils.NetStatusUtils;
import com.android.performanceanalysis.utils.PermissionsUtils;
import com.android.performanceanalysis.utils.WakeLockUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    /**
     * 是否处于前台的标识
     */
    private boolean appIsFront;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        PermissionsUtils.getInstance().chekPermissions(this, permissions,
                new PermissionsUtils.IPermissionsResult() {
                    @Override
                    public void passPermissons() {
                        Toast.makeText(MainActivity.this, "权限通过，可以做其他事情!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void forbitPermissons() {
                        Toast.makeText(MainActivity.this, "权限不通过!", Toast.LENGTH_SHORT).show();
                    }
                });

        RecyclerView rl_demo_list = findViewById(R.id.rl_demo_list);
        rl_demo_list.setLayoutManager(new LinearLayoutManager(this));//线性布局
        HomePageAdapter homePageAdapter = new HomePageAdapter(this, HomeData.addDevTotalRes);
//        homePageAdapter.addHeaderView(R.layout.activity_home_page_header);
        rl_demo_list.setAdapter(homePageAdapter);


        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(fab, "Replace with your own action",
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // 获取本月第一天 0时 到当前时间的流量
        NetStatusUtils.getNetStatus(this, DateUtils.getTimesMonthmorning().getTime(),
                System.currentTimeMillis());

        // 通过生命周期的监听来判断前后台状态
        registerActivityLifecycleCallbacks();
        // 前台后台流量获取
        Executors.newScheduledThreadPool(1).schedule(new Runnable() {
            @Override
            public void run() {
                long netUse = NetStatusUtils.getNetStatus(MainActivity.this,
                        System.currentTimeMillis() - 30 * 1000, System.currentTimeMillis());
                //开始时间：当前时间-30秒，结束时间：就是当前时间
                //前台还是后台
                if (appIsFront) {
                    //前台
                } else {
                    //后台
                }
            }
        }, 30, TimeUnit.SECONDS);

        // 电量优化：以唤醒锁为例
        // 此处模拟的是WakeLock使用的兜底策略
        WakeLockUtils.acquire(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                WakeLockUtils.release();
            }
        }, 200);

        // 电量优化之JobScheduler
        startJobScheduler();
    }

    /**
     * 演示JobScheduler的使用
     */
    private void startJobScheduler() {
        //5.0之后才能使用
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(getPackageName(), JobSchedulerService.class.getName()));
            builder.setRequiresCharging(true) // 任务执行时需要连接电源
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // 任务需要在WIFI的状态下
            if (jobScheduler != null) {
                jobScheduler.schedule(builder.build());
            }
        }
    }

    private void registerActivityLifecycleCallbacks() {
        getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                //认为进入前台
                appIsFront = true;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                //认为进入后台
                appIsFront = false;
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 多一个参数this
        PermissionsUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions,
                grantResults);
    }
}
