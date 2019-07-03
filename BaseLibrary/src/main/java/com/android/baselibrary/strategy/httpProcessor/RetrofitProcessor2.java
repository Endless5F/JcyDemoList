package com.android.baselibrary.strategy.httpProcessor;

import android.os.Handler;
import android.util.Log;

import java.util.WeakHashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import com.android.baselibrary.strategy.httpProcessor.callBack.ObserverCallBack;
import com.android.baselibrary.strategy.httpProcessor.http.RetrofitClient;
import com.android.baselibrary.strategy.httpProcessor.http.RetrofitCreator;
import com.android.baselibrary.strategy.httpProcessor.callBack.HttpCallback;
import com.android.baselibrary.strategy.httpProcessor.http.converter.ResultException;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.android.ActivityEvent;

public class RetrofitProcessor2 implements IHttpProcessor2 {
    private final String TAG = "RetrofitProcessor2" ;
    private Handler myHandler = null;

    public RetrofitProcessor2() {
        myHandler = new Handler();
    }

    @Override
    public void get(String url, WeakHashMap<String, Object> params, LifecycleProvider<ActivityEvent> provider, HttpCallback callback) {
        // 第一种Rx
        WeakHashMap<String, Object> weakHashMap = new WeakHashMap<>();
        weakHashMap.put("aaa", "123");
        RetrofitCreator.getRxRestService()
                .post("", weakHashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(provider.bindUntilEvent(ActivityEvent.DESTROY))// onDestroy取消订阅
                .subscribe(new ObserverCallBack<String>() {
                    @Override
                    public void onNext(String s) {
                        callback.onSuccess(s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        errorControl(e, callback);
                    }
                });
        // 第二种Rx
        RetrofitClient.builder()
                .url("")
                .params(params)
                .build()
                .post()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(provider.bindUntilEvent(ActivityEvent.DESTROY))// onDestroy取消订阅
                .subscribe(new ObserverCallBack<String>() {
                    @Override
                    public void onNext(String s) {
                        callback.onSuccess(s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        errorControl(e,callback);
                    }
                });
    }

    @Override
    public void post(String url, WeakHashMap<String, Object> params, LifecycleProvider<ActivityEvent> provider, HttpCallback callback) {
        // 第一种Rx
        WeakHashMap<String, Object> weakHashMap = new WeakHashMap<>();
        weakHashMap.put("aaa", "123");
        RetrofitCreator.getRxRestService()
                .post("", weakHashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ObserverCallBack<String>() {
                    @Override
                    public void onNext(String s) {
                        callback.onSuccess(s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        errorControl(e, callback);
                    }
                });
        // 第二种Rx
        RetrofitClient.builder()
                .url("")
                .params(params)
                .build()
                .post()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ObserverCallBack<String>() {
                    @Override
                    public void onNext(String s) {
                        callback.onSuccess(s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        errorControl(e, callback);
                    }
                });
    }

    private void errorControl(Throwable e, HttpCallback callback) {
        /**
         * 注：若遇到 Throwable    null...java.net.SocketTimeoutException  异常，则服务器接口出现问题，询问后台接口
         * */
        Log.e(TAG, "=== > Throwable    " + e.getMessage() + "  " + e.toString());
        //失败的时候调用-----一下可以忽略 直接 callBack.onFaild("请求失败");
        if (e instanceof ResultException) {
            //自定义的ResultException
            //由于返回200,300返回格式不统一的问题，自定义GsonResponseBodyConverter凡是300的直接抛异常
            //System.out.println("---------errorCode------->"+((ResultException) e).getErrCode());
            callback.onFailure(((ResultException) e).getMsg());
        } else {
            callback.onFailure(e.getMessage());
        }
    }
}