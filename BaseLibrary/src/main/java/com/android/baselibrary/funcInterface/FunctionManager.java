package com.android.baselibrary.funcInterface;

import android.text.TextUtils;

import com.android.baselibrary.util.log.LoggerUtil;

import java.util.HashMap;
import java.util.Map;

public class FunctionManager {

    private static FunctionManager instance;

    private Map<String, FunctionNoParamNoResult> mNoParamNoResultMap;
    private Map<String, FunctionNoParamHasResult> mNoParamHasResultMap;
    private Map<String, FunctionHasParamNoResult> mHasParamNoResultMap;
    private Map<String, FunctionHasParamHasResult> mHasParamHasResultMap;

    private FunctionManager() {
        mNoParamNoResultMap = new HashMap<>();
        mNoParamHasResultMap = new HashMap<>();
        mHasParamNoResultMap = new HashMap<>();
        mHasParamHasResultMap = new HashMap<>();
    }

    public static FunctionManager getInstance() {
        if (instance == null) {
            synchronized (FunctionManager.class) {
                if (instance == null) {
                    instance = new FunctionManager();
                }
            }
        }
        return instance;
    }

    /**
     * 添加无参无返回值方法到函数管理类
     */
    public void addFunction(FunctionNoParamNoResult function) {
        mNoParamNoResultMap.put(function.functionName, function);
    }

    /**
     * 根据functionName执行方法
     */
    public void invokeFunction(String functionName) {
        if (TextUtils.isEmpty(functionName)) {
            return;
        }
        if (mNoParamNoResultMap != null) {
            FunctionNoParamNoResult f = mNoParamNoResultMap.get(functionName);
            if (f != null) {
                f.function();
            } else {
                LoggerUtil.d("没有找到该方法：" + functionName);
            }
        }
    }

    /**
     * 添加无参有返回值方法到函数管理类
     */
    public void addFunction(FunctionNoParamHasResult function) {
        mNoParamHasResultMap.put(function.functionName, function);
    }

    /**
     * 根据functionName执行方法
     */
    public <R> R invokeFunction(String functionName, Class<R> r) {
        if (TextUtils.isEmpty(functionName)) {
            return null;
        }
        if (mNoParamHasResultMap != null) {
            FunctionNoParamHasResult f = mNoParamHasResultMap.get(functionName);
            if (f != null) {
                if (r != null) {
                    return r.cast(f.function());
                }
            } else {
                LoggerUtil.d("没有找到该方法：" + functionName);
            }
        }
        return null;
    }

    /**
     * 添加有参无返回值方法到函数管理类
     */
    public void addFunction(FunctionHasParamNoResult function) {
        mHasParamNoResultMap.put(function.functionName, function);
    }

    /**
     * 根据functionName执行方法
     */
    public <T> void invokeFunction(String functionName, T t) {
        if (TextUtils.isEmpty(functionName)) {
            return;
        }
        if (mHasParamNoResultMap != null) {
            FunctionHasParamNoResult f = mHasParamNoResultMap.get(functionName);
            if (f != null) {
                if (t != null) {
                    f.function(t);
                }
            } else {
                LoggerUtil.d("没有找到该方法：" + functionName);
            }
        }
    }

    /**
     * 添加有参有返回值方法到函数管理类
     */
    public void addFunction(FunctionHasParamHasResult function) {
        mHasParamHasResultMap.put(function.functionName, function);
    }

    /**
     * 根据functionName执行方法
     */
    public <T, R> R invokeFunction(String functionName, T t, Class<R> r) {
        if (TextUtils.isEmpty(functionName)) {
            return null;
        }
        if (mHasParamHasResultMap != null) {
            FunctionHasParamHasResult f = mHasParamHasResultMap.get(functionName);
            if (f != null) {
                if (r != null && t != null) {
                    return r.cast(f.function(t));
                }
            } else {
                LoggerUtil.d("没有找到该方法：" + functionName);
            }
        }
        return null;
    }
}
