package com.android.baselibrary.strategy.demo.httpProcessor;

//import com.android.baselibrary.GlobalApplication;
//
//import org.xutils.common.Callback;
//import org.xutils.http.RequestParams;
//import org.xutils.x;

import java.util.Map;

public class XUtilsProcessor implements IHttpProcessor{
//    public XUtilsProcessor(GlobalApplication app){
//        x.Ext.init(app);
//    }

    @Override
    public void post(String url, Map<String, Object> params, final HttpCallback callback) {
//        RequestParams requestParams  = new RequestParams(url);
//        x.http().post(requestParams, new Callback.CommonCallback<String>() {
//            @Override
//            public void onSuccess(String result) {
//                callback.onSuccess(result);
//            }
//
//            @Override
//            public void onError(Throwable ex, boolean isOnCallback) {
//                callback.onFailure(ex.getMessage());
//            }
//
//            @Override
//            public void onCancelled(CancelledException cex) {
//
//            }
//
//            @Override
//            public void onFinished() {
//
//            }
//        });
    }

    @Override
    public void get(String url, Map<String, Object> params, HttpCallback callback) {

    }
}