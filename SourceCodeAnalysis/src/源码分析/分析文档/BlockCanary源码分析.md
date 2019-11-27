主流开源框架源码深入了解第5篇——BlockCanary源码分析。(源码以1.5.0版为准)

## UI卡顿原理
问：为什么16ms没完成绘制就会卡顿？

我们先来了解几个概念：
1. Android系统每隔16ms就会重新绘制一次Activity，因此，我们的应用必须在16ms内完成屏幕刷新的全部逻辑操作，每一帧只能停留16ms，否则就会出现掉帧现象(也就是用户看到的卡顿现象)。
2. 16ms = 1000/60hz，相当于60fps(每秒帧率)。这是因为人眼与大脑之间的协作无法感知超过60fps的画面更新。12fps大概类似手动快速翻书的帧率，这个速度明显可以感知是不够顺滑的。24fps使得人眼感知的是连续线性运动，24fps是电影胶圈通常使用的帧率，这个帧率可以支撑大部分电影画面需要表达的内容。但是低于30fps是无法顺畅表现绚丽的画面内容，此时需要使用60fps来达到想要的效果。因此，如果应用没有在16ms内完成屏幕刷新的全部逻辑操作，就会发生卡顿。
3. Android不允许在UI线程中做耗时的操作，否则有可能发生ANR的可能，默认情况下，在Android中Activity的最长执行时间是5秒，BroadcastReceiver的最长执行时间则是10秒，Service前台20s、后台200s未完成启动。如果超过默认最大时长，则会产生ANR。

答：Android系统每隔16ms就会发出VSYNC信号，触发对UI进行渲染，VSYNC是Vertical Synchronization(垂直同步)的缩写，可以简单的把它认为是一种定时中断。在Android 4.1中开始引入VSYNC机制。为什么是16ms？因为Android设定的刷新率是60FPS(Frame Per Second)，也就是每秒60帧的刷新率，约16ms刷新一次。这就意味着，我们需要在16ms内完成下一次要刷新的界面的相关运算，以便界面刷新更新。举个例子，当运算需要24ms完成时，16ms时就无法正常刷新了，而需要等到32ms时刷新，这就是丢帧了。丢帧越多，给用户的感觉就越卡顿。

