package com.android.baselibrary.strategy.demo.httpProcessor;

import java.util.Map;

/**
 * 网络抽象层接口
 */
public interface IHttpProcessor {
    void post(String url, Map<String, Object> params, final HttpCallback callback);
    void get(String url, Map<String, Object> params, final HttpCallback callback);
}

