package com.android.baselibrary.strategy.httpProcessor.callBack;

/**
 * 回调接口
 */
public interface ICallback {
    void onSuccess(String result);
    void onFailure(String e);
}
