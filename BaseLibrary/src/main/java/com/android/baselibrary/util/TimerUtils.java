package com.android.baselibrary.util;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author jiaochengyun.ex
 * BasicThreadFactory 需要依赖：implementation 'org.apache.commons:commons-lang3:3.8'
 */
public class TimerUtils {
    /**
     * 延迟执行一次任务（单位毫秒）
     * @param command 任务
     * @param delay   延迟时间
     */
    public static ScheduledExecutorService schedule(Runnable command, long delay) {
        /*
         * 用Executor代替new timer
         *
         * 1是核心线程数量，后面参数是一个线程工厂，采用了建造者模式创建
         * 可以通过线程工厂给每个创建出来的线程设置符合业务的名字。
         */
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("example-schedule-pool-%d").daemon(true).build());
        executorService.schedule(command, delay, TimeUnit.MILLISECONDS);
        return executorService;
    }

    /**
     * 每个 period 时间执行一次任务，首次执行延迟 initialDelay（单位毫秒）
     *
     * @param command 任务
     * @param initialDelay   初始延迟时间
     * @param period   周期间隔时间
     */
    public static ScheduledExecutorService scheduleAtFixedRate(Runnable command, long initialDelay, long period) {
        /*
         * 用Executor代替new timer
         *
         * 1是核心线程数量，后面参数是一个线程工厂，采用了建造者模式创建
         * 可以通过线程工厂给每个创建出来的线程设置符合业务的名字。
         */
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("example-schedule-pool-%d").daemon(true).build());
        executorService.scheduleAtFixedRate(command, initialDelay, period, TimeUnit.MILLISECONDS);
        return executorService;
    }

    /**
     * 取消定时任务
     */
    public static void cancel(ScheduledExecutorService executorService) {
        if (executorService != null) {
            executorService.shutdownNow();
            // 非单例模式，置空防止重复的任务
            executorService = null;
        }
    }
}
