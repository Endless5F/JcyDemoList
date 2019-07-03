package com.android.baselibrary.strategy.dbProcessor;

/**
 * 回调接口
 */
public interface IDbCallback<T> {
    void onResult(T result);
}
