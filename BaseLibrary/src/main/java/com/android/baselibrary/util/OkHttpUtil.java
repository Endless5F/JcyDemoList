package com.android.baselibrary.util;

import com.android.baselibrary.util.MainHandlerUtil;
import com.android.baselibrary.util.log.LoggerUtil;

import org.litepal.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @author jiaochengyun.ex
 */
public class OkHttpUtil {

    /**
     *  初始化OkHttp实例
     * */
    private static final class OkHttpHolder {
        private static final OkHttpClient.Builder BUILDER = new OkHttpClient.Builder();

        // OkHttp进行添加拦截器loggingInterceptor
        private static OkHttpClient.Builder addInterceptor() {
            // 日志显示级别
            HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.BODY;
            // 新建log拦截器
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    LoggerUtil.i("OkHttpClient", "OkHttp====Message:" + message);
                }
            });
            loggingInterceptor.setLevel(level);
            BUILDER.addInterceptor(loggingInterceptor);
            return BUILDER;
        }

        //定制OkHttp
        private static final OkHttpClient OK_HTTP_CLIENT = addInterceptor()
                .connectTimeout(60, TimeUnit.SECONDS)
                // 设置缓存 ：参数1：缓存路径（/storage/emulated/0/Android/data/xxx包名/cache） 参数2：最大缓存值(100MB)
//                .cache(new Cache(new File(getExternalCacheDir()), 100 * 1024 * 1024))
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 获取OkHttp实例
     * */
    public static OkHttpClient getOkHttpClient() {
        return OkHttpHolder.OK_HTTP_CLIENT;
    }

    private String mUrl;
    private final HashMap<String, String> mHeader;
    private final ISuccess mSuccess;
    private final IFailure mFailure;
    private final HashMap<String, String> mParams;

    /**
     * @param url 请求url
     * @param header 请求头
     * @param params 请求参数
     * @param success 请求成功回调
     * @param failure 请求失败回调
     * */
    private OkHttpUtil(String url, HashMap<String, String> header
            , HashMap<String, String> params, ISuccess success, IFailure failure) {
        mUrl = url;
        mHeader = header;
        mParams = params;
        mSuccess = success;
        mFailure = failure;
    }

    public static OkHttpUtilBuilder builder() {
        return new OkHttpUtilBuilder();
    }

    public static class OkHttpUtilBuilder {
        private String url;
        private HashMap<String, String> header;
        private ISuccess success;
        private IFailure failure;
        private HashMap<String, String> params = new HashMap<>();

        public final OkHttpUtilBuilder url(String url) {
            this.url = url;
            return this;
        }

        public final OkHttpUtilBuilder addHeader(HashMap<String, String> header) {
            this.header = header;
            return this;
        }

        public final OkHttpUtilBuilder params(HashMap<String, String> params) {
            params.putAll(params);
            return this;
        }

        public final OkHttpUtilBuilder params(String key, String value) {
            params.put(key, value);
            return this;
        }

        public final OkHttpUtilBuilder success(ISuccess iSuccess) {
            this.success = iSuccess;
            return this;
        }

        public final OkHttpUtilBuilder failure(IFailure iFailure) {
            this.failure = iFailure;
            return this;
        }

        public final OkHttpUtil build() {
            return new OkHttpUtil(url, header, params, success, failure);
        }
    }

    /**
     * get请求
     * */
    public void get() {
        if (mParams != null && !mParams.isEmpty()) {
            mUrl = appendParams(mUrl, mParams);
        }
        // 添加请求头
        Request.Builder builder = new Request.Builder();
        if(mHeader != null && !mHeader.isEmpty()) {
            for (Map.Entry<String,String> entry: mHeader.entrySet()) {
                builder.addHeader(entry.getKey(),entry.getValue());
            }
        }
        // 创建请求的Request 对象
        Request request = builder
                .url(mUrl)
                .build();
        // 在Okhttp中创建Call 对象，将request和Client进行绑定
        Call call = getOkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailure();
                LoggerUtil.d("onFailure :  "+e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                responseProcess(response);
            }
        });
    }

    /**
     * 拼接参数到 Url
     * */
    private static String appendParams(String url, Map<String, String> params) {
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
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.append("&" + entry.getKey()).append("=").append(encode(entry.getValue()));
        }
        return urlBuilder.toString();
    }

    /**
     * 设置编码
     * */
    private static String encode(String str) {
        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * post请求
     * */
    public void post() {
        FormBody.Builder formBody = new FormBody.Builder();
        if(mParams != null && !mParams.isEmpty()) {
            for (Map.Entry<String,String> entry: mParams.entrySet()) {
                formBody.add(entry.getKey(),entry.getValue());
            }
        }
        RequestBody form = formBody.build();
        // 添加请求头
        Request.Builder builder = new Request.Builder();
        if(mHeader != null && !mHeader.isEmpty()) {
            for (Map.Entry<String,String> entry: mHeader.entrySet()) {
                builder.addHeader(entry.getKey(),entry.getValue());
            }
        }
        // 创建请求的Request 对象
        final Request request = builder
                .post(form)
                .url(mUrl)
                .build();
        Call call = getOkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailure();
                LoggerUtil.d("onFailure :  "+e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                responseProcess(response);
            }
        });
    }

    /**
     * 处理OkHttp响应逻辑
     * @param response 请求回来的响应体
     * */
    private void responseProcess(Response response) {
        if (response != null && response.code() == 200) {
            if (response.body() != null) {
                try {
                    sendSuccess(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                    sendFailure();
                    LoggerUtil.d("onFailure :  "+e.getMessage());
                }
            } else {
                sendFailure();
                LoggerUtil.d("onFailure :  response.body() == null");
            }
        } else {
            sendFailure();
            LoggerUtil.d("onFailure : response == null or response.code() == "+response.code());
        }
    }

    /**
     * 回调成功-返回数据（主线程）
     * */
    private void sendSuccess(final String data) {
        if (mSuccess != null) {
            MainHandlerUtil.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    mSuccess.onSuccess(data);
                }
            });
        }
    }

    /**
     * 回调失败（主线程）
     * */
    private void sendFailure() {
        if (mFailure != null) {
            MainHandlerUtil.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    mFailure.onFailure();
                }
            });
        }
    }

    public static interface ISuccess {
        void onSuccess(String response);
    }

    public static interface IFailure {
        void onFailure();
    }
}
