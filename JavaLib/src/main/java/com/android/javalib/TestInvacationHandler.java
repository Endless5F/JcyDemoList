package com.android.javalib;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TestInvacationHandler implements InvocationHandler {
    private final ProxyInterface proxy;
    public TestInvacationHandler(ProxyInterface proxy){
        this.proxy = proxy;
    }
    /**
     proxy：就是代理对象，newProxyInstance方法的返回对象
     method：调用的方法
     args: 方法中的参数
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 方法执行前
        System.out.println("---------before-------");
        // 调用方法
        Object invoke = method.invoke(proxy, args);
        // 方法执行后
        System.out.println("---------after-------");
        return null;
    }
}