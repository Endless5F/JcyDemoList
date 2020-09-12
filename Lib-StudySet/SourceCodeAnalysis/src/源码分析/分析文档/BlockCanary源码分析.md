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
9. HandlerThreadFactory：传入一个HandlerThread类Looper的异步Handler。HandlerThread本质上是一个线程类，它继承了Thread；HandlerThread有自己的内部Looper对象，可以进行loop循环；通过获取HandlerThread的looper对象传递给Handler对象，可以在handleMessage方法中执行异步任务；创建HandlerThread后必须先调用HandlerThread.start()方法，Thread会先调用run方法，创建Looper对象。

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
```
    public static BlockCanary install(Context context, BlockCanaryContext blockCanaryContext) {
        // 将上下文和我们自定义的blockCanaryContext传入
        BlockCanaryContext.init(context, blockCanaryContext);
        // 根据displayNotification()设置是否启用或者禁用DisplayActivity组件
        setEnabled(context, DisplayActivity.class, BlockCanaryContext.get().displayNotification());
        // 返回单例BlockCanary
        return get();
    }
```
我们可以看到install方法中干了3件事情，我们分别来分析一下。
#### 1. BlockCanaryContext.init
```
// BlockCanaryContext类：
    private static Context sApplicationContext;
    private static BlockCanaryContext sInstance = null;

    static void init(Context context, BlockCanaryContext blockCanaryContext) {
        sApplicationContext = context;
        // 将我们自定义的blockCanaryContext类，保存在BlockCanaryContext类的成员变量sInstance中
        sInstance = blockCanaryContext;
    }
```
第一步，实际上就是在我们使用BlockCanary时，将我们自定义的AppBlockCanaryContext保存在BlockCanaryContext类的成员变量sInstance中，以供BlockCanary可以通过sInstance，来使用我们自已配置的各种信息(包括应用标识符，用户uid，网络类型，卡顿判断阀值，Log保存位置等)。
#### 2. setEnabled启用或禁用组件
```
// BlockCanaryContext类：
    public static BlockCanaryContext get() {
        if (sInstance == null) {
            throw new RuntimeException("BlockCanaryContext null");
        } else {
            return sInstance;
        }
    }

 // BlockCanary类：
    // 调用newSingleThreadExecutor初始化文件IO线程池
    private static final Executor fileIoExecutor = newSingleThreadExecutor("File-IO");

    private static void setEnabledBlocking(Context appContext,
                                           Class<?> componentClass,
                                           boolean enabled) {
        // 初始化组件对象
        ComponentName component = new ComponentName(appContext, componentClass);
        // 获取包管理者
        PackageManager packageManager = appContext.getPackageManager();
        int newState = enabled ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED;
        // 动态不杀死应用启用或者禁用组件，若enabled为true则启用，否则禁用
        packageManager.setComponentEnabledSetting(component, newState, DONT_KILL_APP);
    }

    private static void executeOnFileIoThread(Runnable runnable) {
        fileIoExecutor.execute(runnable);
    }

    private static Executor newSingleThreadExecutor(String threadName) {
        return Executors.newSingleThreadExecutor(new SingleThreadFactory(threadName));
    }

    private static void setEnabled(Context context,
                                   final Class<?> componentClass,
                                   final boolean enabled) {
        final Context appContext = context.getApplicationContext();
        executeOnFileIoThread(new Runnable() {
            @Override
            public void run() {
                setEnabledBlocking(appContext, componentClass, enabled);
            }
        });
    }
```
从上述代码中，可以看出来setEnabled方法，通过参数：BlockCanaryContext.get().displayNotification()，来设置DisplayActivity组件(用于显示记录的异常信息给开发者)是否启用。
1. BlockCanaryContext.get()返回的实际上就是第一步中所说到的我们自定义的AppBlockCanaryContext对象的引用变量sInstance，因此，若我们自定义的AppBlockCanaryContext中定义了displayNotification()方法，则按照我们自己定义的执行，若没有定义则按照其父类，即BlockCanaryContext中的displayNotification()方法返回值执行，默认返回为true。
2. setEnabled方法中，通过executeOnFileIoThread方法，使用静态常量fileIoExecutor线程池执行异步任务，根据我们传入的enabled(是否允许启用组件标识)，来最终启用或者禁用对应组件。关于动态启用或者禁用组件可参考：<a href="https://blog.csdn.net/zhaoyazhi2129/article/details/40586145">Android动态启用和禁用四大组件</a>。
#### 3. get()返回单例BlockCanary对象
```
    public static BlockCanary get() {
        if (sInstance == null) {
            synchronized (BlockCanary.class) {
                if (sInstance == null) {
                    sInstance = new BlockCanary();
                }
            }
        }
        return sInstance;
    }

    private BlockCanary() {
        // 将BlockCanaryContext.get()，即sInstance(我们自定义的AppBlockCanaryContext)
        // 设置到BlockCanary核心类BlockCanaryInternals中，用来获取我们自定义配置的信息
        BlockCanaryInternals.setContext(BlockCanaryContext.get());
        // 初始化BlockCanaryInternals
        mBlockCanaryCore = BlockCanaryInternals.getInstance();
        // 添加拦截器(将自定义的AppBlockCanaryContext添加到拦截器中，可回调其onBlock方法)
        mBlockCanaryCore.addBlockInterceptor(BlockCanaryContext.get());
        // 根据我们自定义的AppBlockCanaryContext获取是否展示通知，默认为true
        if (!BlockCanaryContext.get().displayNotification()) {
            return;
        }
        // 若允许展示通知，则将DisplayService继续添加到拦截器中
        mBlockCanaryCore.addBlockInterceptor(new DisplayService());

    }
```
我们从这部分源码中看到，BlockCanary的构造方法中完成了其核心类：BlockCanaryInternals的初始化与设置(包括sInstance传入和添加拦截器)，那么我们再来看一看BlockCanaryInternals的初始化都有些什么操作：
```
// BlockCanaryInternals类：
    static BlockCanaryInternals getInstance() {
        if (sInstance == null) {
            synchronized (BlockCanaryInternals.class) {
                if (sInstance == null) {
                    sInstance = new BlockCanaryInternals();
                }
            }
        }
        return sInstance;
    }

    public BlockCanaryInternals() {
        // 初始化堆栈采样器
        stackSampler = new StackSampler(
                Looper.getMainLooper().getThread(),
                sContext.provideDumpInterval());
        // 初始化cpu采样器
        cpuSampler = new CpuSampler(sContext.provideDumpInterval());
        // 设置监视器，传入LooperMonitor looper监控器
        // LooperMonitor 实际上就是我们上面【BlockCanary原理】中讲到的Printer接口的子类
        setMonitor(new LooperMonitor(new LooperMonitor.BlockListener() {

            @Override
            public void onBlockEvent(long realTimeStart, long realTimeEnd,
                                     long threadTimeStart, long threadTimeEnd) {
                // Get recent thread-stack entries and cpu usage
                ArrayList<String> threadStackEntries = stackSampler
                        .getThreadStackEntries(realTimeStart, realTimeEnd);
                if (!threadStackEntries.isEmpty()) {
                    BlockInfo blockInfo = BlockInfo.newInstance()
                            .setMainThreadTimeCost(realTimeStart, realTimeEnd, threadTimeStart,
                                    threadTimeEnd)
                            .setCpuBusyFlag(cpuSampler.isCpuBusy(realTimeStart, realTimeEnd))
                            .setRecentCpuRate(cpuSampler.getCpuRateInfo())
                            .setThreadStackEntries(threadStackEntries)
                            .flushString();
                    // 卡顿日志记录
                    LogWriter.save(blockInfo.toString());

                    if (mInterceptorChain.size() != 0) {
                        // 遍历所有拦截器成员，调用每个成员的onBlock，并将卡顿信息传入
                        for (BlockInterceptor interceptor : mInterceptorChain) {
                            interceptor.onBlock(getContext().provideContext(), blockInfo);
                        }
                    }
                }
            }
        }, getContext().provideBlockThreshold(), getContext().stopWhenDebugging()));

        LogWriter.cleanObsolete();
    }

    private void setMonitor(LooperMonitor looperPrinter) {
        // setMonitor把创建的LooperMonitor赋值给BlockCanaryInternals的成员变量monitor。
        monitor = looperPrinter;
    }
```
BlockCanaryInternals的构造方法中，初始化了几个变量，包括：堆栈采样器、cpu采样器、looper监控器，以及looper监控器的回调方法onBlockEvent。
### 2. BlockCanary.start()
#### 1. 监控卡顿
```
    public void start() {
        if (!mMonitorStarted) {
            mMonitorStarted = true;
            // 设置Looper中的mLogging，每次消息处理前后，
            // 都可回调自定义的实现Printer接口LooperMonitor类的println方法
            Looper.getMainLooper().setMessageLogging(mBlockCanaryCore.monitor);
        }
    }
```
将在BlockCanaryInternals中创建的LooperMonitor给主线程Looper的mLogging变量赋值。这样主线程Looper就可以消息分发前后使用LooperMonitor#println输出日志。此时BlockCanary已经开始监控卡顿情况，所以我们现在需要关注的就是LooperMonitor的println方法。

