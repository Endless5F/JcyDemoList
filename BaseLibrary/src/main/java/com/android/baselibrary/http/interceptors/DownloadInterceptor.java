package com.android.baselibrary.http.interceptors;

import com.android.baselibrary.http.download.DownloadListener;
import com.android.baselibrary.http.download.DownloadResponseBody;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Interceptor for download
 * 下载进度拦截器，通过拦截，然后对请求返回来的响应体进行包装，实现下载进度
 */
public class DownloadInterceptor implements Interceptor {

    private DownloadListener listener;

    public DownloadInterceptor(DownloadListener listener) {
        this.listener = listener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        //拦截
        Response originalResponse = chain.proceed(chain.request());
        //包装响应体
        return originalResponse.newBuilder()
                .body(new DownloadResponseBody(originalResponse.body(), listener))
                .build();
    }
}
