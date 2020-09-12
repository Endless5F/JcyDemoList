package com.android.baselibrary.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import java.util.Iterator;

/**
 * 跨进程共享数据：
 * 1. SharedPreferences：使用存储模式为MODE_MULTI_PROCESS的SharedPreferences
 * 来实现数据存储，各个进程都需要建立自己的SharedPreference实例，通过它来访问数据，系统机制保证数据的同步。不过这种方式不完全可靠，已经被官方弃用，新的Android
 * 版本已经不再支持。
 * 2. ContentProvider：ContentProvider
 * 是官方推荐应用间共享数据的方式，也是被大家最广泛使用的方式，由系统来保证进程间数据同步的安全性和可靠性，稳定可靠。ContentProvider
 * 提供了增删改查的接口，与数据库结合，相当于为其他进程提供了一个远程数据库，功能强大，只是实现上相当于定义了一套远程访问数据库的接口协议，稍显复杂。
 * 3. 第三方框架：常见的有github上的Tray
 * <p>
 * 本类使用SharedPreferences作支持的ContentProvider，来进行跨进程共享数据
 * <p>
 * 写入共享：GlobalProvider.save(context, PARAMETER_KEY, PARAMETER_VALUE);
 * 读取共享：GlobalProvider.getInt(context,PARAMETER_KEY,DEFAULT_VALUE_WHILE_NULL);
 * <p>
 * 注1：若需要复杂操作的共享数据，还是乖乖滴基于数据库根据自己的业务需求实现完整的ContentProvider吧！
 * 注2：若当前应用，是系统应用(拥有System权限)或者用于root权限，则可以使用系统属性SystemProperties,通过反射来设置写入自定义属性以及读取属性，相比SharedPreferences更优秀。
 */

public class GlobalProvider extends ContentProvider {

    public static final Uri AUTHORITY_URI = Uri.parse("content://[YOUR PACKAGE NAME]");
    public static final Uri CONTENT_URI = AUTHORITY_URI;

    public static final String PARAM_KEY = "key";

    public static final String PARAM_VALUE = "value";

    private final String DB_NAME = "global.sp";
    private SharedPreferences mStore;

    public static Cursor query(Context context, String... keys) {
        return context.getContentResolver().query(CONTENT_URI, keys, null, null, null);
    }

    public static String getString(Context context, String key) {
        return getString(context, key, null);
    }

    public static String getString(Context context, String key, String defValue) {
        Cursor cursor = query(context, key);
        String ret = defValue;
        if (cursor.moveToNext()) {
            ret = cursor.getString(0);
            if (TextUtils.isEmpty(ret)) {
                ret = defValue;
            }
        }
        cursor.close();
        return ret;
    }

    public static int getInt(Context context, String key, int defValue) {
        Cursor cursor = query(context, key);
        int ret = defValue;
        if (cursor.moveToNext()) {
            try {
                ret = cursor.getInt(0);
            } catch (Exception e) {

            }
        }
        cursor.close();
        return ret;
    }

    public static Uri save(Context context, ContentValues values) {
        return context.getContentResolver().insert(GlobalProvider.CONTENT_URI, values);
    }

    public static Uri save(Context context, String key, String value) {
        ContentValues values = new ContentValues(1);
        values.put(key, value);
        return save(context, values);
    }

    public static Uri remove(Context context, String key) {
        return save(context, key, null);
    }

    @Override
    public boolean onCreate() {
        mStore = getContext().getSharedPreferences(DB_NAME, Context.MODE_PRIVATE);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        int size = projection == null ? 0 : projection.length;
        if (size > 0) {
            String[] values = new String[size];
            for (int i = 0; i < size; i++) {
                values[i] = getValue(projection[i], null);
            }
            return createCursor(projection, values);
        }
        String key = uri.getQueryParameter(PARAM_KEY);
        String value = null;
        if (!TextUtils.isEmpty(key)) {
            value = getValue(key, null);
        }
        return createSingleCursor(key, value);
    }

    protected Cursor createSingleCursor(String key, String value) {
        MatrixCursor cursor = new MatrixCursor(new String[]{key}, 1);
        if (!TextUtils.isEmpty(value)) {
            cursor.addRow(new Object[]{value});
        }
        return cursor;
    }

    protected Cursor createCursor(String[] keys, String[] values) {
        MatrixCursor cursor = new MatrixCursor(keys, 1);
        cursor.addRow(values);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (values != null && values.size() > 0) {
            save(values);
        } else {
            String key = uri.getQueryParameter(PARAM_KEY);
            String value = uri.getQueryParameter(PARAM_VALUE);
            if (!TextUtils.isEmpty(key)) {
                save(key, value);
            }
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        String key = selection == null ? selection : uri.getQueryParameter(PARAM_KEY);
        if (!TextUtils.isEmpty(key)) {
            remove(key);
            return 1;
        }
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        if (values != null && values.size() > 0) {
            save(values);
            return values.size();
        }
        String key = uri.getQueryParameter(PARAM_KEY);
        String value = uri.getQueryParameter(PARAM_VALUE);
        if (!TextUtils.isEmpty(key)) {
            save(key, value);
            return 1;
        }
        return 0;
    }

    protected String getValue(String key, String defValue) {
        return mStore.getString(key, defValue);
    }

    protected void save(ContentValues values) {
        String key;
        String value;
        Iterator<String> iterator = values.keySet().iterator();
        SharedPreferences.Editor editor = mStore.edit();
        while (iterator.hasNext()) {
            key = iterator.next();
            value = values.getAsString(key);
            if (!TextUtils.isEmpty(key)) {
                if (value != null) {
                    editor.putString(key, value);
                } else {
                    editor.remove(key);
                }
            }
        }
        editor.commit();
    }

    protected void save(String key, String value) {
        SharedPreferences.Editor editor = mStore.edit();
        if (value != null) {
            editor.putString(key, value);
        } else {
            editor.remove(key);
        }
        editor.commit();
    }

    protected void remove(String key) {
        SharedPreferences.Editor editor = mStore.edit();
        editor.remove(key);
        editor.commit();
    }

}