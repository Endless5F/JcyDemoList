package com.android.baselibrary.util;

import androidx.annotation.NonNull;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池辅助类，整个应用程序就只有一个线程池去管理线程。
 * <p>
 * 可以设置核心线程数、最大线程数、额外线程空状态生存时间，阻塞队列长度来优化线程池。
 */
public class ThreadPoolUtils {

    private ThreadPoolUtils() {
    }

    private static class Loader {
        private static final ThreadPoolUtils INSTANCE = new ThreadPoolUtils();
    }

    public static ThreadPoolUtils getInstance() {
        return Loader.INSTANCE;
    }

    // 线程池核心线程数
    private final int CORE_POOL_SIZE = 3;
    // 线程池最大线程数
    private final int MAX_POOL_SIZE = 10;
    // 额外线程空状态生存时间
    private final int KEEP_ALIVE_TIME = 10;

    private class MyThreadFactory implements ThreadFactory {

        private ThreadGroup group;

        MyThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread()
                    .getThreadGroup();
        }

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(group, r, "MyThread" + r.getClass().getName(), 0);
        }

    }

    // 线程池
    private ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), new MyThreadFactory(),
            new ThreadPoolExecutor.DiscardPolicy());

    /**
     * 从线程池中抽取线程，执行指定的Runnable对象
     *
     * @param runnable
     */
    public void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }

}
