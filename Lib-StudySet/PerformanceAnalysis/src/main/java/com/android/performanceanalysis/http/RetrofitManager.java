package com.android.performanceanalysis.http;

import com.android.performanceanalysis.LaunchApplication;
import com.android.performanceanalysis.http.converter.FastJsonConverterFactory;
import com.android.performanceanalysis.http.dns.OkHttpDNS;
import com.android.performanceanalysis.http.interceptor.GzipRequestInterceptor;
import com.android.performanceanalysis.http.interceptor.NoNetInterceptor;
import com.android.performanceanalysis.http.listener.OkHttpEventListener;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public class RetrofitManager {
    private static final APIService API_SERVICE;

    public static APIService getApiService() {
        return API_SERVICE;
    }

    public static final String HTTP_SPORTSNBA_QQ_COM = "http://sportsnba.qq.com/";

    static {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        //设置缓存目录
        Cache cache = new Cache(LaunchApplication.getInstance().getCacheDir(), 10 * 1024 * 1024);
        //设置拦截器
        client.dns(OkHttpDNS.getIns(LaunchApplication.getInstance()))
                .eventListenerFactory(OkHttpEventListener.FACTORY)
                .cache(cache)
                .addInterceptor(new NoNetInterceptor())
                // 可尝试开启，未测试
//                .addInterceptor(new GzipRequestInterceptor())
                .addInterceptor(logging);

        final Retrofit RETROFIT = new Retrofit.Builder()
                .baseUrl(HTTP_SPORTSNBA_QQ_COM)
                .addConverterFactory(FastJsonConverterFactory.create())
                .client(client.build())
                .build();
        API_SERVICE = RETROFIT.create(APIService.class);
    }
}
