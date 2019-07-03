package com.android.baselibrary.strategy.httpProcessor;

import android.content.Context;
import android.os.Handler;
import java.util.WeakHashMap;

import com.android.baselibrary.strategy.httpProcessor.net.RestClient;
import com.android.baselibrary.strategy.httpProcessor.callBack.HttpCallback;
import com.android.baselibrary.strategy.httpProcessor.net.callBack.IFailure;
import com.android.baselibrary.strategy.httpProcessor.net.callBack.ISuccess;

public class RetrofitProcessor implements IHttpProcessor {
    private Handler myHandler = null;

    public RetrofitProcessor() {
        myHandler = new Handler();
    }

    @Override
    public void post(Context context, String url, WeakHashMap<String, Object> params, final HttpCallback callback) {
        RestClient.builder()
                .url(url)
                .loader(context)
                .params(params)
                .success(new ISuccess() {
                    @Override
                    public void onSuccess(String response) {
                        callback.onSuccess(response);
                    }
                })
                .failure(new IFailure() {
                    @Override
                    public void onFailure() {
                        callback.onFailure("失败啦");
                    }
                })
                .build().post();
    }

    @Override
    public void get(Context context, String url, WeakHashMap<String, Object> params, HttpCallback callback) {
        RestClient.builder()
                .url(url)
                .params(params)
                .loader(context)
                .success(new ISuccess() {
                    @Override
                    public void onSuccess(String response) {
                        callback.onSuccess(response);
                    }
                }).build().get();

        // 第一种Rx
//        RestCreator.getRxRestService()
//                .get(url, params)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new ObserverCallBack<String>() {
//                    @Override
//                    public void onNext(String response) {
//                        callback.onSuccess(response);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//                });

        // 第二种Rx
//        RxRestClient.builder()
//                .url(url)
//                .params(params)
//                .build()
//                .get()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new ObserverCallBack<String>() {
//                    @Override
//                    public void onNext(String response) {
//                        callback.onSuccess(response);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//                });
    }
}