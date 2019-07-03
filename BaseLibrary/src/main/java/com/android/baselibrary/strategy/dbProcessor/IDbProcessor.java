package com.android.baselibrary.strategy.dbProcessor;

/**
 * 网络抽象层接口
 */
public interface IDbProcessor {

    <T, R> void add(T table, final IDbCallback<R> callback);

    <T> void deleteAll(T table);

    <T, R> void update(T table, final IDbCallback<R> callback);

    <T, R> void queryAll(T table, final IDbCallback<R> callback);

    <T, R> void queryFrist(T table, final IDbCallback<R> callback);
}

