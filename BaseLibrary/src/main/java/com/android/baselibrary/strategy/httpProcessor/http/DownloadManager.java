package com.android.baselibrary.strategy.httpProcessor.http;

import android.util.Log;

import com.android.baselibrary.base.globalConf.AppConf;
import com.android.baselibrary.strategy.httpProcessor.http.converter.ResponseConverterFactory;
import com.android.baselibrary.strategy.httpProcessor.http.converter.ResultException;
import com.android.baselibrary.strategy.httpProcessor.http.download.DownloadProgressInterceptor;
import com.android.baselibrary.strategy.httpProcessor.http.download.DownloadProgressListener;
import com.android.baselibrary.util.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * Created by JokAr on 16/7/5.
 */
public class DownloadManager {
    private static final String TAG = "DownloadManager";
    private static final int DEFAULT_TIMEOUT = 15;
    public Retrofit retrofit;


    public DownloadManager(DownloadProgressListener listener) {

        DownloadProgressInterceptor interceptor = new DownloadProgressInterceptor(listener);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .retryOnConnectionFailure(true)
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();


        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("http://download.fir.im/v2/app/install/")
                .addConverterFactory(ResponseConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public void downloadAPK(String url, final File file, DisposableObserver disposableObserver) {
        Log.d(TAG, "downloadAPK: " + url);
        retrofit.create(APIService.class)
                .download(url)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(responseBody -> responseBody.byteStream())
                .observeOn(Schedulers.computation())
                .doOnNext(inputStream -> {
                    try {
                        FileUtil.writeFile(inputStream, file);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new ResultException(AppConf.DOWNLOAD_ERROR,e.getMessage());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(disposableObserver);
    }
}
