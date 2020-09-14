package com.android.framework.launch.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.android.baselibrary.base.BaseToolbarCompatActivity;
import com.android.baselibrary.bean.EventBean;
import com.android.baselibrary.utils.ToastUtils;
import com.android.framework.R;
import com.facebook.stetho.common.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class EventActivity1 extends BaseToolbarCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event1);
        setMiddleTitle("我是EventBus界面1,点击图标跳转");
        setLeftButtonIsBack(true);
        LogUtil.i("onCreate1");
        findViewById(R.id.tv_event1).setOnClickListener(v -> {
            Intent intent = new Intent(EventActivity1.this, EventActivity2.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.i("onStart1");
        // 官方demo 注册事件在onStart中
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.i("onResume1");
    }

    /**
     * @Subscribe 订阅者方法
     * 订阅者方法将在发布事件所在的线程中被调用。这是 默认的线程模式。事件的传递是同步的，
     * 一旦发布事件，所有该模式的订阅者方法都将被调用。这种线程模式意味着最少的性能开销，
     * 因为它避免了线程的切换。因此，对于不要求是主线程并且耗时很短的简单任务推荐使用该模式。
     * 使用该模式的订阅者方法应该快速返回，以避免阻塞发布事件的线程，这可能是主线程。
     * 注：POSTING 就是和发布事件 post 所在一个线程中，post为 主/子 线程POSTING就为 主/子 线程中
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onMessageEventPosting(EventBean member) {
        LogUtil.i(member.toString());
        LogUtil.i("onMessageEventPosting(), current thread is " + Thread.currentThread().getName());
    }

    /**
     * @Subscribe 订阅者方法
     * 注：不管post发布事件在什么线程中，MAIN 都在主线程
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false, priority = 2)
    public void onMessageEventMain(EventBean member) {
        LogUtil.i(member.toString());
        TextView viewById = (TextView) findViewById(R.id.tv_event1);
        viewById.setText(member.toString());
        ToastUtils.showShortToast(member.toString());
        LogUtil.i("onMessageEventMain(), current thread is " + Thread.currentThread().getName());
    }

    /**
     * @Subscribe 订阅者方法
     * 订阅者方法将在主线程（UI线程）中被调用。因此，可以在该模式的订阅者方法中直接更新UI界面。
     * 事件将先进入队列然后才发送给订阅者，所以发布事件的调用将立即返回。
     * 这使得事件的处理保持严格的串行顺序。使用该模式的订阅者方法必须快速返回，以避免阻塞主线程。
     * 注：不管post发布事件在什么线程中，MAIN_ORDERED 也都在主线程，不过该模式事件是串行的，按先后顺序的
     */
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onMessageEventMainOrdered(EventBean member) {
        LogUtil.i("onMessageEventMainOrdered(), current thread is " + Thread.currentThread().getName());
    }

    /**
     * @Subscribe 订阅者方法
     * 注：post（发布者）若为子线程，则 BACKGROUND 则是于post同一子线程中，若post为主线程，则BACKGROUND为单独的后台线程
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEventBackground(EventBean member) {
        LogUtil.i("onMessageEventBackground(), current thread is " + Thread.currentThread().getName());
    }

    /**
     * @Subscribe 订阅者方法
     * 注：post（发布者）无论是在子线程还是主线程，ASYNC 都会单开一个线程
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageEventAsync(EventBean member) {
        LogUtil.i("onMessageEventAsync(), current thread is " + Thread.currentThread().getName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.i("onStop1");
        // 官方demo 解绑事件在onStop中
        EventBus.getDefault().unregister(this);
    }
}
