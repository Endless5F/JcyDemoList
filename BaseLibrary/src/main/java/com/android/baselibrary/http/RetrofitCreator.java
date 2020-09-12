package com.android.baselibrary.http;

import com.android.baselibrary.app.AppGlobal;
import com.android.baselibrary.app.ConfigKeys;
import com.android.baselibrary.bean.ShareBeanResult;
import com.android.baselibrary.bean.deser.ShareBeanResultDeser;
import com.android.baselibrary.http.download.DownloadListener;
import com.android.baselibrary.http.interceptors.DownloadInterceptor;
import com.android.baselibrary.http.interceptors.LogInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitCreator {
    /**
     * 提供快捷解析Gson方式，从流直接解析成对象，也可防止字符串过长导致异常
     * @return
     */
    private static Gson providerGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ShareBeanResult.class, new ShareBeanResultDeser());
        gsonBuilder.setLenient();
        return gsonBuilder.create();
    }

    private static final class OkHttpHolder {
        private static final int TIME_OUT = 5;
        private static final int CONN_TIMEOUT = 5;//连接超时时间,单位秒

        private static final OkHttpClient.Builder BUILDER = new OkHttpClient.Builder();

        private static OkHttpClient.Builder addInterceptor() {
            BUILDER.addInterceptor(new LogInterceptor());
            return BUILDER;
        }

        private static OkHttpClient getDownloadClient(DownloadListener listener) {
            BUILDER.addInterceptor(new DownloadInterceptor(listener));
            return BUILDER
                    .retryOnConnectionFailure(true)
                    .connectTimeout(TIME_OUT, TimeUnit.SECONDS) // 设置读取时间
                    .readTimeout(CONN_TIMEOUT, TimeUnit.SECONDS) // 设置连接时间
                    .build();
        }

        private static final OkHttpClient OK_HTTP_CLIENT = addInterceptor()
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS) // 设置读取时间
                .readTimeout(CONN_TIMEOUT, TimeUnit.SECONDS) // 设置连接时间
                .build();
    }

    /**
     * 构建全局Retrofit客户端
     */
    private static final class RetrofitHolder {
        private static final String BASE_URL = AppGlobal.getConfiguration(ConfigKeys.API_HOST);

        private static final Retrofit RETROFIT_CLIENT = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(OkHttpHolder.OK_HTTP_CLIENT)
                .addConverterFactory(GsonConverterFactory.create(providerGson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        private static Retrofit getDownloadClient(DownloadListener listener) {
            return new Retrofit.Builder()
                    .client(OkHttpHolder.getDownloadClient(listener))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
    }

    private static final class RxRetrofitServiceHolder {
        private static final APIService API_SERVICE = RetrofitHolder.RETROFIT_CLIENT.create(APIService.class);
    }

    public static APIService getRxRestService() {
        return RxRetrofitServiceHolder.API_SERVICE;
    }

    public static APIService getDownloadService(DownloadListener listener) {
        return RetrofitHolder.getDownloadClient(listener).create(APIService.class);
    }
}