再回顾一下Looper的loop方法：
```
//Looper
    for (;;) {
        Message msg = queue.next();
        // This must be in a local variable, in case a UI event sets the logger
        Printer logging = me.mLogging;
        if (logging != null) {
            logging.println(">>>>> Dispatching to " + msg.target + " " +
                    msg.callback + ": " + msg.what);
        }

        msg.target.dispatchMessage(msg);

        if (logging != null) {
            logging.println("<<<<< Finished to " + msg.target + " " + msg.callback);
        }
        ...
    }
```
Lopper的loop方法中logging现在就是BlockCanary中实现了Printer接口的LooperMonitor类。
```
// LooperMonitor类：
    private boolean mPrintingStarted = false;
    @Override
    public void println(String x) {
        if (mStopWhenDebugging && Debug.isDebuggerConnected()) {
            return;
        }
        if (!mPrintingStarted) {
            // 获取消息处理前系统当前时间
            mStartTimestamp = System.currentTimeMillis();
            // 获取当前线程运行时间
            mStartThreadTimestamp = SystemClock.currentThreadTimeMillis();
            // 将此标识置为true，下此进入就是消息处理之后
            mPrintingStarted = true;
            // 开始获取堆栈信息
            startDump();
        } else {
            // 获取消息处理后系统当前时间
            final long endTime = System.currentTimeMillis();
            // 将此标识置为true，下此进入就是下一条消息处理之前
            mPrintingStarted = false;
            // 判断是否发生卡顿
            if (isBlock(endTime)) {
                // 发生卡顿，通知卡顿事件发生
                notifyBlockEvent(endTime);
            }
            // 停止获取堆栈信息
            stopDump();
        }
    }
```
对于每一个Message消息而言，println方法都是按顺序成对出现的，因此根据mPrintingStarted是否是消息开始前的标识，来判断此消息当前的处理前后两种状态。下面我们来看一下卡顿发生的情况：
```
// LooperMonitor类：
    private boolean isBlock(long endTime) {
        // 判断消息执行时间是否超过阈值
        return endTime - mStartTimestamp > mBlockThresholdMillis;
    }

    // 若超过阀值，则通知卡顿事件
    private void notifyBlockEvent(final long endTime) {
        final long startTime = mStartTimestamp;
        final long startThreadTime = mStartThreadTimestamp;
        // 获取消息处理结束后线程运行时间
        final long endThreadTime = SystemClock.currentThreadTimeMillis();
        // HandlerThreadFactory异步线程Looper的Handler
        HandlerThreadFactory.getWriteLogThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                // 异步线程中执行onBlockEvent回调
                mBlockListener.onBlockEvent(startTime, endTime, startThreadTime, endThreadTime);
            }
        });
    }
```
通过消息执行的前后时间差 - 我们自定义AppBlockCanaryContext中设置的卡顿阀值，来确定是否发生卡顿，卡顿后的回调消息是在设置为异步线程Looper的Handler中执行。
```
// BlockCanaryInternals类构造方法中：
    setMonitor(new LooperMonitor(new LooperMonitor.BlockListener() {
            @Override
            public void onBlockEvent(long realTimeStart, long realTimeEnd,
                                     long threadTimeStart, long threadTimeEnd) {
                // 根据开始及结束时间，从堆栈采集器的map当中获取记录信息
                ArrayList<String> threadStackEntries = stackSampler
                        .getThreadStackEntries(realTimeStart, realTimeEnd);
                if (!threadStackEntries.isEmpty()) {
                    // 构建 BlockInfo对象，设置相关的信息
                    BlockInfo blockInfo = BlockInfo.newInstance()
                            .setMainThreadTimeCost(realTimeStart, realTimeEnd, threadTimeStart, threadTimeEnd)
                            .setCpuBusyFlag(cpuSampler.isCpuBusy(realTimeStart, realTimeEnd))
                            .setRecentCpuRate(cpuSampler.getCpuRateInfo())
                            .setThreadStackEntries(threadStackEntries)
                            .flushString();
                    // 记录信息
                    LogWriter.save(blockInfo.toString());
                    // 遍历拦截器，通知
                    if (mInterceptorChain.size() != 0) {
                        for (BlockInterceptor interceptor : mInterceptorChain) {
                            interceptor.onBlock(getContext().provideContext(), blockInfo);
                        }
                    }
                }
            }
        }, getContext().provideBlockThreshold(), getContext().stopWhenDebugging()));
```
最后若拦截器成员中存在DisplayService，则会发送前台的通知，代码如下：
```
// DisplayService类：
    @Override
    public void onBlock(Context context, BlockInfo blockInfo) {
        Intent intent = new Intent(context, DisplayActivity.class);
        intent.putExtra("show_latest", blockInfo.timeStart);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, FLAG_UPDATE_CURRENT);
        String contentTitle = context.getString(R.string.block_canary_class_has_blocked, blockInfo.timeStart);
        String contentText = context.getString(R.string.block_canary_notification_message);
        // 根据不同的sdk兼容所有版本的通知栏显示
        show(context, contentTitle, contentText, pendingIntent);
    }
```
#### 2. 卡顿信息记录
```
// LooperMonitor类：
    private void startDump() {
        if (null != BlockCanaryInternals.getInstance().stackSampler) {
            // 开始记录堆栈信息
            BlockCanaryInternals.getInstance().stackSampler.start();
        }

        if (null != BlockCanaryInternals.getInstance().cpuSampler) {
            // 开始记录cpu信息
            BlockCanaryInternals.getInstance().cpuSampler.start();
        }
    }

    private void stopDump() {
        if (null != BlockCanaryInternals.getInstance().stackSampler) {
            // 停止记录堆栈信息
            BlockCanaryInternals.getInstance().stackSampler.stop();
        }

        if (null != BlockCanaryInternals.getInstance().cpuSampler) {
            // 停止记录cpu信息
            BlockCanaryInternals.getInstance().cpuSampler.stop();
        }
    }

    public void start() {
        // mShouldSample实际上是AtomicBoolean原子布尔值。
        if (mShouldSample.get()) {
            return;
        }
        // 原子布尔值，能够保证在高并发的情况下只有一个线程能够访问这个属性值。
        // 原子布尔值具体详情，参考：https://www.jianshu.com/p/8a44d4a819bc
        mShouldSample.set(true);
        // 移除上一次任务
        HandlerThreadFactory.getTimerThreadHandler().removeCallbacks(mRunnable);
        // 延迟 卡顿阀值*0.8 的时间执行相应信息的收集
        HandlerThreadFactory.getTimerThreadHandler().postDelayed(mRunnable,
                BlockCanaryInternals.getInstance().getSampleDelay());
    }

    public void stop() {
        if (!mShouldSample.get()) {
            return;
        }
        mShouldSample.set(false);
        // 移除任务
        HandlerThreadFactory.getTimerThreadHandler().removeCallbacks(mRunnable);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            // 调用doSample方法，执行相应操作
            doSample();
            // 若此原子布尔值为true，即此时为开始记录堆栈信息
            if (mShouldSample.get()) {
                // 延迟 卡顿阀值 时间执行任务
                HandlerThreadFactory.getTimerThreadHandler()
                        .postDelayed(mRunnable, mSampleInterval);
            }
        }
    };

// BlockCanaryInternals类：
    long getSampleDelay() {
        // 卡顿阀值的0.8
        return (long) (BlockCanaryInternals.getContext().provideBlockThreshold() * 0.8f);
    }
```
卡顿信息的记录，实际上是通过CpuSampler和StackSampler两者相同父类AbstractSampler类，提供的方法start和stop记录，而start方法中通过HandlerThreadFactory获取异步的TimerThreadHandler发送延时消息，最后分别调用CpuSampler类和StackSampler类中，继承自AbstractSampler抽象方法doSample()完成的卡顿信息的记录。下面分别看一下CpuSampler类和StackSampler类的doSample()方法的实现。

