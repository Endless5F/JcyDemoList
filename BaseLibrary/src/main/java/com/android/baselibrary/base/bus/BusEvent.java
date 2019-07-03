package com.android.baselibrary.base.bus;

/**
 * @author jcy
 * @date 2018/8/25
 * descrption: Event
 * 描述：EventBus Event基类只有继承该类才能通过重写BaseActivity的
 * public void onEvent(Event event)方法监听到
 */

public class BusEvent<T> {
    public static final String TAG = "BusEvent";
    private int code;
    private T data;

    public BusEvent(int code) {
        this.code = code;
    }

    public BusEvent(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}