package com.android.performanceanalysis.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.android.performanceanalysis.R;
import com.android.performanceanalysis.adapter.HomePageAdapter;
import com.android.performanceanalysis.data.HomeData;
import com.android.performanceanalysis.utils.DateUtils;
import com.android.performanceanalysis.utils.NetStatusUtils;

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
                long netUse = NetStatusUtils.getNetStatus(MainActivity.this, System.currentTimeMillis() - 30 * 1000, System.currentTimeMillis());//开始时间：当前时间-30秒，结束时间：就是当前时间
                //前台还是后台
                if (appIsFront) {
                    //前台
                } else {
                    //后台
                }
            }
        }, 30, TimeUnit.SECONDS);
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
}
