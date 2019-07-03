package com.android.baselibrary.strategy.httpProcessor.http.common;

/**
 * 统一接口返回bean类文件
 * 接口返回值bean文件，data为接口主要返回数据的bean文件，根据不同需求更改
 * 注：根据不同的需要，更改该bean文件
 *  该接口文档规范为：统一返回值为 code msg time ，不同之处就在于每个接口返回的数据 即data不同，因此需要使用泛型传入
 * 若接口中返回值的名称不同，则依旧需要更改名称，例如：请求成功码为：successCode，则请将code改为successCode，大小写不同亦需要改
 */
public class HttpResult<T> {
    private int status;
    private String message;
    private String time;
    private T data;

    public HttpResult() {
    }

    public HttpResult(int code, String msg, String time, T data) {
        this.status = code;
        this.message = msg;
        this.time = time;
        this.data = data;
    }

    public int getCode() {
        return status;
    }

    public void setCode(int code) {
        this.status = code;
    }

    public String getMsg() {
        return message;
    }

    public void setMsg(String msg) {
        this.message = msg;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "HttpResult{" +
                "code=" + status +
                ", msg='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
