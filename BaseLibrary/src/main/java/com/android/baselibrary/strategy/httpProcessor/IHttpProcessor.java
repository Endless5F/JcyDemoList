package com.android.baselibrary.strategy.httpProcessor;

import android.content.Context;

import com.android.baselibrary.strategy.httpProcessor.callBack.HttpCallback;

import java.util.WeakHashMap;

/**
 * 网络抽象层接口
 */
public interface IHttpProcessor {
    void get(Context context, String url, WeakHashMap<String, Object> params, final HttpCallback callback);
    void post(Context context, String url, WeakHashMap<String, Object> params, final HttpCallback callback);
}

