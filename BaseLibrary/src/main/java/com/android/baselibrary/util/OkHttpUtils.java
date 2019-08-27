package com.android.baselibrary.util;

import com.android.baselibrary.util.log.LoggerUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
public class OkHttpUtils {

    /**
     * 初始化OkHttp实例
     */
    private static final class OkHttpHolder {
        private static final OkHttpClient.Builder BUILDER = new OkHttpClient.Builder();

        // OkHttp进行添加拦截器loggingInterceptor
        private static OkHttpClient.Builder addInterceptor() {
            // 日志显示级别
            HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.BODY;
            // 新建log拦截器
            HttpLoggingInterceptor loggingInterceptor =
                    new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    LoggerUtil.d("OkHttpClient", "OkHttp====Message:" + message);
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
     */
    public static OkHttpClient getOkHttpClient() {
        return OkHttpHolder.OK_HTTP_CLIENT;
    }

    private String mUrl;
    private final HashMap<String, String> mHeader;
    private final ISuccess mSuccess;
    private final IFailure mFailure;
    private final HashMap<String, String> mParams;

    /**
     * @param url     请求url
     * @param header  请求头
     * @param params  请求参数
     * @param success 请求成功回调
     * @param failure 请求失败回调
     */
    private OkHttpUtils(String url, HashMap<String, String> header
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
            this.params.putAll(params);
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

        public final OkHttpUtils build() {
            return new OkHttpUtils(url, header, params, success, failure);
        }
    }

    /**
     * get请求
     *
     * @param isNeedMainLooper 返回结果时是否需要在主线程中返回
     */
    public void get(final boolean isNeedMainLooper) {
        if (mParams != null && !mParams.isEmpty()) {
            mUrl = appendParams(mUrl, mParams);
        }
        // 添加请求头
        Request.Builder builder = new Request.Builder();
        if (mHeader != null && !mHeader.isEmpty()) {
            for (Map.Entry<String, String> entry : mHeader.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        LoggerUtil.d("url ====" + mUrl);
        // 创建请求的Request 对象
        Request request = builder
                .url(mUrl)
                .build();
        // 在Okhttp中创建Call 对象，将request和Client进行绑定
        Call call = getOkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (call.isCanceled()) {
                    LoggerUtil.d("call is cancel");
                } else {
                    sendFailure(isNeedMainLooper);
                }
                LoggerUtil.d("onFailure :  " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (call.isCanceled()) {
                    LoggerUtil.d("call is cancel");
                } else {
                    responseProcess(response, isNeedMainLooper);
                }
            }
        });
    }

    /**
     * 取消请求
     */
    public static void cancel() {
        getOkHttpClient().dispatcher().cancelAll();
    }

    /**
     * 拼接参数到 Url
     */
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
     */
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
     *
     * @param isNeedMainLooper 返回结果时是否需要在主线程中返回
     */
    public void post(final boolean isNeedMainLooper) {
        FormBody.Builder formBody = new FormBody.Builder();
        if (mParams != null && !mParams.isEmpty()) {
            for (Map.Entry<String, String> entry : mParams.entrySet()) {
                formBody.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody form = formBody.build();
        // 添加请求头
        Request.Builder builder = new Request.Builder();
        if (mHeader != null && !mHeader.isEmpty()) {
            for (Map.Entry<String, String> entry : mHeader.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
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
                if (call.isCanceled()) {
                    LoggerUtil.d("call is cancel");
                } else {
                    sendFailure(isNeedMainLooper);
                }

                LoggerUtil.d("onFailure :  " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (call.isCanceled()) {
                    LoggerUtil.d("call is cancel");
                } else {
                    responseProcess(response, isNeedMainLooper);
                }

            }
        });
    }

    /**
     * 处理OkHttp响应逻辑
     *
     * @param response 请求回来的响应体
     */
    private void responseProcess(Response response, boolean isNeedMainLooper) {
        if (response != null && response.code() == 200) {
            if (response.body() != null) {
                try {
                    sendSuccess(response.body().string(), isNeedMainLooper);
                } catch (IOException e) {
                    e.printStackTrace();
                    sendFailure(isNeedMainLooper);
                    LoggerUtil.d("onFailure :  " + e.getMessage());
                }
            } else {
                sendFailure(isNeedMainLooper);
                LoggerUtil.d("onFailure :  response.body() == null");
            }
        } else {
            sendFailure(isNeedMainLooper);
            LoggerUtil.d("onFailure : response == null or response.code() == " + response.code() + ",msg = " + response.message());
        }
    }

    /**
     * 回调成功-返回数据（）
     *
     * @param isNeedMainLooper 返回结果时是否需要在主线程中返回
     */
    private void sendSuccess(final String data, boolean isNeedMainLooper) {
        if (mSuccess != null) {
            if (isNeedMainLooper) {
                MainHandlerUtil.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        mSuccess.onSuccess(data);
                    }
                });
            } else {
                mSuccess.onSuccess(data);
            }

        }
    }

    /**
     * 回调失败（）
     *
     * @param isNeedMainLooper 返回结果时是否需要在主线程中返回
     */
    private void sendFailure(boolean isNeedMainLooper) {
        if (mFailure != null) {
            if (isNeedMainLooper) {
                MainHandlerUtil.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        mFailure.onFailure();
                    }
                });
            } else {
                mFailure.onFailure();
            }
        }
    }

    public interface ISuccess {
        void onSuccess(String response);
    }

    public interface IFailure {
        void onFailure();
    }
}
