package com.android.baselibrary.strategy.httpProcessor;

import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.android.baselibrary.strategy.httpProcessor.callBack.HttpCallback;

import java.util.WeakHashMap;

/**
 * 网络抽象层接口
 */
public interface IHttpProcessor2 {
    void get(String url, WeakHashMap<String, Object> params, LifecycleProvider<ActivityEvent> provider, final HttpCallback callback);
    void post(String url, WeakHashMap<String, Object> params, LifecycleProvider<ActivityEvent> provider, final HttpCallback callback);
}

