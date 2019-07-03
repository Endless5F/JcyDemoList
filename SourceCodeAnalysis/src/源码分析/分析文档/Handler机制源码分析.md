## Handler机制
> 相关概念：在理解Handler机制之前，需要先熟知跟Hanlder相关的几个类：Handler、Message、Message Queue、Looper，这几个类之间的关系如下图：

![](https://user-gold-cdn.xitu.io/2019/4/14/16a19a6ddc3746c0?w=1123&h=794&f=jpeg&s=165065)
1. Looper：线程是一段可执行的代码，当可执行代码执行完成后，线程生命周期便会终止，线程就会退出，而主线程要是自动退出，那还玩个什么，因此此时就用到了Looper，在主线程中调用Looper.prepare()...Looper.loop()，在loop方法里会执行一段for (;;) {}代码，保证主线程不会自动退出，具体下面再说，主线程会在这段死循环中不断等其他线程给它发消息（消息包括：Activity启动，生命周期，更新UI，控件事件等），一有消息就根据消息做相应的处理，Looper的另外一部分工作就是在循环代码中会不断从消息队列挨个拿出消息给主线程处理。

2.MessageQueue：就是一个队列，特性就是“先进先出，后进后出”，每一个Looper都会维护这样一个队列，而且仅此一个，这个队列的消息只能由该线程处理。

3.Handler：Handler在sendMessage的时候会往消息队列里插入新消息。Handler的另外一个作用，就是能统一处理消息的回调。

接下来开始讲解Hanldre机制的源码，源码将分为3部分说明，包括程序启动初始化、Loop的轮询机制、Handler消息的发送

* 程序初始化：应用程序的开始入口是从ActivityThread.main入口的，具体可看上面Activity的启动流程中，假设的第一种：应用第一次启动
```
// 程序入口函数
public static void main(String[] args) {
        Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "ActivityThreadMain");
        CloseGuard.setEnabled(false);
        Environment.initForCurrentUser();

        // Set the reporter for event logging in libcore
        EventLogger.setReporter(new EventLoggingReporter());

        // Make sure TrustedCertificateStore looks in the right place for CA certificates
        final File configDir = Environment.getUserConfigDirectory(UserHandle.myUserId());
        TrustedCertificateStore.setDefaultUserDirectory(configDir);

        Process.setArgV0("<pre-initialized>");
        
        // 主线程Loop初始化
        Looper.prepareMainLooper();
        // 初始化资源管理器
        ActivityThread thread = new ActivityThread();

        /**
         *  attach方法会创建一个Binder线程（具体是指ApplicationThread，该Binder线程会通过向
         *   Handler将Message发送给主线程)。我们之前提到主线程最后会进入无限循环当中，
         *   在没有在进入死循环之前会在这里创建Binder线程，这个线程会接收来自系统服务
         *   发送来的一些事件封装了Message并发送给主线程，主线程在无限循环中从队列里拿到这
         *   些消息并处理。(Binder线程发生的消息包括LAUNCH_ACTIVITY，PAUSE_ACTIVITY等等)
         *
         * 并且会在该方法中完成Application对象的初始化，然后调用Application的onCreate()方法
        */

        thread.attach(false);

        if (sMainThreadHandler == null) {
            sMainThreadHandler = thread.getHandler();
        }

        if (false) {
            Looper.myLooper().setMessageLogging(new
                    LogPrinter(Log.DEBUG, "ActivityThread"));
        }

        // End of event ActivityThreadMain.
        Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
        
        // 启动Looper的loop轮询方法
        Looper.loop();

        throw new RuntimeException("Main thread loop unexpectedly exited");
    }
```
因此在程序入口函数中，就完成了Looper的主线程初始化以及轮询方法的运行。

* Looper的轮询机制，接下来看看主线程Looper的初始化，以及轮询loop方法,从程序入口方法中看到主线程的Looper的初始化调用prepareMainLooper方法完成。
```
// 主线程Looper的初始化
    public static void prepareMainLooper() {
        prepare(false); // 实际上调用此方法初始化Looper
        synchronized (Looper.class) {
            if (sMainLooper != null) {
                throw new IllegalStateException("The main Looper has already been prepared.");
            }
            sMainLooper = myLooper(); // 设置主线程Looper
        }
    }
    
    private static void prepare(boolean quitAllowed) {
        if (sThreadLocal.get() != null) {
            throw new RuntimeException("Only one Looper may be created per thread");
        }
        // 将Looper设置到ThreadLocal中，具体后面再将
        sThreadLocal.set(new Looper(quitAllowed));
    }
    // Looper构造方法
    private Looper(boolean quitAllowed) {
        // Looper在初始化时，会实例一个消息队列，此时Looper持有MessageQueue的引用
        mQueue = new MessageQueue(quitAllowed);
        // 当前Looper所在线程
        mThread = Thread.currentThread();
    }
    
    public static void loop() {
        // 获取当前Looper的实例对象
        final Looper me = myLooper();
        if (me == null) {
            throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
        }
        // 获取当前Looper的消息队列
        final MessageQueue queue = me.mQueue;
        ...
        // 主线程开启无限循环模式
        for (;;) {
            // 获取队列中下一条消息并删除，可能会线程阻塞
            Message msg = queue.next();
            if (msg == null) {
                return;
            }
            ...
            try {
                //分发Message，
                //这个Message会持有这个Handler的引用，并放到自己的target变量中,这样就可以回调我们重写的handler的handleMessage方法。
                /**
                 * 分发消息：
                 *  msg.target是一个Handler对象，在Handler调用sendMessage方法时会设置此变量
                 *   因此就可以做到哪个Handler发送的消息，就会通过哪个Handler的分发消息方法
                 *   dispatchMessage将消息重新回调到该Hanlder中
                */
                msg.target.dispatchMessage(msg);
                end = (slowDispatchThresholdMs == 0) ? 0 : SystemClock.uptimeMillis();
            } finally {
                if (traceTag != 0) {
                    Trace.traceEnd(traceTag);
                }
            }
            ...
            // 将Message回收到消息池,下次要用的时候不需要重新创建，obtain()就可以了。
            msg.recycleUnchecked();
        }
    }
```
prepareMainLooper()方法实际上就是new了一个Looper实例并放入Looper类下面一个static的sThreadLocal静态变量中，同时给sMainLooper赋值,给sMainLooper赋值是为了方便通过Looper.getMainLooper()快速获取主线程的Looper，sMainLooper是主线程的Looper可能获取会比较频繁，避免每次都到 sThreadLocal 去查找获取。loop()方法 == 消息出队 + 分发给对应的Handler实例

注：此处有一个重要的概念ThreadLocal,后面具体分析

* Handler：先接上面个Looper的loop方法，看看Handler的dispatchMessage分发消息方法，再看看Handler的初始化以及发送消息sendMessage方法
```
    public void dispatchMessage(Message msg) {
        // 若msg.callback属性不为空，则代表使用了post(Runnable r)发送消息
        // 则执行handleCallback(msg)，即回调Runnable对象里复写的run()
        if (msg.callback != null) {
            handleCallback(msg);
        } else {
            if (mCallback != null) {
                if (mCallback.handleMessage(msg)) {
                    return;
                }
            }
            // 若msg.callback属性为空，则代表使用了sendMessage(Message msg)发送消息
            // 则执行handleMessage(msg)，即回调复写的handleMessage(msg)
            handleMessage(msg);
        }
    }
    
    // 直接回调Runnable对象里复写的run()方法
    private static void handleCallback(Message message) {
        message.callback.run();
    }
    
    // 注：handleMessage为空方法，在创建Handler实例时会复写该方法
    public void handleMessage(Message msg) {
    }
    
    // Handler构造方法1
    public Handler() {
        // 调用Handler构造方法6
        this(null, false);
    }
    // Handler构造方法2
    public Handler(Callback callback) {
        // 调用Handler构造方法6
        this(callback, false);
    }
    // Handler构造方法3
    public Handler(Looper looper) {
        // 调用Handler构造方法7
        this(looper, null, false);
    }
    // Handler构造方法4
    public Handler(Looper looper, Callback callback) {
        // 调用Handler构造方法7
        this(looper, callback, false);
    }
    // Handler构造方法5
    public Handler(boolean async) {
        // 调用Handler构造方法6
        this(null, async);
    }
    // Handler构造方法6
    public Handler(Callback callback, boolean async) {
        ...
        mLooper = Looper.myLooper();
        if (mLooper == null) {
            throw new RuntimeException(
                "Can't create handler inside thread that has not called Looper.prepare()");
        }
        mQueue = mLooper.mQueue;
        mCallback = callback;
        mAsynchronous = async;
    }
    // Handler构造方法7
    public Handler(Looper looper, Callback callback, boolean async) {
        mLooper = looper;
        mQueue = looper.mQueue;
        mCallback = callback;
        mAsynchronous = async;
    }
    
    // sendMessage方法
    public final boolean sendMessage(Message msg){
        return sendMessageDelayed(msg, 0);
    }
    public final boolean sendMessageDelayed(Message msg, long delayMillis){
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        return sendMessageAtTime(msg, SystemClock.uptimeMillis() + delayMillis);
    }
    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        // 获取Looper中的队列
        MessageQueue queue = mQueue;
        if (queue == null) {
            RuntimeException e = new RuntimeException(
                    this + " sendMessageAtTime() called with no mQueue");
            Log.w("Looper", e.getMessage(), e);
            return false;
        }
        // 将消息压入Looper中的队列中
        return enqueueMessage(queue, msg, uptimeMillis);
    }
    private boolean enqueueMessage(MessageQueue queue, Message msg, long uptimeMillis) {
        // 设置消息的target变量为本Handler
        msg.target = this;
        if (mAsynchronous) {
            msg.setAsynchronous(true);
        }
        return queue.enqueueMessage(msg, uptimeMillis);
    }
```
dispatchMessage方法会先判断msg.callback是否为空，判断出是该消息msg是通过post发送出的还是
sendMessage方法发送出的，从而在对消息进行处理。Handler有众多构造方法，不过最终调用的不过就是构造方法6和7两个，这俩构造方法的区别之处就在于第一个参数，是否自定义设置Looper，若步自定义设置Looper则默认使用主线程的Looper。sendMessage方法从源码中可以看到最终是调用
sendMessageAtTime方法将消息压入Looper中的队列的，而又是在压入队列enqueueMessage方法中设置的消息msg的target变量的Handler值。

MessageQueue中的源码就不分析啦，有兴趣的小伙伴自行查看吧。再放上一张Handler、Message、Message Queue、Looper之间的逻辑图

![](https://user-gold-cdn.xitu.io/2019/4/14/16a1a428da55be6f?w=1123&h=794&f=jpeg&s=77272)

* 最后来看看前面说的ThreadLocal：线程本地存储区（Thread Local Storage，简称为TLS），每个线程都有自己的私有的本地存储区域，不同线程之间彼此不能访问对方的
TLS区域。这里线程自己的本地存储区域存放是线程自己的Looper。
```
public final class Looper {
    // sThreadLocal 是static的变量，可以先简单理解它相当于map，key是线程，value是Looper，
    //那么你只要用当前的线程就能通过sThreadLocal获取当前线程所属的Looper。
    static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal<Looper>();
    //主线程（UI线程）的Looper 单独处理，是static类型的，通过下面的方法getMainLooper() 
    //可以方便的获取主线程的Looper。
    private static Looper sMainLooper; 

    //Looper 所属的线程的消息队列
    final MessageQueue mQueue;
    //Looper 所属的线程
    final Thread mThread;
    ......
}
```
说到ThreadLocal，不得不说下ThreadLocalMap和Thread

1.ThreadLocal类用于存储以线程为作用域的数据，线程之间数据隔离。 

2.ThreadLocalMap类是ThreadLocal的静态内部类，通过操作Entry来存储数据。

3.Thread类比较常用，线程类内部维持一个ThreadLocalMap类实例（threadLocals）。 

handler消息机制中的Looper就存储在ThreadLocal中，也正是因为Looper的创建是依赖当前线程的，一个线程只能有唯一一个Looper，所以Looper使用于存储在ThreadLocal中。ThreadLocal类会根据当前线程存储变量数据的副本，每一个线程之间数据副本互不干扰，实现线程之间的数据隔离。
```
// ThreadLocal类：
    public void set(T value) {
        // 当前线程
        Thread t = Thread.currentThread();
        // 获取当前线程的ThreadLocalMap
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
    }
    ThreadLocalMap getMap(Thread t) {
        // 返回当前线程的ThreadLocalMap
        return t.threadLocals;
    }
    void createMap(Thread t, T firstValue) {
        t.threadLocals = new ThreadLocalMap(this, firstValue);
    }
    
// Looper类
    // Looper初始化时会调用该方法，上面Handler机制Hanlder部分提到过
    private static void prepare(boolean quitAllowed) {
        if (sThreadLocal.get() != null) {
            throw new RuntimeException("Only one Looper may be created per thread");
        }
        // 将Looper设置到ThreadLocal中
        sThreadLocal.set(new Looper(quitAllowed));
    }
```
Looper类中的prepare方法，再结合ThreadLocal类中的set方法，可以看出Looper实例是绑定到线程内部的ThreadLocalMap成员变量中的，因此一个线程只能绑定一个Looper，而且由于每个Thread都会维护一个ThreadLocal.ThreadLocalMap类，因此这就隔绝了多线程间通讯混乱的问题，保证了绝对线程安全。
若Thread中的ThreadLocalMap为空，则在set方法中会create创建。

此时就延申到了ThreadLocal的用处啦，简单说一下：当某些数据是以线程为作用域并且不同线程具有不同的数据副本的时候，就可以考虑采用ThreadLocal。现在来张Thread、ThreadLocalMap和 ThreadLocal关系图：
![](https://user-gold-cdn.xitu.io/2019/4/14/16a1b3f4a8069f33?w=941&h=562&f=jpeg&s=73939)
至此Handler机制完成。