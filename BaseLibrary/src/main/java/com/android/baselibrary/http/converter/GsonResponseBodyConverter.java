package com.android.baselibrary.http.converter;

import android.text.TextUtils;

import com.android.baselibrary.http.common.HttpResult;
import com.android.baselibrary.http.common.ResultException;
import com.android.baselibrary.utils.LogUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Converter  转换器
 * 自定义Gson响应体转换器，可在该类中对请求后返回来的响应json串进行处理，
 * 例如：若请求成功（code==200），则统一进行解析后返回，
 * 若code!=200则解析后传入自定义异常中，会直接出现在联网框架的请求失败的方法中
 * 注：请求成功的code==200，该类可不更改，若code成功为其它值，应更改该类中的code值：httpResult.getCode()==?
 */

public class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final Type type;
    // 请求返回成功码
    private static final int SUCCESS_CODE = 0;

    GsonResponseBodyConverter(Gson gson, Type type) {
        this.gson = gson;
        this.type = type;
    }

    @Override //convert方法完成了Json数据到实体类的映射
    public T convert(ResponseBody value) throws IOException {
        String response = value.string();
        LogUtils.d(TextUtils.isEmpty(response) ? "ResponseBody Value is null" : response);

        //先将返回的json数据解析到Response中，如果code==200，则解析到我们的实体基类中，否则抛异常
        HttpResult httpResult = gson.fromJson(response, HttpResult.class);
        if (httpResult.errorCode == SUCCESS_CODE) {
            //200的时候就直接解析，不可能出现解析异常。因为我们实体基类中传入的泛型，就是数据成功时候的格式
            return gson.fromJson(response, type);
        } else {
            HttpResult errorResponse = gson.fromJson(response, HttpResult.class);
            //抛一个自定义ResultException
            throw new ResultException(errorResponse.errorCode, errorResponse.errorMsg);
        }
    }
}
