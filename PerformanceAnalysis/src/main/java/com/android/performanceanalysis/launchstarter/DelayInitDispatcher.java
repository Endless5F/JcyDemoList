package com.android.performanceanalysis.launchstarter;

import android.os.Looper;
import android.os.MessageQueue;

import com.android.performanceanalysis.launchstarter.task.DispatchRunnable;
import com.android.performanceanalysis.launchstarter.task.Task;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 延迟初始化方案
 * 核心思想：对延迟的任务分批初始化
 * IdleHandler封装类，利用IdleHandler的特性，空闲执行
 * <p>
 * 用法：
 *  DelayInitDispatcher delayInitDispatcher = new DelayInitDispatcher();
 *      delayInitDispatcher.addTask(new DelayInitTaskA())
 *      .addTask(new DelayInitTaskB())
 *      .start();
 * <p>
 * 优点：执行时机明确，解决界面卡顿
 */
public class DelayInitDispatcher {

    private Queue<Task> mDelayTasks = new LinkedList<>();

    private MessageQueue.IdleHandler mIdleHandler = new MessageQueue.IdleHandler() {
        @Override
        public boolean queueIdle() {
            if (mDelayTasks.size() > 0) {
                Task task = mDelayTasks.poll();
                new DispatchRunnable(task).run();
            }
            return !mDelayTasks.isEmpty();
        }
    };

    public DelayInitDispatcher addTask(Task task) {
        mDelayTasks.add(task);
        return this;
    }

    public void start() {
        Looper.myQueue().addIdleHandler(mIdleHandler);
    }

}
