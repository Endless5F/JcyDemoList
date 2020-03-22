package com.android.httpserver.utils;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.github.lizhangqu.coreprogress.ProgressHelper;
import io.github.lizhangqu.coreprogress.ProgressUIListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * @author jiaochengyun.ex
 * <p>
 * implementation 'com.squareup.okhttp3:okhttp:3.12.0'
 * implementation 'com.squareup.okhttp3:logging-interceptor:3.10.0'
 */
public class OkHttpUtils {

    /**
     * 初始化OkHttp实例
     */
    private static final class OkHttpHolder {
        private static final OkHttpClient.Builder BUILDER = new OkHttpClient.Builder();

        /**
         * OkHttp进行添加拦截器loggingInterceptor
         *
         * @return
         */
        private static OkHttpClient.Builder addInterceptor() {
            // 日志显示级别
//            HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.BODY;
            // 若上传大文件时，需要使用这个级别，否则容易出现oom
            HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.HEADERS;

            // 新建log拦截器
            HttpLoggingInterceptor loggingInterceptor =
                    new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                        @Override
                        public void log(String message) {
//                    LogUtils.i("OkHttp====Message:" + message);
                        }
                    });
            loggingInterceptor.setLevel(level);
            BUILDER.addInterceptor(loggingInterceptor);
            return BUILDER;
        }

        /**
         * 定制OkHttp
         */
        private static final OkHttpClient OK_HTTP_CLIENT = addInterceptor()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
