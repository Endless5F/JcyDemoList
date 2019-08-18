package com.android.javalib;

/**
 * 真实类
 */
public class RealClass implements ProxyInterface {

    @Override
    public void handlingEvents() {
        System.out.println("正在处理事件中......");
    }
}
