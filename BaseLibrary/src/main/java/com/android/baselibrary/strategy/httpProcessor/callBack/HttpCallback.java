package com.android.baselibrary.strategy.httpProcessor.callBack;

import com.google.gson.Gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *  回调接口的实现
 *  1.必须想办法得到Result
 *  2.用户把返回string转换成调用层传入的参数化类型 Result
 */
public abstract class HttpCallback<Result> implements ICallback {

    @Override
    public void onSuccess(String result) {
        Gson gson = new Gson();
        Class<?> clz =analysisClassInfo(this);
        Result objResult= (Result) gson.fromJson(result,clz);
        //objResult传回调用者
        onSuccess(objResult);
    }

    public abstract void onSuccess(Result result);

    private static Class<?> analysisClassInfo(Object object){
        //getGenericSuperclass可以得到包含原始类型，参数化类型，数据类型，类型变量。
        Type genType = object.getClass().getGenericSuperclass();
        //获取参数化类型（<Result>）
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        return (Class<?>) params[0];
    }

    @Override
    public void onFailure(String e) {

    }
}

