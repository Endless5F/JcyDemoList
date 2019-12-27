package com.android.javalib;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class FutureTaskClass {

    public static void main(String[] args) {

        makeFutureOfCallable();

    }

    private static void makeFutureOfCallable() {
        FutureTask<String> stringFutureTask = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(5000);
                return "I am Callable";
            }
        });
        ThreadPoolUtils.getInstance().execute(stringFutureTask);
        try {
            // get()函数会阻塞，直到结果返回
            System.out.println("stringFutureTask == " + stringFutureTask.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