1. StackSampler类的doSample()方法
    ```
    private static final LinkedHashMap<Long, String> sStackMap = new LinkedHashMap<>();
    @Override
    protected void doSample() {
        StringBuilder stringBuilder = new StringBuilder();
        // mCurrentThread.getStackTrace():返回一个表示该线程堆栈转储的堆栈跟踪元素数组。
        // 通过mCurrentThread.getStackTrace()获取StackTraceElement，加入到StringBuilder
        for (StackTraceElement stackTraceElement : mCurrentThread.getStackTrace()) {
            stringBuilder
                    .append(stackTraceElement.toString())
                    .append(BlockInfo.SEPARATOR);
        }

        synchronized (sStackMap) {
            // Lru算法，控制LinkHashMap的长度，移除最早添加进来的数据
            if (sStackMap.size() == mMaxEntryCount && mMaxEntryCount > 0) {
                sStackMap.remove(sStackMap.keySet().iterator().next());
            }
            // 以当前系统时间为key，存储此处的堆栈信息
            sStackMap.put(System.currentTimeMillis(), stringBuilder.toString());
        }
    }
    ```
2. CpuSampler类的doSample()方法
    ```
    // 主要通过获取/proc/stat文件 去获取cpu的信息
    @Override
    protected void doSample() {
        BufferedReader cpuReader = null;
        BufferedReader pidReader = null;

        try {
            // 通过bufferReader读取 /proc 下的cpu文件
            cpuReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), BUFFER_SIZE);
            String cpuRate = cpuReader.readLine();
            if (cpuRate == null) {
                cpuRate = "";
            }

            if (mPid == 0) {
                mPid = android.os.Process.myPid();
            }
            // 通过bufferReader读取 /proc 下的内存文件
            pidReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/" + mPid + "/stat")), BUFFER_SIZE);
            String pidCpuRate = pidReader.readLine();
            if (pidCpuRate == null) {
                pidCpuRate = "";
            }

            parse(cpuRate, pidCpuRate);
        } catch (Throwable throwable) {
            Log.e(TAG, "doSample: ", throwable);
        } finally {
            try {
                if (cpuReader != null) {
                    cpuReader.close();
                }
                if (pidReader != null) {
                    pidReader.close();
                }
            } catch (IOException exception) {
                Log.e(TAG, "doSample: ", exception);
            }
        }
    }

    private void parse(String cpuRate, String pidCpuRate) {
        String[] cpuInfoArray = cpuRate.split(" ");
        if (cpuInfoArray.length < 9) {
            return;
        }

        long user = Long.parseLong(cpuInfoArray[2]);
        long nice = Long.parseLong(cpuInfoArray[3]);
        long system = Long.parseLong(cpuInfoArray[4]);
        long idle = Long.parseLong(cpuInfoArray[5]);
        long ioWait = Long.parseLong(cpuInfoArray[6]);
        long total = user + nice + system + idle + ioWait
                + Long.parseLong(cpuInfoArray[7])
                + Long.parseLong(cpuInfoArray[8]);

        String[] pidCpuInfoList = pidCpuRate.split(" ");
        if (pidCpuInfoList.length < 17) {
            return;
        }

        long appCpuTime = Long.parseLong(pidCpuInfoList[13])
                + Long.parseLong(pidCpuInfoList[14])
                + Long.parseLong(pidCpuInfoList[15])
                + Long.parseLong(pidCpuInfoList[16]);

        if (mTotalLast != 0) {
            StringBuilder stringBuilder = new StringBuilder();
            long idleTime = idle - mIdleLast;
            long totalTime = total - mTotalLast;

            stringBuilder
                    .append("cpu:")
                    .append((totalTime - idleTime) * 100L / totalTime)
                    .append("% ")
                    .append("app:")
                    .append((appCpuTime - mAppCpuTimeLast) * 100L / totalTime)
                    .append("% ")
                    .append("[")
                    .append("user:").append((user - mUserLast) * 100L / totalTime)
                    .append("% ")
                    .append("system:").append((system - mSystemLast) * 100L / totalTime)
                    .append("% ")
                    .append("ioWait:").append((ioWait - mIoWaitLast) * 100L / totalTime)
                    .append("% ]");

            synchronized (mCpuInfoEntries) {
                mCpuInfoEntries.put(System.currentTimeMillis(), stringBuilder.toString());
                if (mCpuInfoEntries.size() > MAX_ENTRY_COUNT) {
                    for (Map.Entry<Long, String> entry : mCpuInfoEntries.entrySet()) {
                        Long key = entry.getKey();
                        mCpuInfoEntries.remove(key);
                        break;
                    }
                }
            }
        }
        mUserLast = user;
        mSystemLast = system;
        mIdleLast = idle;
        mIoWaitLast = ioWait;
        mTotalLast = total;

        mAppCpuTimeLast = appCpuTime;
    }
    ```
    Android平台CPU的一些常识：

    1. Android是基于Linux系统的，Android平台关于CPU的计算是跟Linux是完全一样的。
    2. 在Linux中CPU活动信息是保存在/proc/stat文件中，该文件中的所有值都是从系统启动开始累计到当前时刻。
    3. /proc/stat文件内容：
        ```
        > cat /proc/stat
        1. cpu  2255 34 2290 22625563 6290 127 456
        2. cpu0 1132 34 1441 11311718 3675 127 438
        3. cpu1 1123 0 849 11313845 2614 0 18
        4. intr 114930548 113199788 3 0 5 263 0 4 [... lots more numbers ...]
        5. ctxt 1990473
        6. btime 1062191376
        7. processes 2915
        8. procs_running 1
        9. procs_blocked 0
        ```
        这些数字指明了CPU执行不同的任务所消耗的时间（从系统启动开始累计到当前时刻）。时间单位是USER_HZ或jiffies（通常是百分之一秒）。
    4. 解析3中第一行各数值的含义
        ```
        参数	        解析 (以下数值都是从系统启动累计到当前时刻)
        user (38082)	处于用户态的运行时间，不包含 nice值为负进程
        nice (627)	nice值为负的进程所占用的CPU时间
        system (27594)	处于核心态的运行时间
        idle (893908)	除IO等待时间以外的其它等待时间iowait (12256) 从系统启动开始累计到当前时刻，IO等待时间
        irq (581)	硬中断时间
        irq (581)	软中断时间
        stealstolen(0)	一个其他的操作系统运行在虚拟环境下所花费的时间
        guest(0)	这是在Linux内核控制下为客户操作系统运行虚拟CPU所花费的时间
        ```
        总结：总的CPU时间totalCpuTime = user + nice + system + idle + iowait + irq + softirq + stealstolen + guest
    5. /proc/pid/stat文件：包含了某一进程所有的活动的信息，该文件中的所有值都是从系统启动开始累计到当前时刻
        ```
        cat /proc/6873/stat
        6873 (a.out) R 6723 6873 6723 34819 6873 8388608 77 0 0 0 41958 31 0 0 25 0 3 0 5882654 1409024 56 4294967295 134512640 134513720 3215579040 0 2097798 0 0 0 0 0 0 0 17 0 0 0
        ```
        计算CPU使用率有用相关参数：
        ```
        参数	    解析
        pid=6873	进程号
        utime=1587	该任务在用户态运行的时间，单位为jiffies
        stime=41958	该任务在核心态运行的时间，单位为jiffies
        cutime=0	所有已死线程在用户态运行的时间，单位为jiffies
        cstime=0	所有已死在核心态运行的时间，单位为jiffies
        ```
        结论：进程的总CPU时间processCpuTime = utime + stime + cutime + cstime，该值包括其所有线程的CPU时间。
