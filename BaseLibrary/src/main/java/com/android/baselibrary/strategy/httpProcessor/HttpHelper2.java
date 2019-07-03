package com.android.baselibrary.strategy.httpProcessor;

import com.android.baselibrary.strategy.httpProcessor.callBack.HttpCallback;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.net.URLEncoder;
import java.util.Map;
import java.util.WeakHashMap;

public class HttpHelper2 implements IHttpProcessor2 {
    private static volatile HttpHelper2 instance;

    private HttpHelper2() { }

    public static HttpHelper2 obtain() {
        if (instance == null) {
            synchronized (HttpHelper2.class) {
                if (instance == null) {
                    instance = new HttpHelper2();
                }
            }
        }
        return instance;
    }

    private static IHttpProcessor2 mHttpProcessor = null;

    public static void init(IHttpProcessor2 httpProcessor) {
        mHttpProcessor = httpProcessor;
    }

    @Override
    public void post(String url, WeakHashMap<String, Object> params, LifecycleProvider<ActivityEvent> provider, HttpCallback callback) {
        final String finalUrl = appendParams(url, params);
        mHttpProcessor.post(finalUrl, params,provider, callback);
    }

    @Override
    public void get(String url, WeakHashMap<String, Object> params, LifecycleProvider<ActivityEvent> provider, HttpCallback callback) {
        mHttpProcessor.get(url, params,provider, callback);
    }

    private static String appendParams(String url, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }
        StringBuilder urlBuilder = new StringBuilder(url);
        if (urlBuilder.indexOf("?") <= 0) {
            urlBuilder.append("?");
        } else {
            if (!urlBuilder.toString().endsWith("?")) {
                urlBuilder.append("&");
            }
        }
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            urlBuilder.append("&" + entry.getKey()).append("=").append(encode(entry.getValue().toString()));
        }
        return urlBuilder.toString();
    }

    private static String encode(String str) {
        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
