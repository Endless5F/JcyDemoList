package com.android.baselibrary.http.common;

import com.android.baselibrary.utils.JsonUtils;
import com.google.gson.JsonObject;

/**
 * 统一接口返回bean类文件
 * 接口返回值bean文件，data为接口主要返回数据的bean文件，根据不同需求更改
 * 注：根据不同的需要，更改该bean文件
 *  该接口文档规范为：统一返回值为 code msg time ，不同之处就在于每个接口返回的数据 即data不同，因此需要使用泛型传入
 * 若接口中返回值的名称不同，则依旧需要更改名称，例如：请求成功码为：successCode，则请将code改为successCode，大小写不同亦需要改
 */
public class HttpResult {
    public int errorCode;
    public String errorMsg;

    public void parseErrorData(JsonObject jsonObj) {
        this.errorCode = JsonUtils.getInt(jsonObj, "errorCode", -1);
        this.errorMsg = JsonUtils.getString(jsonObj, "errorMsg");
    }

    @Override
    public String toString() {
        return "HttpResult{" +
                "errorCode=" + errorCode +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}
