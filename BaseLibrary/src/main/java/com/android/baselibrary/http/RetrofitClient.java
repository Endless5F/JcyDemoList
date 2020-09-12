package com.android.baselibrary.http;

import android.content.Context;

import com.android.baselibrary.base.globalConf.AppConf;
import com.android.baselibrary.http.callBack.ObserverCallBack;
import com.android.baselibrary.http.common.ResultException;
import com.android.baselibrary.http.download.DownloadListener;
import com.android.baselibrary.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * // 用法，建议直接使用RetrofitCreator.getRxRestService()
 *  RetrofitClient.builder()
 *      .url("")
 *      .params(params)
 *      .build()
 *      .post()
 *      .subscribeOn(Schedulers.io())
 *      .observeOn(AndroidSchedulers.mainThread())
 *      .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner, Lifecycle.Event.ON_DESTROY)))
 *      .subscribe(new ObserverCallBack<String>() {
 *          @Override
 *          public void onNext(String s) {
 *              callback.onSuccess(s);
 *          }
 *
 *          @Override
 *          public void onError(Throwable e) {
 *              errorControl(e, callback);
 *          }
 *                 });
 */
public class RetrofitClient {
    private static final WeakHashMap<String, Object> PARAMS = new WeakHashMap<>();
    private final String URL;
    private final RequestBody BODY;
    private final File FILE;
    private final Context CONTEXT;

    RetrofitClient(String url,
                   Map<String, Object> params,
                   RequestBody body,
                   File file,
                   Context context) {
        this.URL = url;
        PARAMS.putAll(params);
        this.BODY = body;
        this.FILE = file;
        this.CONTEXT = context;
    }

    public static RetrofitClientBuilder builder() {
        return new RetrofitClientBuilder();
    }

    public final Observable<String> get() {
        return request(HttpMethod.GET);
    }

    public final Observable<String> post() {
        if (BODY == null) {
            return request(HttpMethod.POST);
        } else {
            if (!PARAMS.isEmpty()) {
                throw new RuntimeException("params must be null!");
            }
            return request(HttpMethod.POST_RAW);
        }
    }

    public final Observable<String> put() {
        if (BODY == null) {
            return request(HttpMethod.PUT);
        } else {
            if (!PARAMS.isEmpty()) {
                throw new RuntimeException("params must be null!");
            }
            return request(HttpMethod.PUT_RAW);
        }
    }

    public final Observable<String> delete() {
        return request(HttpMethod.DELETE);
    }

    public final Observable<String> upload() {
        return request(HttpMethod.UPLOAD);
    }

    public final void download(String url, File saveFile, DownloadListener listener) {
        RetrofitCreator.getDownloadService(listener)
                .download(url)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(responseBody -> responseBody.byteStream())
                .observeOn(Schedulers.computation())
                .doOnNext(inputStream -> {
                    try {
                        FileUtils.writeFile(inputStream, saveFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new ResultException(AppConf.DOWNLOAD_ERROR,e.getMessage());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ObserverCallBack<InputStream>() {
                    @Override
                    public void onNext(InputStream inputStream) {
                        // 下载进度在listener中
                    }

                    @Override
                    public void onError(Throwable e) {
                        // 下载错误
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        // 下载完成
                    }
                });
    }

    private Observable<String> request(HttpMethod method) {
        final APIService service = RetrofitCreator.getRxRestService();
        Observable<String> observable = null;

        switch (method) {
            case GET:
                observable = service.get(URL, PARAMS);
                break;
            case POST:
                observable = service.post(URL, PARAMS);
                break;
            case POST_RAW:
                observable = service.postRaw(URL, BODY);
                break;
            case PUT:
                observable = service.put(URL, PARAMS);
                break;
            case PUT_RAW:
                observable = service.putRaw(URL, BODY);
                break;
            case DELETE:
                observable = service.delete(URL, PARAMS);
                break;
            case UPLOAD:
                final RequestBody requestBody =
                        RequestBody.create(MediaType.parse(MultipartBody.FORM.toString()), FILE);
                final MultipartBody.Part body =
                        MultipartBody.Part.createFormData("file", FILE.getName(), requestBody);
                observable = service.upload(URL, body);
                break;
            default:
                break;
        }

        return observable;
    }

    public static class RetrofitClientBuilder {
        private static final WeakHashMap<String, Object> PARAMS = new WeakHashMap<>();
        private String mUrl = null;
        private RequestBody mBody = null;
        private Context mContext = null;
        private File mFile = null;

        RetrofitClientBuilder() {
        }

        public final RetrofitClientBuilder url(String url) {
            this.mUrl = url;
            return this;
        }

        public final RetrofitClientBuilder params(WeakHashMap<String, Object> params) {
            PARAMS.putAll(params);
            return this;
        }

        public final RetrofitClientBuilder params(String key, Object value) {
            PARAMS.put(key, value);
            return this;
        }

        public final RetrofitClientBuilder file(File file) {
            this.mFile = file;
            return this;
        }

        public final RetrofitClientBuilder file(String file) {
            this.mFile = new File(file);
            return this;
        }

        public final RetrofitClientBuilder raw(String raw) {
            this.mBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), raw);
            return this;
        }

        public final RetrofitClient build() {
            return new RetrofitClient(mUrl, PARAMS, mBody, mFile, mContext);
        }
    }
}