package com.android.baselibrary.util;

import android.text.TextUtils;

import com.android.baselibrary.util.log.LoggerUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class DataProcessUtil {

    //根据泛型返回解析制定的类型
    public static <T> T fromToJson(String json, Class<T> listType) {
        if (TextUtils.isEmpty(json)) return null;
        Gson gson = new Gson();
        try {
            return gson.fromJson(json, listType);
        } catch (JsonSyntaxException exception) {
            LoggerUtil.i("JsonSyntaxException", "Json解析异常");
        }
        return null;
    }
}
