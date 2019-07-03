package com.android.baselibrary.strategy.dbProcessor;

import android.os.Handler;

import com.android.baselibrary.util.ThreadPoolUtil;

import org.litepal.LitePal;
import org.litepal.tablemanager.Connector;

import java.util.List;

/**
 * 数据库真实处理类，更换数据库，只需更换该类，及实现相应方法
 */
public class LitepalProcessor implements IDbProcessor {
    private static final String TAG = "LitepalProcessor";

    private Handler myHandler = null;

    public LitepalProcessor() {
        // litepal创建数据库
        Connector.getDatabase();
        myHandler = new Handler();
    }

    @Override
    public <T, R> void add(final T table, final IDbCallback<R> callback) {
        deleteAll(table);
        ThreadPoolUtil.getInstance().execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public <T> void deleteAll(final T table) {
        final Class tableName = getTableName(table);
        ThreadPoolUtil.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                LitePal.deleteAll(tableName);
            }
        });
    }

    @Override
    public <T, R> void update(final T table, final IDbCallback<R> callback) {
        final Class tableName = getTableName(table);
    }

    @Override
    public <T, R> void queryAll(final T table, final IDbCallback<R> callback) {
        final Class tableName = getTableName(table);
        ThreadPoolUtil.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final List all = LitePal.findAll(tableName, true);
                callback.onResult((R) all);
            }
        });
    }

    @Override
    public <T, R> void queryFrist(final T table, final IDbCallback<R> callback) {
        final Class tableName = getTableName(table);
        ThreadPoolUtil.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                Object first = LitePal.findFirst(tableName);
                callback.onResult((R) first);
            }
        });
    }

    private <T> Class getTableName(T table) {
        Class tableName;
        if (table instanceof Class) {
            tableName = (Class) table;
        } else {
            tableName = table.getClass();
        }
        return tableName;
    }
}