#### 3. 卡顿日志记录
卡顿发生时，会回调LooperMonitor的onBlockEvent方法，而此方法中，会将卡顿信息写入本地日志文件，日志的路径在自定义的AppBlockCanaryContext中定义。
```
// BlockCanaryInternals类构造方法中：
    setMonitor(new LooperMonitor(new LooperMonitor.BlockListener() {
            @Override
            public void onBlockEvent(long realTimeStart, long realTimeEnd,
                                     long threadTimeStart, long threadTimeEnd) {
                ArrayList<String> threadStackEntries = stackSampler
                        .getThreadStackEntries(realTimeStart, realTimeEnd);
                if (!threadStackEntries.isEmpty()) {
                    BlockInfo blockInfo = BlockInfo.newInstance()
                            .setMainThreadTimeCost(realTimeStart, realTimeEnd, threadTimeStart, threadTimeEnd)
                            .setCpuBusyFlag(cpuSampler.isCpuBusy(realTimeStart, realTimeEnd))
                            .setRecentCpuRate(cpuSampler.getCpuRateInfo())
                            .setThreadStackEntries(threadStackEntries)
                            .flushString();
                    // 日志保存
                    LogWriter.save(blockInfo.toString());

                    if (mInterceptorChain.size() != 0) {
                        for (BlockInterceptor interceptor : mInterceptorChain) {
                            interceptor.onBlock(getContext().provideContext(), blockInfo);
                        }
                    }
                }
            }
        }, getContext().provideBlockThreshold(), getContext().stopWhenDebugging()));

// LogWriter类：
    public static String save(String str) {
        String path;
        synchronized (SAVE_DELETE_LOCK) {
            path = save("looper", str);
        }
        return path;
    }

    private static String save(String logFileName, String str) {
        String path = "";
        BufferedWriter writer = null;
        try {
            // 根据开发者自己配置的日志存储路径，生成文件
            File file = BlockCanaryInternals.detectedBlockDirectory();
            long time = System.currentTimeMillis();
            path = file.getAbsolutePath() + "/"
                    + logFileName + "-"
                    + FILE_NAME_FORMATTER.format(time) + ".log";
            // 写入卡顿信息
            OutputStreamWriter out =
                    new OutputStreamWriter(new FileOutputStream(path, true), "UTF-8");

            writer = new BufferedWriter(out);

            writer.write(BlockInfo.SEPARATOR);
            writer.write("**********************");
            writer.write(BlockInfo.SEPARATOR);
            writer.write(TIME_FORMATTER.format(time) + "(write log time)");
            writer.write(BlockInfo.SEPARATOR);
            writer.write(BlockInfo.SEPARATOR);
            writer.write(str);
            writer.write(BlockInfo.SEPARATOR);

            writer.flush();
            writer.close();
            writer = null;

        } catch (Throwable t) {
            Log.e(TAG, "save: ", t);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "save: ", e);
            }
        }
        return path;
    }
```
## BlockCanary卡顿参数解读
1. cpuCore：手机cpu个数。
2. processName：应用包名。
3. freeMemory: 手机剩余内存,单位KB。
4. totalMemory: 手机内训总和，单位KB。
5. timecost: 该Message(事件)执行时间，单位 ms。
6. threadtimecost: 该Message(事件)执行线程时间（线程实际运行时间，不包含别的线程占用cpu时间），单位 ms。
7. cpubusy: true表示cpu负载过重，false表示cpu负载不重。cpu负载过重导致该Message(事件) 超时，错误不在本事件处理上。

至此，BlockCanary的整体已分析完成，收工咯。

## 参考链接
https://www.jianshu.com/p/0d00cb85fdf3

https://www.jianshu.com/p/5602ca1322b2?from=timeline&isappinstalled=0

https://www.jianshu.com/p/e58992439793

...

<font color="#ff0000">注：若有什么地方阐述有误，敬请指正。**期待您的点赞哦！！！**</font>

