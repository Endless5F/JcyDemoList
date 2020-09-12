package com.android.baselibrary.http.interceptors;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 创建日志拦截器
 * 打印服务器接口地址，头信息等（以红颜色）
 */
public class LogInterceptor implements Interceptor {

    private Response getResponse(Chain chain, String json) {
        return new Response.Builder()
                .code(200)
                .addHeader("Content-Type", "application/json")
                .body(ResponseBody.create(MediaType.parse("application/json"), json))
                .message("OK")
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .build();
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        long t1 = System.nanoTime();
        Log.e("LogInterceptor  OkHttp", "HttpHelper1 " + String.format("Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()));
        /**
         * 注：若遇到 Throwable    null...java.net.SocketTimeoutException  异常，则服务器接口出现问题，询问后台接口
         * 该问题，只针对post请求或者文件带参上传
         * */
        okhttp3.Response response = chain.proceed(request);
        long t2 = System.nanoTime();

        Log.e("LogInterceptor  OkHttp", "HttpHelper2 " + String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers()));
        return response;
    }
}