//                .sslSocketFactory(SSLSocketClient.getSSLSocketFactory())
//                .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                .build();
    }

    /**
     * 获取OkHttp实例
     */
    private static OkHttpClient getOkHttpClient() {
        return OkHttpHolder.OK_HTTP_CLIENT;
    }

    private String mUrl;
    private final HashMap<String, String> mHeader;
    private final HashMap<String, String> mParams;
    private final ISuccess mSuccess;
    private final IFailure mFailure;
    /**
     * 上传文件，支持多文件
     */
    private List<File> mFiles;
    private final IProgress mProgress;
    /**
     * 下载路径
     */
    private String mPath;
    /**
     * 是否支持断点续传
     */
    private boolean mBreakPoint;
    private IDownLoad mDownload;

    /**
     * @param url      请求url
     * @param header   请求头
     * @param params   请求参数
     * @param success  请求成功回调
     * @param failure  请求失败回调
     * @param files    上传文件路径(文件上传专用)
     * @param progress
     */
    private OkHttpUtils(String url, HashMap<String, String> header
            , HashMap<String, String> params, ISuccess success
            , IFailure failure, List<File> files, IProgress progress, String path,
                        boolean breakpoint,
                        IDownLoad download) {
        mUrl = url;
        mHeader = header;
        mParams = params;
        mSuccess = success;
        mFailure = failure;
        mFiles = files;
        mProgress = progress;
        mPath = path;
        mBreakPoint = breakpoint;
        mDownload = download;
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
        private List<File> files;
        private IProgress progress;
        private String path;
        private boolean breakpoint;
        private IDownLoad download;

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
            this.params.put(key, value);
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

        public final OkHttpUtilBuilder file(File file) {
            files = new ArrayList<>();
            this.files.add(file);
            return this;
        }

        public final OkHttpUtilBuilder files(List<File> files) {
            this.files = files;
            return this;
        }

        public final OkHttpUtilBuilder filePath(String filePath) {
            File file = new File(filePath);
            // 若文件存在并且该file是文件(非文件夹)
            if (file.exists() && file.isFile()) {
                files = new ArrayList<>();
                this.files.add(file);
            }
            return this;
        }

        public final OkHttpUtilBuilder progress(IProgress progress) {
            this.progress = progress;
            return this;
        }

        public final OkHttpUtilBuilder path(String path) {
            this.path = path;
            return this;
        }

        public final OkHttpUtilBuilder breakpoint(boolean breakpoint) {
            this.breakpoint = breakpoint;
            return this;
        }

        public final OkHttpUtilBuilder download(IDownLoad download) {
            this.download = download;
            return this;
        }

        public final OkHttpUtils build() {
            return new OkHttpUtils(url, header, params, success, failure, files, progress, path,
                    breakpoint, download);
        }
    }

    /**
     * 设置编码
     */
    private String encode(String str) {
        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * 取消请求
     */
    public static void cancel() {
        getOkHttpClient().dispatcher().cancelAll();
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
        LogUtils.i("url ==== " + mUrl);
        // 创建请求的Request 对象
        Request request = builder
                .url(mUrl)
                .build();
        callRequest(request, isNeedMainLooper);
    }

    /**
     * 拼接参数到 Url
     */
    private String appendParams(String url, Map<String, String> params) {
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
        int i = 0;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (i == 0) {
                urlBuilder.append(entry.getKey()).append("=").append(encode(entry.getValue()));
            } else {
                urlBuilder.append("&").append(entry.getKey()).append("=").append(encode(entry.getValue()));
            }
            i++;
        }
        return urlBuilder.toString();
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
        callRequest(request, isNeedMainLooper);
    }

    /**
     * upload 文件上传
     *
     * @param isNeedMainLooper 返回结果时是否需要在主线程中返回
     *                         <p>
     *                         <p>
     *                         RequestBody
     *                         String转RequestBody：MediaType.parse("application/json;charset=utf-8")
     *                         File转RequestBody：MediaType.parse("application/json;charset=utf-8")
     *                         <p>
     *                         multipart/form-data：
     *                         1、既可以提交普通键值对，也可以提交(多个)文件键值对。
     *                         2、HTTP规范中的Content-Type不包含此类型，只能用在POST提交方式下，属于http客户端(浏览器、java
     *                         httpclient)的扩展
     *                         3、通常在浏览器表单中，或者http客户端(java httpclient)中使用。
     *                         页面中，form的enctype是multipart/form-data,
     *                         提交时，content-type也是multipart/form-data。
     *                         <p>
     *                         application/octet-stream：
     *                         1、只能提交二进制，而且只能提交一个二进制，如果提交文件的话，只能提交一个文件,后台接收参数只能有一个，而且只能是流（或者字节数组）
     *                         2、属于HTTP规范中Content-Type的一种
     *                         3、很少使用
     *                         <p>
     *                         application/x-www-form-urlencoded
     *                         1、不属于http
     *                         content-type规范，通常用于浏览器表单提交，数据组织格式:name1=value1&name2=value2,
     *                         post时会放入http body，get时，显示在在地址栏。
     *                         2、所有键与值，都会被urlencoded
     */
    public void postFile(final boolean isNeedMainLooper) {
        if (mFiles == null) {
            LogUtils.i("mFiles == null");
            if (mFailure != null) {
                mFailure.onFailure();
            }
            return;
        }
        // 1
        // 参数请求体
//        FormBody paramsBody = new FormBody.Builder()
//                .add("fileName", encode(mFile.getName()))
//                .build();
        // 2
        // 文件请求体
//        final RequestBody body =
//                RequestBody.create(MediaType.parse(MultipartBody.FORM.toString()), mFile);


        MediaType mutilpart_form_data = MediaType.parse("multipart/form-data; charset=utf-8");

        // 混合参数和文件请求
        MultipartBody.Builder builder = new MultipartBody.Builder()
                // setType方法至关重要，该参数表明了整体按照什么类型传递
                .setType(MultipartBody.FORM);
        // 多文件上传
        for (File file : mFiles) {
            if (file.exists() && file.isFile()) {
                LogUtils.i("fileName：" + file.getName());
                builder.addFormDataPart("fileName", encode(file.getName()))
//                .addPart(Headers.of("Content-Disposition", "form-data; name=\"params\""),
//                paramsBody) // 1 类似于上一行
                        .addPart(Headers.of("Content-Disposition", "form-data; name=\"file\"; " +
                                "filename=\"upload\""), RequestBody.create(mutilpart_form_data,
                                file));
            } else {
                LogUtils.d("!file.exists() || !file.isFile()");
            }
        }
        MultipartBody requestBody = builder.build();

        // 3
        // 进度请求体，暂时不可用
//        RequestBody requestBody1 = new MultipartBody.Builder()
//                .addPart(new ProgressRequestBody(requestBody, mProgress))
//                .build();

        // wrap your original request body with progress
        // 上传进度需要依赖库：implementation 'io.github.lizhangqu:coreprogress:1.0.2'
        RequestBody requestBody1 = ProgressHelper.withProgress(requestBody,
                new ProgressUIListener() {

                    //if you don't need this method, don't override this methd. It isn't an abstract
                    // method, just an empty method.
                    @Override
                    public void onUIProgressStart(long totalBytes) {
                        super.onUIProgressStart(totalBytes);
                        Log.e("TAG", "onUIProgressStart:" + totalBytes);
                    }

                    @Override
                    public void onUIProgressChanged(long numBytes, long totalBytes, float percent,
                                                    float speed) {
                        Log.e("TAG", "=============start===============");
                        Log.e("TAG", "numBytes:" + numBytes);
                        Log.e("TAG", "totalBytes:" + totalBytes);
                        Log.e("TAG", "percent:" + percent);
                        Log.e("TAG", "speed:" + speed);
                        Log.e("TAG", "============= end ===============");
//                uploadProgress.setProgress((int) (100 * percent));
//                uploadInfo.setText("numBytes:" + numBytes + " bytes" + "\ntotalBytes:" +
//                totalBytes + " bytes" + "\npercent:" + percent * 100 + " %" + "\nspeed:" +
//                speed * 1000 / 1024 / 1024 + "  MB/秒");
                    }

                    //if you don't need this method, don't override this methd. It isn't an abstract
                    // method, just an empty method.
                    @Override
                    public void onUIProgressFinish() {
                        super.onUIProgressFinish();
                        Log.e("TAG", "onUIProgressFinish:");
//                Toast.makeText(getApplicationContext(), "结束上传", Toast.LENGTH_SHORT).show();
                    }

                });

        Request request = new Request.Builder()
                .url(mUrl)
                .post(requestBody1)
                .build();

        callRequest(request, isNeedMainLooper);
    }

    /**
     * 通话请求
     *
     * @param request
     * @param isNeedMainLooper
     */
    private void callRequest(Request request, boolean isNeedMainLooper) {
        // 在Okhttp中创建Call 对象，将request和Client进行绑定
        Call call = getOkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (call.isCanceled()) {
                    LogUtils.i("call is cancel");
                } else {
                    sendFailure(isNeedMainLooper);
                }

                LogUtils.d("onFailure :  " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (call.isCanceled()) {
                    LogUtils.i("call is cancel");
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
                    sendSuccess(Objects.requireNonNull(response.body()).string(), isNeedMainLooper);
                } catch (IOException e) {
                    e.printStackTrace();
                    sendFailure(isNeedMainLooper);
                    LogUtils.d("onFailure :  " + e.getMessage());
                }
            } else {
                sendFailure(isNeedMainLooper);
                LogUtils.d("onFailure :  response.body() == null");
            }
        } else {
            sendFailure(isNeedMainLooper);
            if (response != null) {
                LogUtils.d("onFailure : response == null or response.code() == " + response.code() + ",msg = " + response.message());
            }
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
                MainHandlerUtils.getInstance().post(new Runnable() {
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
                MainHandlerUtils.getInstance().post(new Runnable() {
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

    private long startPosition;
    private Call call;
    private long preDownloadSize = -1;
    private long currentDownloadSize = 0;

    /**
     * 下载整个过程都是在子线程中执行
     */
    public void download() {
        if (TextUtils.isEmpty(mUrl)) {
            LogUtils.i("TextUtils.isEmpty(mUrl)");
            downloadError();
            return;
        }
        File parentFile = initDownload(mPath);
        if (parentFile == null || !parentFile.exists()) {
            LogUtils.i("parentFile == null || !parentFile.exists()");
            downloadError();
            return;
        }


        File downloadFile = null;
        if (mUrl.contains(".")) {
            String typename = mUrl.substring(mUrl.lastIndexOf(".") + 1);
            if (mUrl.contains("/")) {
                String filename = mUrl.substring(mUrl.lastIndexOf("/") + 1, mUrl.lastIndexOf("."));
                String fn = filename + "." + typename;
                downloadFile = new File(parentFile, fn);
                LogUtils.d("downloadFileL：" + downloadFile.toString());
            }
        }
        startPosition = 0;
        if (downloadFile != null && downloadFile.exists()) {
            startPosition = downloadFile.length();
            LogUtils.i("Length start: " + startPosition + "----");
        }

        Request.Builder builder = new Request.Builder();
        if (mBreakPoint) {
            // 断点续传
            builder.addHeader("RANGE", "bytes=" + startPosition + "-");
        }
        final Request request = builder.url(mUrl)
                .build();

        final Request request1 = new Request.Builder()
                .url(mUrl)
                .build();

        if (call == null) {
            call = getOkHttpClient().newCall(request);
        }
        if (!call.isExecuted()) {
            File finalDownloadFile = downloadFile;
            call.enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    downloadError();
                    LogUtils.e("下载失败啦。。。。。。。。。。。。。。。。。。。。。。");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    long l =
                            Objects.requireNonNull(getOkHttpClient().newCall(request1).execute().body()).contentLength();
                    LogUtils.i("Length l: " + l + "----");
                    if (l != 0 && l == startPosition) {
                        mDownload.finishDownload(Objects.requireNonNull(finalDownloadFile).getAbsolutePath());
                        return;
                    } else {
                        if (!mBreakPoint && finalDownloadFile != null && finalDownloadFile.exists()) {
                            finalDownloadFile.delete();
                        }
                    }
                    mDownload.startDownload(l);
                    ResponseBody body = response.body();
                    long totalLength = Objects.requireNonNull(body).contentLength() + startPosition;
                    LogUtils.i("Length content: " + body.contentLength() + "----");
                    LogUtils.i("Length total: " + totalLength + "----");
                    InputStream is = body.byteStream();
                    byte[] bytes = new byte[5120];
                    int len = 0;
                    long totalNum = startPosition;
                    int flag = 0;
                    RandomAccessFile raf = new RandomAccessFile(finalDownloadFile, "rwd");
                    try {
                        while ((len = is.read(bytes, 0, bytes.length)) != -1) {
                            raf.seek(totalNum);
                            raf.write(bytes, 0, len);
                            totalNum += len;
                            flag++;
                            currentDownloadSize = totalNum;
                            if (preDownloadSize != currentDownloadSize) {
                                preDownloadSize = currentDownloadSize;
                            }
                            // 防止频繁给通知栏发消息引起崩溃问题，每100次，即大约0.5Mb更新进度
                            if (flag == 100) {
                                flag = 0;
                                mDownload.downloadProgress(totalNum);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        downloadError();
                        LogUtils.i("断网啦:  111111111111111111111111111111");
                        return;
                    }
                    if (l == totalNum) {
                        mDownload.finishDownload(Objects.requireNonNull(finalDownloadFile).getAbsolutePath());
                    } else {
                        mDownload.downloadError();
                    }
                    body.close();
                }
            });
        }
    }

    /**
     * 初始化下载父路径
     *
     * @return
     */
    private File initDownload(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        String fileAbsolutePath = file.getAbsolutePath();
        LogUtils.d(fileAbsolutePath);
        return file;
    }

    /**
     * 下载出错
     */
    private void downloadError() {
        if (mFailure != null) {
            mFailure.onFailure();
        }
        if (mDownload != null) {
            mDownload.downloadError();
        }
    }

    /**
     * 停止下载
     */
    public void stopDownload() {
        if (call != null && call.isExecuted()) {
            call.cancel();
            call = null;
        }
    }

    public interface ISuccess {
        /**
         * 成功回调
         *
         * @param response
         */
        void onSuccess(String response);
    }

    public interface IFailure {
        /**
         * 失败回调
         */
        void onFailure();
    }

    public interface IProgress {
        /**
         * 回调进度
         *
         * @param totalBytes     总字节数
         * @param remainingBytes 剩余字节数
         * @param done           是否上传完成
         *                       <p>
         *                       System.out.print((totalBytes - remainingBytes) * 100 /
         *                       totalBytes + "%");
         */
        void onProgress(long totalBytes, long remainingBytes, boolean done);
    }

    public interface IDownLoad {
        void startDownload(long fileLength);

        void pauseDownload();

        void finishDownload(String path);

        void downloadProgress(long progress);

        void downloadError();
    }

    /**
     * 包装的请求体，处理文件上传进度
     * User:lizhangqu(513163535@qq.com)
     * Date:2015-09-02
     * Time: 17:15
     */
    class ProgressRequestBody extends RequestBody {

        private final RequestBody requestBody;
        private final IProgress progressListener;
        private BufferedSink bufferedSink;

        /**
         * 构造函数，赋值
         *
         * @param requestBody      待包装的请求体
         * @param progressListener 回调接口
         */
        public ProgressRequestBody(RequestBody requestBody, IProgress progressListener) {
            this.requestBody = requestBody;
            this.progressListener = progressListener;
        }

        /**
         * 重写调用实际的响应体的contentType
         *
         * @return MediaType
         */
        @Override
        public MediaType contentType() {
            return requestBody.contentType();
        }

        /**
         * 重写调用实际的响应体的contentLength
         *
         * @return contentLength
         * @throws IOException 异常
         */
        @Override
        public long contentLength() throws IOException {
            return requestBody.contentLength();
        }

        /**
         * 重写进行写入
         *
         * @param sink BufferedSink
         * @throws IOException 异常
         */
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            if (bufferedSink == null) {
                bufferedSink = Okio.buffer(sink(sink));
            }
            requestBody.writeTo(bufferedSink);
            bufferedSink.flush();
        }

        /**
         * 写入，回调进度接口
         *
         * @param sink Sink
         * @return Sink
         */
        private Sink sink(Sink sink) {
            return new ForwardingSink(sink) {
                long bytesWritten = 0L;
                long contentLength = 0L;

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    super.write(source, byteCount);
                    if (contentLength == 0) {
                        contentLength = contentLength();
                    }
                    bytesWritten += byteCount;
                    if (progressListener != null) {
                        progressListener.onProgress(bytesWritten, contentLength,
                                bytesWritten == contentLength);
                    }
                }
            };
        }
    }

    /*
     * Android之让代码跑在主线程(无context上下文)的封装
     */
    /*public class MainHandlerUtils extends Handler {

        private static volatile com.example.test.utils.MainHandlerUtils mInstance;

        private MainHandlerUtils() {
            super(Looper.getMainLooper());
        }

        public static com.example.test.utils.MainHandlerUtils getInstance() {
            if (mInstance == null) {
                synchronized (com.example.test.utils.MainHandlerUtils.class) {
                    if (mInstance == null) {
                        mInstance = new com.example.test.utils.MainHandlerUtils();
                    }
                }
            }
            return mInstance;
        }
    }*/
}
