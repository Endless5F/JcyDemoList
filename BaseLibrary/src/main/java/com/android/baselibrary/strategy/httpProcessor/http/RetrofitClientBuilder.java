package com.android.baselibrary.strategy.httpProcessor.http;

import android.content.Context;
import com.android.baselibrary.strategy.httpProcessor.net.RestCreator;
import com.android.baselibrary.ui.loader.LoaderStyle;

import java.io.File;
import java.util.WeakHashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class RetrofitClientBuilder {

    private static final WeakHashMap<String, Object> PARAMS = RestCreator.getParams();
    private String mUrl = null;
    private RequestBody mBody = null;
    private Context mContext = null;
    private LoaderStyle mLoaderStyle = null;
    private File mFile = null;

    RetrofitClientBuilder() { }

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

    public final RetrofitClientBuilder loader(Context context, LoaderStyle style) {
        this.mContext = context;
        this.mLoaderStyle = style;
        return this;
    }

    public final RetrofitClientBuilder loader(Context context) {
        this.mContext = context;
        this.mLoaderStyle = LoaderStyle.BallClipRotatePulseIndicator;
        return this;
    }

    public final RetrofitClient build() {
        return new RetrofitClient(mUrl, PARAMS, mBody, mFile,mLoaderStyle, mContext);
    }
}
