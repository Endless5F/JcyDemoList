package com.android.javalib.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TestInvacationHandler implements InvocationHandler {
    // 被代理的真实对象
    private final ProxyInterface object;
    public TestInvacationHandler(ProxyInterface object){
        this.object = object;
    }
    /**
     * proxy：就是代理对象，newProxyInstance方法的返回对象
     *  proxy的作用：
     *      1. 可以使用反射获取代理对象的信息（也就是proxy.getClass().getName()）。
     *      2. 可以将代理对象返回以进行连续调用，这就是proxy存在的目的。因为this并不是代理对象，
     * method：调用的方法
     * args: 方法中的参数
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 方法执行前
        System.out.println("---------before-------");
        // 调用方法
        Object invoke = method.invoke(object, args);
//        Object invoke = method.invoke(proxy, args); // 若此处使用此句代码，则该程序会形成死循环
        // 方法执行后
        System.out.println("---------after-------");
        return invoke;
    }
}