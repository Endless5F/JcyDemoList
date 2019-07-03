package com.android.baselibrary.strategy.demo.httpProcessor;

import java.net.URLEncoder;
import java.util.Map;

public class HttpHelper implements IHttpProcessor {
    private static volatile HttpHelper instance;

    private HttpHelper() { }

    public static HttpHelper obtain() {
        if (instance == null) {
            synchronized (HttpHelper.class) {
                if (instance == null) {
                    instance = new HttpHelper();
                }
            }
        }
        return instance;
    }

    private static IHttpProcessor mHttpProcessor = null;

    public static void init(IHttpProcessor httpProcessor) {
        mHttpProcessor = httpProcessor;
    }

    @Override
    public void post(String url, Map<String, Object> params, HttpCallback callback) {
        final String finalUrl = appendParams(url, params);
        mHttpProcessor.post(finalUrl, params, callback);
    }

    @Override
    public void get(String url, Map<String, Object> params, HttpCallback callback) {
        mHttpProcessor.post(url, params, callback);
    }

    public static String appendParams(String url, Map<String, Object> params) {
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
