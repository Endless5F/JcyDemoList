package com.android.performanceanalysis.http.interceptor;

import com.android.performanceanalysis.LaunchApplication;
import com.android.performanceanalysis.launchstarter.utils.Utils;
import com.android.performanceanalysis.utils.NetUtils;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

//创建拦截器
public class NoNetInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();
        //无网络的情况下使用缓存
        if(NetUtils.getNetWorkStart(LaunchApplication.getInstance()) == 1){
            builder.cacheControl(CacheControl.FORCE_CACHE);
        }
        return chain.proceed(builder.build());
    }
}