正常流畅刷新图示：
![](https://user-gold-cdn.xitu.io/2019/11/26/16ea83e59a79899a?w=800&h=365&f=png&s=86744)

哎呀！丢帧啦。卡顿图示：
![](https://user-gold-cdn.xitu.io/2019/11/26/16ea83ea26c99105?w=800&h=400&f=png&s=78005)

## BlockCanary原理
在说原理之前，我们先来了解几个概念：
1. 主线程ActivityThread：严格来说，UI主线程不是ActivityThread。ActivityThread类是Android APP进程的初始类，它的main函数是这个APP进程的入口。APP进程中UI事件的执行代码段都是由ActivityThread提供的。也就是说，Main Thread实例是存在的，只是创建它的代码我们不可见。ActivityThread的main函数就是在这个Main Thread里被执行的。这个主线程会创建一个Looper(Looper.prepare)，而Looper又会关联一个MessageQueue，主线程Looper会在应用的生命周期内不断轮询(Looper.loop)，从MessageQueue取出Message 更新UI。

        // ActivityThread类：
        public static void main(String[] args) {
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "ActivityThreadMain");
            ...
            Looper.prepareMainLooper();
            ...
            ActivityThread thread = new ActivityThread();
            thread.attach(false, startSeq);

            if (sMainThreadHandler == null) {
                sMainThreadHandler = thread.getHandler();
            }
            ...
            // End of event ActivityThreadMain.
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            // Looper开始轮询
            Looper.loop();

            throw new RuntimeException("Main thread loop unexpectedly exited");
        }
2. Vsync信号：屏幕的刷新过程是每一行从左到右（行刷新，水平刷新，Horizontal Scanning），从上到下（屏幕刷新，垂直刷新，Vertical Scanning）。当整个屏幕刷新完毕，即一个垂直刷新周期完成，会有短暂的空白期，此时发出 VSync 信号。所以，VSync 中的 V指的是垂直刷新中的垂直-Vertical。Android系统每隔16ms发出VSYNC信号，触发对UI进行渲染，VSync是Vertical Synchronization(垂直同步)的缩写，是一种在PC上很早就广泛使用的技术，可以简单的把它认为是一种定时中断。而在Android 4.1(JB)中已经开始引入VSync机制，用来同步渲染，让App的UI和SurfaceFlinger可以按硬件产生的VSync节奏进行工作。
3. 界面刷新：界面上任何一个 View 的刷新请求最终都会走到 ViewRootImpl 中的 scheduleTraversals() 里来安排一次遍历绘制 View 树的任务；并且通过源码都可以知道所有的界面刷新(包括Vsync信号触发的)，都会通过Choreographer 的 postCallback() 方法，将界面刷新这个 Runnable 任务以当前事件放进一个待执行的队列里，最后通过主线程的Looper的loop方法取出消息并执行。

    ```
    // Looper类：
        public static void loop() {
            final Looper me = myLooper();
            ...
            // 获取当前Looper的消息队列
            final MessageQueue queue = me.mQueue;
            ...
            for (; ; ) {
                // 取出一个消息
                Message msg = queue.next(); // might block
                ...
                // "此mLogging可通过Looper.getMainLooper().setMessageLogging方法设置自定义"
                final Printer logging = me.mLogging;
                if (logging != null) {// 消息处理前
                    // "若mLogging不为null，则此处可回调到该类的println方法"
                    logging.println(">>>>> Dispatching to " + msg.target + " " +
                            msg.callback + ": " + msg.what);
                }

                ...
                try {
                    // 消息处理
                    msg.target.dispatchMessage(msg);
                    dispatchEnd = needEndTime ? SystemClock.uptimeMillis() : 0;
                } finally {
                    if (traceTag != 0) {
                        Trace.traceEnd(traceTag);
                    }
                }
                ...

                if (logging != null) {// 消息处理后
                    // "消息处理后，也可调用logging的println方法"
                    logging.println("<<<<< Finished to " + msg.target + " " + msg.callback);
                }

                ...
            }
        }
    ```
4. 卡顿发生点：从第3条中，我们可以看出，所有消息最终都经过dispatchMessage方法。因此界面的卡顿最终都应该是发生在Handler的dispatchMessage里。
    ```
    // Handler类：
    public void dispatchMessage(Message msg) {
        if (msg.callback != null) {
            handleCallback(msg);
        } else {
            if (mCallback != null) {
                if (mCallback.handleMessage(msg)) {
                    return;
                }
            }
            handleMessage(msg);
        }
    }
    ```
5. 屏幕刷新机制，具体可参考：<a href="https://www.jianshu.com/p/0d00cb85fdf3">Android 屏幕刷新机制</a>
Handler消息机制和View的绘制机制，具体可参考：<a href="https://juejin.im/post/5cd7b89be51d45475e613e59">Handler机制</a>和<a href="https://juejin.im/post/5cb438695188257a9e31263a">View绘制流程源码分析</a>

**原理：** 上面几个概念中，其实里面已包含卡顿监控的原理啦。我们在界面刷新中Looper的loop方法注释中声明："若mLogging不为null，则此处可回调到该类的println方法"，因此我们可以通过自定义的mLogging(实际为Printer接口的子类)，实现Printer接口的println方法，然后在println方法中监控是否有卡顿发生。从loop方法中，可以看出logging.println调用是成对出现的，会在消息处理前后分别调用，因此可以在自定义的println方法中通过标识来分辨是消息处理前/后，通过计算时间差与我们自己设置的阀值(我们认为消息处理的最长时间，即卡顿的临界值)比对，来监控我们的程序是否发生卡顿。

官方原理介绍示例图：
![](https://user-gold-cdn.xitu.io/2019/11/27/16ead4d25211da6a?w=711&h=698&f=png&s=276028)

## BlockCanary简介
### 1. 关联类功能说明
1. BlockCanary：外观类，提供初始化及开始、停止监听
2. BlockCanaryContext：配置上下文，可配置id、当前网络信息、卡顿阈值、log保存路径等。建议：通过自己实现继承该类的子类，配置应用标识符，用户uid，网络类型，卡顿判断阀值，Log保存位置等，可通过继承该类将卡顿信息收集上传云端或保存本地等。
3. BlockCanaryInternals：blockcanary核心的调度类，内部包含了monitor（设置到MainLooper的printer）、stackSampler（栈信息处理器）、cpuSampler（cpu信息处理器）、mInterceptorChain（注册的拦截器）、以及onBlockEvent的回调及拦截器的分发。
4. LooperMonitor：继承了Printer接口，用于设置到MainLooper中。通过复写println的方法来获取MainLooper的dispatch前后的执行时间差，并控制stackSampler和cpuSampler的信息采集。
5. StackSampler：用于获取线程的栈信息，将采集的栈信息存储到一个以key为时间戳的LinkHashMap中。通过mCurrentThread.getStackTrace()获取当前线程的StackTraceElement。
6. CpuSampler：用于获取cpu信息，将采集的cpu信息存储到一个以key为时间戳的LinkHashMap中。通过读取/proc/stat文件获取cpu的信息。
7. DisplayService：继承了BlockInterceptor拦截器，onBlock回调会触发发送前台通知。
8. DisplayActivity：用于显示记录的异常信息的Activity。

### 2. BlockCanary简单使用
```
// Application中
    // 卡顿优化
    // 指定的卡顿阀值为500毫秒——provideBlockThreshold()方法；可在onBlock方法处收集堆栈信息
    BlockCanary.install(this, new AppBlockCanaryContext()).start();

/**
 * BlockCanary配置的各种信息(部分)
 */
public class AppBlockCanaryContext extends BlockCanaryContext {
    // 实现各种上下文，包括应用标识符，用户uid，网络类型，卡顿判断阀值，Log保存位置等

    /**
     * 指定的卡顿阀值 500毫秒
     */
    public int provideBlockThreshold() {
        return 500;
    }

    /**
     * 保存日志的路径
     */
    public String providePath() {
        return "/blockcanary/";
    }

    /**
     * 是否需要在通知栏通知卡顿
     */
    public boolean displayNotification() {
        return true;
    }

    /**
     * 此处可收集堆栈信息，以备上传分析
     * Block interceptor, developer may provide their own actions.
     */
    public void onBlock(Context context, BlockInfo blockInfo) {
        Log.i("lz","blockInfo "+blockInfo.toString());
        // 获取当前执行方法的调用栈信息
//        String trace = Log.getStackTraceString(new Throwable());
    }
```
AppBlockCanaryContext具体配置可参考：<a href="https://github.com/Endless5F/JcyDemoList/blob/master/PerformanceAnalysis/src/main/java/com/android/performanceanalysis/blockcanary/AppBlockCanaryContext.java">AppBlockCanaryContext.java</a>

## BlockCanary源码
### 1. BlockCanary.install
### 2. BlockCanary.start()


## 参考链接
https://www.jianshu.com/p/0d00cb85fdf3

https://www.jianshu.com/p/5602ca1322b2?from=timeline&isappinstalled=0

https://www.jianshu.com/p/e58992439793

<font color="#ff0000">注：若有什么地方阐述有误，敬请指正。**期待您的点赞哦！！！**</font>

