package com.android.baselibrary.strategy.dbProcessor;

import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * 策略模式 封装数据库操作类
 */
public class DbHelper implements IDbProcessor {
    private static volatile DbHelper instance;
    // 扩展，弱引用，防止内存泄漏
    private static WeakReference<Context> mContext;

    private DbHelper() {
    }

    public static DbHelper obtain(Context context) {
        mContext = new WeakReference<>(context);
        if (instance == null) {
            synchronized (DbHelper.class) {
                if (instance == null) {
                    instance = new DbHelper();
                }
            }
        }
        return instance;
    }

    private static IDbProcessor mDbProcessor = null;

    public static void init(IDbProcessor dbProcessor) {
        mDbProcessor = dbProcessor;
    }

    @Override
    public <T, R> void add(T table, IDbCallback<R> callback) {
        mDbProcessor.add(table, callback);
    }

    @Override
    public <T> void deleteAll(T table) {
        mDbProcessor.deleteAll(table);
    }

    @Override
    public <T, R> void update(T table, IDbCallback<R> callback) {

    }

    @Override
    public <T, R> void queryAll(T table, IDbCallback<R> callback) {
        mDbProcessor.queryAll(table, callback);
    }

    @Override
    public <T, R> void queryFrist(T table, IDbCallback<R> callback) {
        mDbProcessor.queryFrist(table, callback);
    }
}
