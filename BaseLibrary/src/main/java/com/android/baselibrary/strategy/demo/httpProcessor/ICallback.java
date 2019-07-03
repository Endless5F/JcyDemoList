package com.android.baselibrary.strategy.demo.httpProcessor;

/**
 * 回调接口
 */
public interface ICallback {
    void onSuccess(String result);
    void onFailure(String e);
}
