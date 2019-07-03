package com.android.baselibrary.strategy.httpProcessor.http;

import com.android.baselibrary.app.AppGlobal;
import com.android.baselibrary.app.ConfigKeys;
import com.android.baselibrary.strategy.httpProcessor.http.converter.ResponseConverterFactory;
import com.android.baselibrary.strategy.httpProcessor.net.RestService;
import com.android.baselibrary.strategy.httpProcessor.net.rx.RxRestService;

import java.util.ArrayList;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitCreator {
    /**
     * 构建全局Retrofit客户端
     * */
    private static final class RetrofitHolder {
        private static final String BASE_URL = (String) AppGlobal.getConfiguration(ConfigKeys.API_HOST);
        private static final Retrofit RETROFIT_CLIENT = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(OkHttpHolder.OK_HTTP_CLIENT)
                .addConverterFactory(ResponseConverterFactory.create())//添加自定义响应转换器
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    private static final class OkHttpHolder {
        private static final int TIME_OUT = 60;
        private static final int CONN_TIMEOUT = 12;//连接超时时间,单位秒

        private static final OkHttpClient.Builder BUILDER = new OkHttpClient.Builder();
        private static final ArrayList<Interceptor> INTERCEPTORS = AppGlobal.getConfiguration(ConfigKeys.INTERCEPTOR);

        private static OkHttpClient.Builder addInterceptor() {
            if (INTERCEPTORS != null && !INTERCEPTORS.isEmpty()) {
                for (Interceptor interceptor : INTERCEPTORS) {
                    BUILDER.addInterceptor(interceptor);
                }
            }
            return BUILDER;
        }
        private static final OkHttpClient OK_HTTP_CLIENT = addInterceptor()
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)//设置读取时间为一分钟
                .readTimeout(CONN_TIMEOUT, TimeUnit.SECONDS)//设置连接时间为12s
                .build();
    }

    private static final class RxRetrofitServiceHolder {
        private static final APIService API_SERVICE = RetrofitHolder.RETROFIT_CLIENT
                .create(APIService.class);
    }

    public static APIService getRxRestService() {
        return RxRetrofitServiceHolder.API_SERVICE;
    }

    /**
     * 参数容器
     */
    private static final class ParamsHolder {
        private static final WeakHashMap<String, Object> PARAMS = new WeakHashMap<>();
    }

    public static WeakHashMap<String, Object> getParams() {
        return ParamsHolder.PARAMS;
    }
}
