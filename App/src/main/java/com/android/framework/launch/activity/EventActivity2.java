package com.android.framework.launch.activity;

import android.os.Bundle;

import com.android.baselibrary.base.BaseToolbarCompatActivity;
import com.android.baselibrary.bean.EventBean;
import com.android.baselibrary.bean.Member;
import com.android.framework.R;
import com.facebook.stetho.common.LogUtil;

import org.greenrobot.eventbus.EventBus;

public class EventActivity2 extends BaseToolbarCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event2);
        setMiddleTitle("我是EventBus界面2");
        setLeftButtonIsBack(true);
        LogUtil.i("onCreate2");
        EventBus.getDefault().post(new EventBean("zhangsan", 23, "12345678901"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.i("onStart2");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.i("onResume2");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.i("onStop2");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i("onDestroy2");
    }
}
