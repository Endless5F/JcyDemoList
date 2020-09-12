package com.android.javalib.proxy;

import java.lang.reflect.Proxy;

public class MainClass {
    private static long count;

    public static void main(String[] args) {

        String ad = "-1000";
        System.out.println(Integer.parseInt(ad)+"");

        ProxyInterface real = new RealClass();
        ProxyInterface proInterface = (ProxyInterface) Proxy
                .newProxyInstance(real.getClass().getClassLoader()
                        ,RealClass.class.getInterfaces(), new TestInvacationHandler(real));
        proInterface.handlingEvents();

    }
}
