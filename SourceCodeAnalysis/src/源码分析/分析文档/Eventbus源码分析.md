主流开源框架源码深入了解第6篇——EventBus源码分析。(源码以3.1.1版为准)

![](https://user-gold-cdn.xitu.io/2019/12/29/16f520a780ddf968?w=300&h=300&f=png&s=51701)
## EventBus简介
EventBus是一个Android事件发布/订阅轻量级框架，通过此框架可以解耦发布者和订阅者，可以简化Android的事件传递。事件传递既可以用于Android的四大组件间的通讯，也可以用于异步线程和主线程通讯。其最大的优点在于，代码简洁、使用简单以及将事件的发布和订阅充分的解耦。

![](https://user-gold-cdn.xitu.io/2019/12/30/16f56ea6562e575b?w=800&h=299&f=png&s=69649)
### EventBus使用
```
public class EventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.i("onStart1");
        // 官方demo 注册事件在onStart中
        EventBus.getDefault().register(this);
    }

    /**
     * @Subscribe 订阅者方法
     * 订阅者方法将在发布事件所在的线程中被调用。这是 默认的线程模式。事件的传递是同步的，
     * 一旦发布事件，所有该模式的订阅者方法都将被调用。这种线程模式意味着最少的性能开销，
     * 因为它避免了线程的切换。因此，对于不要求是主线程并且耗时很短的简单任务推荐使用该模式。
     * 使用该模式的订阅者方法应该快速返回，以避免阻塞发布事件的线程，这可能是主线程。
     * 注：POSTING 就是和发布事件 post 所在一个线程中，post为 主/子 线程POSTING就为 主/子 线程中
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onMessageEventPosting(EventBean member) {
        LogUtil.i(member.toString());
        LogUtil.i("onMessageEventPosting(), current thread is " + Thread.currentThread().getName());
    }

    /**
     * @Subscribe 订阅者方法
     * 注：不管post发布事件在什么线程中，MAIN 都在主线程
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(EventBean member) {
        LogUtil.i(member.toString());
        TextView viewById = (TextView) findViewById(R.id.tv_event1);
        viewById.setText(member.toString());
        ToastUtil.showShortToast(member.toString());
        LogUtil.i("onMessageEventMain(), current thread is " + Thread.currentThread().getName());
    }

    /**
     * @Subscribe 订阅者方法
     * 订阅者方法将在主线程（UI线程）中被调用。因此，可以在该模式的订阅者方法中直接更新UI界面。
     * 事件将先进入队列然后才发送给订阅者，所以发布事件的调用将立即返回。
     * 这使得事件的处理保持严格的串行顺序。使用该模式的订阅者方法必须快速返回，以避免阻塞主线程。
     * 注：不管post发布事件在什么线程中，MAIN_ORDERED 也都在主线程，不过该模式事件是串行的，按先后顺序的
     */
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onMessageEventMainOrdered(EventBean member) {
        LogUtil.i("onMessageEventMainOrdered(), current thread is " + Thread.currentThread().getName());
    }

    /**
     * @Subscribe 订阅者方法
     * 注：post（发布者）若为子线程，则 BACKGROUND 则是于post同一子线程中，若post为主线程，则BACKGROUND为单独的后台线程
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEventBackground(EventBean member) {
        LogUtil.i("onMessageEventBackground(), current thread is " + Thread.currentThread().getName());
    }

    /**
     * @Subscribe 订阅者方法
     * 注：post（发布者）无论是在子线程还是主线程，ASYNC 都会单开一个线程
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageEventAsync(EventBean member) {
        LogUtil.i("onMessageEventAsync(), current thread is " + Thread.currentThread().getName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.i("onStop1");
        // 官方demo 解绑事件在onStop中
        EventBus.getDefault().unregister(this);
    }
}
```
### EventBus分析流程
1. EventBus初始化
2. EventBus成员属性
3. EventBus线程切换
4. Subscribe注解
5. EventBus注册
6. 发布事件EventBus 
7. 取消注册unregister
8. Subscriber 索引
## EventBus源码分析
### EventBus初始化
```
private static final EventBusBuilder DEFAULT_BUILDER = new EventBusBuilder();

// 方法一：EventBus.getDefault()，EventBus为应用提供便利的单例
    public static EventBus getDefault() {
        // 双重校验并加锁的单例模式
        if (defaultInstance == null) {
            synchronized (EventBus.class) {
                if (defaultInstance == null) {
                    defaultInstance = new EventBus();
                }
            }
        }
        return defaultInstance;
    }
// 方法二：new EventBus(); ，创建一个新的EventBus实例；每个实例都是传递事件的单独范围。要使用中央总线，请考虑使用#getDefault()方式。
    public EventBus() {
        this(DEFAULT_BUILDER);
    }
```
1. EventBus初始化有两种方式，一种是EventBus自己为开发者提供的便利用法，第二种则是直接实例化对象。
2. 为什么EventBus的构造方法是public的呢？
这就要说到EventBus在我们项目中，并不是仅仅存在一条总线，也存在其它的总线。因此，我们订阅者也可以订阅到其它的总线之上，然后通过不同的EventBus发送数据。那么，我们需要注意的是，不同的EventBus发送的数据，是相互隔离开来的，订阅者只会收到注册到当前的EventBus发送的数据。
3. 我们最后看一下EventBus构造方法中传入的DEFAULT_BUILDER，实际上是EventBusBuilder。因此我们就可以了解到EventBus是通过建造者模式初始化的实例。
### EventBus成员属性
```
// EventBus类：
    private static final Map<Class<?>, List<Class<?>>> eventTypesCache = new HashMap<>();

    // 以Event事件为key，以subscriptions订阅者为value，因此当发送Event时可通过该hashmap找到订阅此事件的订阅者
    private final Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscriptionsByEventType;
    // 以Subscriber订阅者为key，以types类型为value，因此当发送注册和反注册时都会操作此hashmap
    private final Map<Object, List<Class<?>>> typesBySubscriber;
    // 维护粘性事件，使用并发的hashmap保存
    private final Map<Class<?>, Object> stickyEvents;

    // 线程内部数据存储类，在指定的线程中存储数据，也只能在指定线程中获取到存储数据。
    private final ThreadLocal<PostingThreadState> currentPostingThreadState = new ThreadLocal<PostingThreadState>() {
        @Override
        protected PostingThreadState initialValue() {
            return new PostingThreadState();
        }
    };

    // @Nullable
    private final MainThreadSupport mainThreadSupport;
    // @Nullable
    private final Poster mainThreadPoster;
    private final BackgroundPoster backgroundPoster;
    private final AsyncPoster asyncPoster;
    private final SubscriberMethodFinder subscriberMethodFinder;
    private final ExecutorService executorService;

    private final boolean throwSubscriberException;
    private final boolean logSubscriberExceptions;
    private final boolean logNoSubscriberMessages;
    private final boolean sendSubscriberExceptionEvent;
    private final boolean sendNoSubscriberEvent;
    private final boolean eventInheritance;

    private final int indexCount;
    private final Logger logger;

    EventBus(EventBusBuilder builder) {
        // 日志打印
        logger = builder.getLogger();
        // 事件对应的 订阅者和订阅者方法集合映射的封装类 存储
        subscriptionsByEventType = new HashMap<>();
        // 注册的订阅者存储
        typesBySubscriber = new HashMap<>();
        // 粘性事件存储
        stickyEvents = new ConcurrentHashMap<>();
        mainThreadSupport = builder.getMainThreadSupport();
        // Android主线程处理事件
        mainThreadPoster = mainThreadSupport != null ? mainThreadSupport.createPoster(this) : null;
        // Background事件发送者
        backgroundPoster = new BackgroundPoster(this);
        // 异步事件发送者
        asyncPoster = new AsyncPoster(this);
        indexCount = builder.subscriberInfoIndexes != null ? builder.subscriberInfoIndexes.size() : 0;
        // @Subscribe注解方法找寻器
        subscriberMethodFinder = new SubscriberMethodFinder(builder.subscriberInfoIndexes,
                builder.strictMethodVerification, builder.ignoreGeneratedIndex);
        // 标识1：当调用事件处理函数时，若发生了异常是否进行异常信息打印。默认true
        logSubscriberExceptions = builder.logSubscriberExceptions;
        // 标识2：当没有订阅者订阅该事件时，是否打印日志。默认true
        logNoSubscriberMessages = builder.logNoSubscriberMessages;
        // 标识3：当调用事件处理函数时，若发生了异常是否发送SubscriberExceptionEvent这个事件。默认true
        sendSubscriberExceptionEvent = builder.sendSubscriberExceptionEvent;
        // 标识4：当没有订阅者订阅该事件时，是否发送NoSubscriberEvent事件。默认true
        sendNoSubscriberEvent = builder.sendNoSubscriberEvent;
        // 标识5：是否抛出SubscriberException异常。默认false
        throwSubscriberException = builder.throwSubscriberException;
        // 标识6：与event有继承关系的是否都需要发送。默认true
        eventInheritance = builder.eventInheritance;
        // 执行服务线程池
        executorService = builder.executorService;
    }
```
### EventBus线程切换
EventBus的成员属性中，我们来简单看一下其非常重要的三个Poster：mainThreadPoster、backgroundPoster、asyncPoster

1. mainThreadPoster：用于主线程切换。
    ```
    // EventBusBuilder类：
        MainThreadSupport getMainThreadSupport() {
            if (mainThreadSupport != null) {
                return mainThreadSupport;
            } else if (Logger.AndroidLogger.isAndroidLogAvailable()) {
                // 获取主线程Looper，正常情况不会为null
                Object looperOrNull = getAndroidMainLooperOrNull();
                return looperOrNull == null ? null :
                        // 初始化MainThreadSupport，实际上是AndroidHandlerMainThreadSupport对象
                        new MainThreadSupport.AndroidHandlerMainThreadSupport((Looper) looperOrNull);
            } else {
                return null;
            }
        }

        Object getAndroidMainLooperOrNull() {
            try {
                // 获取主线程Looper
                return Looper.getMainLooper();
            } catch (RuntimeException e) {
                // 并不是真正的功能性Android（例如，“ Stub！” Maven依赖项）
                return null;
            }
        }

        public interface MainThreadSupport {

            boolean isMainThread();

            Poster createPoster(EventBus eventBus);

            class AndroidHandlerMainThreadSupport implements MainThreadSupport {

                private final Looper looper;

                public AndroidHandlerMainThreadSupport(Looper looper) {
                    // 初始化looper属性，此处就是上面传进来的主线程Looper
                    this.looper = looper;
                }

                @Override
                public boolean isMainThread() {
                    // 判断是否为主线程
                    return looper == Looper.myLooper();
                }

                @Override
                public Poster createPoster(EventBus eventBus) {
                    // 初始化HandlerPoster，实际是Handler子类
                    return new HandlerPoster(eventBus, looper, 10);
                }
            }

        }
    ```
    从构造函数中mainThreadPoster初始化，实际上是通过mainThreadSupport.createPoster(this)，而mainThreadSupport通过分析，我们可以了解实际上是AndroidHandlerMainThreadSupport类的对象。因此通过调用其createPoster(this)方法最后返回了HandlerPoster。
    ```
    // 继承自Handler
    public class HandlerPoster extends Handler implements Poster {

        // PendingPostQueue队列，用于保存用于即将执行的，待发送的post队列
        private final PendingPostQueue queue;
        // 表示post事件可以最大的在HandlerMessage中存活时间。规定最大的运行时间，因为运行在主线程，不能阻塞主线程。
        private final int maxMillisInsideHandleMessage;
        private final EventBus eventBus;
        // 标识Handler是否有效，即是否运行起来啦
        private boolean handlerActive;

        protected HandlerPoster(EventBus eventBus, Looper looper, int maxMillisInsideHandleMessage) {
            // 此处传入通过调用createPoster(this)方法，
            // 传入保存到AndroidHandlerMainThreadSupport成员属性的主线程looper。
            // 因此HandlerPoster是主线程的Handler，用于将EventBus异步线程消息切换回主线程。
            super(looper);
            // 将createPoster(this)，传入的this，即EventBus保存
            this.eventBus = eventBus;
            this.maxMillisInsideHandleMessage = maxMillisInsideHandleMessage;
            queue = new PendingPostQueue();
        }

        // 消息入队，若此Handler已运行，则直接发送obtainMessage消息，最终回到下面的handleMessage中处理入队消息。
        public void enqueue(Subscription subscription, Object event) {
            // 根据传进来的参数封装PendingPost待处理post对象。PendingPost的封装，使用了数据缓存池。
            PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
            synchronized (this) {
                // 待处理消息入队
                queue.enqueue(pendingPost);
                if (!handlerActive) {
                    handlerActive = true;
                    // 调用sendMessage，发送事件回到主线程，最终会调用下面的handleMessage方法
                    if (!sendMessage(obtainMessage())) {
                        throw new EventBusException("Could not send handler message");
                    }
                }
            }
        }

        // Handler中最重要的方法就是handleMessage，所有的消息处理最后都会经过此方法。
        @Override
        public void handleMessage(Message msg) {
            boolean rescheduled = false;
            try {
                long started = SystemClock.uptimeMillis();
                // 无限循环
                while (true) {
                    // 从队列中取出消息
                    PendingPost pendingPost = queue.poll();
                    // 双重判断并加锁，若pendingPost为null，则返回
                    if (pendingPost == null) {
                        synchronized (this) {
                            // Check again, this time in synchronized
                            pendingPost = queue.poll();
                            if (pendingPost == null) {
                                handlerActive = false;
                                return;
                            }
                        }
                    }
                    // 使用反射的方法调用订阅者的订阅方法，进行事件的分发。
                    eventBus.invokeSubscriber(pendingPost);
                    long timeInMethod = SystemClock.uptimeMillis() - started;
                    // 每次分发完事件都会重新比较一下处于此循环的时间，若大于最大设定值则返回
                    if (timeInMethod >= maxMillisInsideHandleMessage) {
                        if (!sendMessage(obtainMessage())) {
                            throw new EventBusException("Could not send handler message");
                        }
                        rescheduled = true;
                        return;
                    }
                }
            } finally {
                handlerActive = rescheduled;
            }
        }
    }
    ```
2. backgroundPoster：后台消息切换。
    ```
    // 实现Runnable接口
    final class BackgroundPoster implements Runnable, Poster {

        private final PendingPostQueue queue;
        private final EventBus eventBus;
        // 标识此Runnable是否正在运行
        private volatile boolean executorRunning;

        BackgroundPoster(EventBus eventBus) {
            // 初始化eventBus属性
            this.eventBus = eventBus;
            // 初始化待处理post队列
            queue = new PendingPostQueue();
        }

        // 消息入队，若此Runnable正在运行，则通过eventBus获取其初始化时构造的线程池，执行当前任务，最终回到下面的run方法中。
        public void enqueue(Subscription subscription, Object event) {
            // 根据传进来的参数封装PendingPost待处理post对象。PendingPost的封装，使用了数据缓存池。
            PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
            synchronized (this) {
                // 消息入队
                queue.enqueue(pendingPost);
                if (!executorRunning) {
                    executorRunning = true;
                    // 通过eventBus成员获取在EventBus构造方法中初始化的线程池，然后执行当前Runnable
                    eventBus.getExecutorService().execute(this);
                }
            }
        }

        @Override
        public void run() {
            try {
                try {
                    while (true) {
                        // 双重判断并加锁，若pendingPost为null，则返回
                        PendingPost pendingPost = queue.poll(1000);
                        if (pendingPost == null) {
                            synchronized (this) {
                                // Check again, this time in synchronized
                                pendingPost = queue.poll();
                                if (pendingPost == null) {
                                    executorRunning = false;
                                    return;
                                }
                            }
                        }
                        // 通过eventBus执行分发事件，此while循环会将队列中所有消息都取出，并分发。
                        eventBus.invokeSubscriber(pendingPost);
                    }
                } catch (InterruptedException e) {
                    eventBus.getLogger().log(Level.WARNING, Thread.currentThread().getName() + " was interruppted", e);
                }
            } finally {
                executorRunning = false;
            }
        }
    }
    ```
3. asyncPoster：
    ```
    // 实现Runnable接口
    class AsyncPoster implements Runnable, Poster {

        private final PendingPostQueue queue;
        private final EventBus eventBus;

        AsyncPoster(EventBus eventBus) {
            this.eventBus = eventBus;
            queue = new PendingPostQueue();
        }

        // 消息入队，直接使用evnetBus中线程池执行当前Runnable，最终回到run方法中
        public void enqueue(Subscription subscription, Object event) {
            PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
            queue.enqueue(pendingPost);
            eventBus.getExecutorService().execute(this);
        }

        @Override
        public void run() {
            // 获取消息队列中的消息
            PendingPost pendingPost = queue.poll();
            if(pendingPost == null) {
                throw new IllegalStateException("No pending post available");
            }
            // 只执行消息队列中的一个消息即结束
            eventBus.invokeSubscriber(pendingPost);
        }
    }
    ```

#### 小结
1. 三个Poster均实现了Poster接口，此接口只有enqueue一个方法。此方法用于消息入队，然后通过Handler发送obtainMessage消息或者线程池执行当前Runnable任务，最终回到消息队列处理的方法中，即handleMessage或者run方法。
2. backgroundPoster和asyncPoster实际上很相似，差别在于backgroundPoster会一次性将消息队列中的消息通过eventBus分发完，而asyncPoster一次只分发一个。
3. 三个Poster入队的消息最后都被封装为PendingPost对象，此类的封装，使用了数据缓存池。PendingPost类内部维护一个类型PendingPost的ArrayList，最大容量为10000。每一个PendingPost消息被分发完成，都会调用PendingPost#releasePendingPost方法释放其内部成员属性值，并将其添加到内部集合中，已备调用obtainPendingPost而复用，这样就减少了初始化PendingPost对象的时间，直接更改其内部属性值以提高效率。
    ```
    final class PendingPost {
        private final static List<PendingPost> pendingPostPool = new ArrayList<PendingPost>();

        Object event;
        Subscription subscription;
        PendingPost next;

        private PendingPost(Object event, Subscription subscription) {
            this.event = event;
            this.subscription = subscription;
        }
        // 复用或者创建新的obtainPendingPost
        static PendingPost obtainPendingPost(Subscription subscription, Object event) {
            synchronized (pendingPostPool) {
                int size = pendingPostPool.size();
                if (size > 0) {
                    // 移除并获取最后一位PendingPost
                    PendingPost pendingPost = pendingPostPool.remove(size - 1);
                    // 重置成员属性值
                    pendingPost.event = event;
                    pendingPost.subscription = subscription;
                    pendingPost.next = null;
                    return pendingPost;
                }
            }
            return new PendingPost(event, subscription);
        }
        // 释放PendingPost成员属性保存的值
        static void releasePendingPost(PendingPost pendingPost) {
            // 清空属性值
            pendingPost.event = null;
            pendingPost.subscription = null;
            pendingPost.next = null;
            synchronized (pendingPostPool) {
                // Don't let the pool grow indefinitely
                if (pendingPostPool.size() < 10000) {
                    pendingPostPool.add(pendingPost);
                }
            }
        }
    }
    ```
### Subscribe注解
```
// 注解的使用：@Subscribe(threadMode = ThreadMode.MAIN, sticky = false, priority = 2)

/**
 * Java四大元注解：
 * 1. @Target ：用于描述注解的使用范围，也就是说使用了@Target去定义一个注解，那么可以决定定义好的注解能用在什么地方
 *    内部值：ElementType[]  value()数组， value值类型   ElementType枚举类型
 *    元注解中的枚举值决定了，一个注解可以标记的范围
 *      TYPE ： 只能用在类、接口、枚举类型上。
 *      FIELD ：字段声明和枚举常量
 *      METHOD ：只能用在方法声明上
 *      PARAMETER ： 只能用在参数的声明上 【参数名】
 *      CONSTRUCTOR ： 只能用在构造方法的声明上
 *      LOCAL_VARIABLE ： 只能用在局部变量声明上
 *      ANNOTATION_TYPE ：只能用在注解上
 *      PACKAGE ：只能用在包上
 *      TYPE_PARAMETER ： 参数类型【形式参数类型】
 *      TYPE_USE ： 任何位置都可以
 * 2. @Retention：用于描述一个注解存在的生命周期【源码，字节码文件，运行时】
 * 	    内部值：RetentionPolicy value();非数组，意味着只能一个值
 * 	    值类型：枚举值RetentionPolicy，几个值决定了几个状态。如下几个值：
 * 		SOURCE ：表示一个注解可以存在于源码中==>java的源码中
 *      CLASS ：表示 一个注解可以在源码中，并且可以在字节码文件中
 *      RUNTIME ：表示 一个注解可以在源码、字节码、及运行时期该注解都会存在
 * 3. @Document ：使用了该注解后，将自定义注解设置为文档说明内容，在生成javadoc时会将该注解加入到文档中。
 * 4. @Inherited ：用于标注一个父类的注解是否可以被子类继承，如果一个注解需要被其子类所继承，则在声明时直接使用@Inherited注解即可。如果没有写此注解，则无法被子类继承。
 * 	    不是说注解与注解之间能否相互继承，而是说：一个类A被注解了，那么另外一个类B，继承了A类，B类能够继承到A类中，的注解 (即被@Inherited注解过的注解)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Subscribe {
    // 线程模式：对应mainThreadPoster、backgroundPoster、asyncPoster这三个Poster
    ThreadMode threadMode() default ThreadMode.POSTING; // 默认的POSTING，无论消息处于什么线程，都直接分发，不做线程切换

    // 是否是粘性事件
    boolean sticky() default false;

    // 订户优先级影响事件传递的顺序。在同一传送线程ThreadMode中，优先级较高的订户将在优先级较低的其他订户之前接收事件。默认优先级为0。
    // 注意：优先级不会影响具有不同ThreadMode的订户之间发送的顺序！
    int priority() default 0;
}
```
### 注册EventBus—register(this)
```
// EventBus类：
    public void register(Object subscriber) {
        // 获取订阅者的Class对象
        Class<?> subscriberClass = subscriber.getClass();
        // 通过订户方法查找器(通过注解查找)找到订阅者里订阅方法的集合
        List<SubscriberMethod> subscriberMethods = subscriberMethodFinder.findSubscriberMethods(subscriberClass);
        synchronized (this) {
            // 遍历集合按个执行订阅
            for (SubscriberMethod subscriberMethod : subscriberMethods) {
                subscribe(subscriber, subscriberMethod);
            }
        }
    }
```
注册方法中总共完成两件事情：1. 查找订阅方法集合   2. 遍历集合按个订阅

* 查找订阅方法集合
    ```
    // 订阅方法对象
    public class SubscriberMethod {
        final Method method;    // 订阅的方法
        final ThreadMode threadMode;    // 线程模式
        final Class<?> eventType;   // 事件类型：即我们订阅方法的唯一参数，就是事件类型；同时也是post(T)的参数。
        final int priority; // 优先级
        final boolean sticky;   // 是否为粘性事件
        String methodString;    // 用于有效比较，是否同一个订阅方法
    }

    // 订阅方法查找器
    class SubscriberMethodFinder {
        // 用于缓存订阅者和订阅者订阅方法集合
        private static final Map<Class<?>, List<SubscriberMethod>> METHOD_CACHE = new ConcurrentHashMap<>();
        private static final int POOL_SIZE = 4;
        // FindState数组，缓存大小为4
        private static final FindState[] FIND_STATE_POOL = new FindState[POOL_SIZE];

        // 查找订阅方法集合
        List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
            List<SubscriberMethod> subscriberMethods = METHOD_CACHE.get(subscriberClass);
            // 先查找缓存
            if (subscriberMethods != null) {
                return subscriberMethods;
            }
            // 默认为0
            if (ignoreGeneratedIndex) {
                // 使用反射查找，耗费性能
                subscriberMethods = findUsingReflection(subscriberClass);
            } else {
                // 查找使用信息
                subscriberMethods = findUsingInfo(subscriberClass);
            }
            if (subscriberMethods.isEmpty()) {
                throw new EventBusException("Subscriber " + subscriberClass
                        + " and its super classes have no public methods with the @Subscribe annotation");
            } else {
                METHOD_CACHE.put(subscriberClass, subscriberMethods);
                return subscriberMethods;
            }
        }

        // FindState中间器，用于查找保存状态
        static class FindState {
            // 保存订阅方法
            final List<SubscriberMethod> subscriberMethods = new ArrayList<>();
            // 以事件类型为key，方法为value
            final Map<Class, Object> anyMethodByEventType = new HashMap<>();
            // 以方法为key，订阅者的Class对象为value
            final Map<String, Class> subscriberClassByMethodKey = new HashMap<>();
            final StringBuilder methodKeyBuilder = new StringBuilder(128);

            Class<?> subscriberClass;
            Class<?> clazz;
            boolean skipSuperClasses;
            SubscriberInfo subscriberInfo;
            // 初始化传入订阅类
            void initForSubscriber(Class<?> subscriberClass) {
                this.subscriberClass = clazz = subscriberClass;
                skipSuperClasses = false;
                subscriberInfo = null;
            }

            // 回收释放，已备复用
            void recycle() {
                ...
            }

            // 用来判断FindState的anyMethodByEventType map是否已经添加过以当前eventType为key的键值对，没添加过则返回true
            boolean checkAdd(Method method, Class<?> eventType) {
                ...
            }

            // 移动到父类Class
            void moveToSuperclass() {
                ...
            }
        }

        private List<SubscriberMethod> findUsingInfo(Class<?> subscriberClass) {
            // 准备FindState对象
            FindState findState = prepareFindState();
            // findState中间器初始化订阅者
            findState.initForSubscriber(subscriberClass);
            while (findState.clazz != null) {
                // 获取subscriberInfo，默认为null。此方法会在【Subscriber 索引】详细讲解。
                findState.subscriberInfo = getSubscriberInfo(findState);
                if (findState.subscriberInfo != null) {
                    SubscriberMethod[] array = findState.subscriberInfo.getSubscriberMethods();
                    for (SubscriberMethod subscriberMethod : array) {
                        if (findState.checkAdd(subscriberMethod.method, subscriberMethod.eventType)) {
                            findState.subscriberMethods.add(subscriberMethod);
                        }
                    }
                } else {
                    // 通过反射查找
                    findUsingReflectionInSingleClass(findState);
                }
                // 转移到父类Class
                findState.moveToSuperclass();
            }
            // 通过findState获取方法并缓存findState
            return getMethodsAndRelease(findState);
        }

        // FindState的缓存大小为4，并使用一维的静态数组，所以这里需要注意线程同步的问题
        private FindState prepareFindState() {
            // 若有缓存则直接返回，否则创建
            synchronized (FIND_STATE_POOL) {
                for (int i = 0; i < POOL_SIZE; i++) {
                    FindState state = FIND_STATE_POOL[i];
                    if (state != null) {
                        FIND_STATE_POOL[i] = null;
                        return state;
                    }
                }
            }
            return new FindState();
        }

        // 在单个类中使用反射查找订阅方法
        private void findUsingReflectionInSingleClass(FindState findState) {
            Method[] methods;
            try {
                // 获取所有声明的方法
                methods = findState.clazz.getDeclaredMethods();
            } catch (Throwable th) {
                // 获取订阅者中声明的public方法，跳过父类
                methods = findState.clazz.getMethods();
                findState.skipSuperClasses = true;
            }
            for (Method method : methods) {
                // 获得方法的修饰符
                int modifiers = method.getModifiers();
                // 如果是public类型，但非abstract、static等
                if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
                    // 获得当前方法所有参数的类型
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    // 如果当前方法只有一个参数
                    if (parameterTypes.length == 1) {
                        Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
                        // 如果当前方法使用了Subscribe注解
                        if (subscribeAnnotation != null) {
                            // 得到该方法参数的类型
                            Class<?> eventType = parameterTypes[0];
                            // checkAdd()方法用来判断FindState的anyMethodByEventType map是否已经添加过以当前eventType为key的键值对，没添加过则返回true
                            if (findState.checkAdd(method, eventType)) {
                                // 得到Subscribe注解的threadMode属性值，即线程模式
                                ThreadMode threadMode = subscribeAnnotation.threadMode();
                                // 创建一个SubscriberMethod对象，并添加到subscriberMethods集合
                                findState.subscriberMethods.add(new SubscriberMethod(method, eventType, threadMode,
                                        subscribeAnnotation.priority(), subscribeAnnotation.sticky()));
                            }
                        }
                    } else if (strictMethodVerification && method.isAnnotationPresent(Subscribe.class)) {
                        String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                        throw new EventBusException("@Subscribe method " + methodName +
                                "must have exactly 1 parameter but has " + parameterTypes.length);
                    }
                } else if (strictMethodVerification && method.isAnnotationPresent(Subscribe.class)) {
                    String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                    throw new EventBusException(methodName +
                            " is a illegal @Subscribe method: must be public, non-static, and non-abstract");
                }
            }
        }

        // 从findState中间件中获取订阅的方法集合，并释放findState中间件，最后将findState缓存到数组中
        private List<SubscriberMethod> getMethodsAndRelease(FindState findState) {
            List<SubscriberMethod> subscriberMethods = new ArrayList<>(findState.subscriberMethods);
            findState.recycle();
            synchronized (FIND_STATE_POOL) {
                for (int i = 0; i < POOL_SIZE; i++) {
                    if (FIND_STATE_POOL[i] == null) {
                        FIND_STATE_POOL[i] = findState;
                        break;
                    }
                }
            }
            return subscriberMethods;
        }
    }
    ```
* 遍历集合按个订阅
    ```
    // EventBus类：
        // 必须在同步块中调用
        private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
             // 得到当前订阅了事件的方法的参数类型
            Class<?> eventType = subscriberMethod.eventType;
            // Subscription类保存了要注册的类对象以及当前的subscriberMethod
            Subscription newSubscription = new Subscription(subscriber, subscriberMethod);
            // subscriptionsByEventType是一个HashMap，保存了以eventType为key,Subscription对象集合为value的键值对
            // 先查找subscriptionsByEventType是否存在以当前eventType为key的值
            CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
            // 如果不存在，则创建一个subscriptions，并保存到subscriptionsByEventType
            if (subscriptions == null) {
                subscriptions = new CopyOnWriteArrayList<>();
                // '绑定订阅者、订阅方法和事件类型的集合，subscriptions真正的值，在下面for循环中根据订阅方法优先级添加'
                subscriptionsByEventType.put(eventType, subscriptions);
            } else {
                if (subscriptions.contains(newSubscription)) {
                    throw new EventBusException("Subscriber " + subscriber.getClass() + " already registered to event "
                            + eventType);
                }
            }
            // 添加上边创建的newSubscription对象到subscriptions中
            int size = subscriptions.size();
            for (int i = 0; i <= size; i++) {
                // 按照优先级将订阅方法添加置subscriptions中，实际上是保存到subscriptionsByEventType集合中
                if (i == size || subscriberMethod.priority > subscriptions.get(i).subscriberMethod.priority) {
                    subscriptions.add(i, newSubscription);
                    break;
                }
            }
            // typesBySubscribere也是一个HashMap，保存了以当前要注册类的对象为key，注册类中订阅事件的方法的参数类型的集合为value的键值对
            // 查找是否存在对应的参数类型集合
            List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
            // 不存在则创建一个subscribedEvents，并保存到typesBySubscriber
            if (subscribedEvents == null) {
                subscribedEvents = new ArrayList<>();
                typesBySubscriber.put(subscriber, subscribedEvents);
            }
            // 保存当前订阅了事件的方法的参数类型
            subscribedEvents.add(eventType);
            // 粘性事件相关的
            if (subscriberMethod.sticky) {
                // 判断事件的继承性，默认是true
                if (eventInheritance) {
                    // 获取所有粘性事件并遍历，stickyEvents就是发送粘性事件时，保存了事件类型和对应事件
                    Set<Map.Entry<Class<?>, Object>> entries = stickyEvents.entrySet();
                    for (Map.Entry<Class<?>, Object> entry : entries) {
                        Class<?> candidateEventType = entry.getKey();
                        // isAssignableFrom：判定此 Class 对象所表示的类或接口与指定的 Class 参数所表示的类或接口是否相同，或是否是其超类或超接口。如果是则返回 true；否则返回 false。
                        if (eventType.isAssignableFrom(candidateEventType)) {
                            // 获取粘性事件
                            Object stickyEvent = entry.getValue();
                            // 处理粘性事件
                            checkPostStickyEventToSubscription(newSubscription, stickyEvent);
                        }
                    }
                } else {
                    // 获取并处理粘性事件
                    Object stickyEvent = stickyEvents.get(eventType);
                    checkPostStickyEventToSubscription(newSubscription, stickyEvent);
                }
            }
        }

        // 处理粘性事件
        private void checkPostStickyEventToSubscription(Subscription newSubscription, Object stickyEvent) {
            if (stickyEvent != null) {
                // 发布到订阅者相应方法中
                postToSubscription(newSubscription, stickyEvent, isMainThread());
            }
        }

        // 粘性事件订阅者，在注册时，会及时收到事件，就是在注册后通过此方法将粘性事件及时发布的
        private void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
            // 判断订阅事件方法的线程模式
            switch (subscription.subscriberMethod.threadMode) {
                case POSTING:
                    // 默认的线程模式，在那个线程发送事件就在那个线程处理事件
                    invokeSubscriber(subscription, event);
                    break;
                case MAIN:  // 在主线程处理事件
                    if (isMainThread) {
                        // 如果在主线程发送事件，则直接在主线程通过反射处理事件
                        invokeSubscriber(subscription, event);
                    } else {
                        // 如果是在子线程发送事件，则将事件入队列，通过Handler切换到主线程执行处理事件
                        mainThreadPoster.enqueue(subscription, event);
                    }
                    break;
                case MAIN_ORDERED:
                    // 无论在那个线程发送事件，都先将事件入队列，然后通过 Handler 切换到主线程，依次处理事件。mainThreadPoster不会为null
                    if (mainThreadPoster != null) {
                        mainThreadPoster.enqueue(subscription, event);
                    } else {
                        // temporary: technically not correct as poster not decoupled from subscriber
                        invokeSubscriber(subscription, event);
                    }
                    break;
                case BACKGROUND:
                    if (isMainThread) {
                        // 如果在主线程发送事件，则先将事件入队列，然后通过线程池依次处理事件
                        backgroundPoster.enqueue(subscription, event);
                    } else {
                        // 如果在子线程发送事件，则直接在发送事件的线程通过反射处理事件
                        invokeSubscriber(subscription, event);
                    }
                    break;
                case ASYNC:
                    // 无论在那个线程发送事件，都将事件入队列，然后通过线程池处理。
                    asyncPoster.enqueue(subscription, event);
                    break;
                default:
                    throw new IllegalStateException("Unknown thread mode: " + subscription.subscriberMethod.threadMode);
            }
        }

        // 用反射来执行订阅事件的方法，这样发送出去的事件就被订阅者接收并做相应处理
        void invokeSubscriber(Subscription subscription, Object event) {
            try {
                subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
            } catch (InvocationTargetException e) {
                handleSubscriberException(subscription, event, e.getCause());
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unexpected exception", e);
            }
        }
    ```
* 总结：
    1. 初始化EventBus对象时传入一个EventBus.Builder对象对EventBus进行初始化，其中有三个比较重要的集合和一个SubscriberMethodFinder对象。
    2. 调用register方法,首先获取订阅者的Class对象，然后通过SubscriberMethodFinder对象获取订阅者中所有订阅方法集合,它先从缓存中获取，如果缓存中有，直接返回；如果缓存中没有，通过反射的方式去遍历订阅者类内部被Subscribe注解的方法，将这些参数只有一个的方法放入到集合中进行返回。
    3. 按个将所有订阅者和对应事件方法进行绑定。在绑定之后会判断绑定的事件是否是粘性事件，如果是粘性事件，直接调用postToSubscription方法，将之前发送的粘性事件发送给订阅者。这就是粘性事件为什么在事件发送去之后，再注册该事件时，还能接受到此消息的。
### 发布事件EventBus—post(event)
```
// EventBus类：
    // 发布的线程状态
    final static class PostingThreadState {
        final List<Object> eventQueue = new ArrayList<>();  // 事件队列
        boolean isPosting;  // 是否正在发布
        boolean isMainThread;   // 是否在主线程
        Subscription subscription;  // 订阅者和订阅方法封装类
        Object event;   // 事件
        boolean canceled;   // 取消标志位
    }

    // ThreadLocal使用方法很简单：get()和set(T)
    // ThreadLocal内部维护一个静态内部类ThreadLocalMap。每个Thread中都具备一个ThreadLocalMap，而ThreadLocalMap可以存储以ThreadLocal为key的键值对。
    private final ThreadLocal<PostingThreadState> currentPostingThreadState = new ThreadLocal<PostingThreadState>() {
        // initialValue方法，同一个线程使用ThreadLocal#get()方法时，只有第一次会调用，即同一个线程使用的PostingThreadState对象是同一个。
        @Override
        protected PostingThreadState initialValue() {
            return new PostingThreadState();
        }
    };

    // 1. 将给定事件发布到事件总线
    public void post(Object event) {
        // currentPostingThreadState实际上是：ThreadLocal<PostingThreadState>
        // Threadlocal而是一个线程内部的存储类，可以在指定线程内存储数据，数据存储以后，只有指定线程可以得到存储数据
        // 因此，若是同一个线程，则postingState是唯一的，可以类比成单例。
        PostingThreadState postingState = currentPostingThreadState.get();
        List<Object> eventQueue = postingState.eventQueue;
        // 将事件添加到事件队列中，即同一线程post发布的所有事件都在该队列中
        eventQueue.add(event);
        // 是否正在发布事件
        if (!postingState.isPosting) {
            // 是否是主线程
            postingState.isMainThread = isMainThread();
            // 正在发布
            postingState.isPosting = true;
            if (postingState.canceled) {
                throw new EventBusException("Internal error. Abort state was not reset");
            }
            try {
                // 将事件队列中所有事件，移除队列并发布
                while (!eventQueue.isEmpty()) {
                    postSingleEvent(eventQueue.remove(0), postingState);
                }
            } finally {
                postingState.isPosting = false;
                postingState.isMainThread = false;
            }
        }
    }

    // 1.1 发布单个事件
    private void postSingleEvent(Object event, PostingThreadState postingState) throws Error {
        Class<?> eventClass = event.getClass();
        boolean subscriptionFound = false;
        // 判断事件的继承性，默认是true
        if (eventInheritance) {
            // 查找所有事件类型，包括父类和接口
            List<Class<?>> eventTypes = lookupAllEventTypes(eventClass);
            int countTypes = eventTypes.size();
            // 遍历Class集合，继续处理事件
            for (int h = 0; h < countTypes; h++) {
                Class<?> clazz = eventTypes.get(h);
                // 通过postSingleEventForEventType方法返回的boolean值和subscriptionFound，进行或运算，subscriptionFound默认为false
                subscriptionFound |= postSingleEventForEventType(event, postingState, clazz);
            }
        } else {
            subscriptionFound = postSingleEventForEventType(event, postingState, eventClass);
        }
        // 若subscriptionFound为false，则证明没有订阅者订阅此事件
        if (!subscriptionFound) {
            if (logNoSubscriberMessages) {
                logger.log(Level.FINE, "No subscribers registered for event " + eventClass);
            }
            if (sendNoSubscriberEvent && eventClass != NoSubscriberEvent.class &&
                    eventClass != SubscriberExceptionEvent.class) {
                // 发送 '没有订阅者事件' 事件
                post(new NoSubscriberEvent(this, event));
            }
        }
    }

    // 1.1.1 查找所有Class对象，包括超类和接口。
    private static List<Class<?>> lookupAllEventTypes(Class<?> eventClass) {
        synchronized (eventTypesCache) {
            // 先查找缓存
            List<Class<?>> eventTypes = eventTypesCache.get(eventClass);
            // 缓存没有则遍历查找
            if (eventTypes == null) {
                eventTypes = new ArrayList<>();
                Class<?> clazz = eventClass;
                while (clazz != null) {
                    eventTypes.add(clazz);  // 添加当前事件类型
                    // 获取当前事件类实现的所有接口，添加到事件类型集合中
                    addInterfaces(eventTypes, clazz.getInterfaces());
                    clazz = clazz.getSuperclass();
                }
                // 通过以事件Class类型位key，缓存跟此事件有关的所有事件
                eventTypesCache.put(eventClass, eventTypes);
            }
            return eventTypes;
        }
    }

    // 1.1.1.1 通过父接口递归，添加事件的父接口类型
    static void addInterfaces(List<Class<?>> eventTypes, Class<?>[] interfaces) {
        // 遍历当前事件类所实现的所有接口
        for (Class<?> interfaceClass : interfaces) {
            // 若事件集合不包含此接口，则添加，并获取接口的父接口，继续迭代
            if (!eventTypes.contains(interfaceClass)) {
                eventTypes.add(interfaceClass);
                // 继续迭代
                addInterfaces(eventTypes, interfaceClass.getInterfaces());
            }
        }
    }

    // 1.1.2 针对事件类型发布单个事件
    private boolean postSingleEventForEventType(Object event, PostingThreadState postingState, Class<?> eventClass) {
        CopyOnWriteArrayList<Subscription> subscriptions;
        synchronized (this) {
            // 通过事件类型，获取 订阅者和订阅者方法集合映射的封装类 集合
            subscriptions = subscriptionsByEventType.get(eventClass);
        }
        if (subscriptions != null && !subscriptions.isEmpty()) {
            // 遍历subscriptions集合
            for (Subscription subscription : subscriptions) {
                postingState.event = event;
                postingState.subscription = subscription;
                boolean aborted = false;
                try {
                    // 发布到订阅
                    postToSubscription(subscription, event, postingState.isMainThread);
                    aborted = postingState.canceled;
                } finally {
                    // 重置postingState
                    postingState.event = null;
                    postingState.subscription = null;
                    postingState.canceled = false;
                }
                if (aborted) {
                    break;
                }
            }
            return true;
        }
        return false;
    }

    // 1.1.2.1 最后发布到订阅者，根据线程模式，进行线程切换
    private void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
         // 判断订阅事件方法的线程模式
         switch (subscription.subscriberMethod.threadMode) {
             case POSTING:
                 // 默认的线程模式，在那个线程发送事件就在那个线程处理事件
                 invokeSubscriber(subscription, event);
                 break;
             case MAIN:  // 在主线程处理事件
                 if (isMainThread) {
                     // 如果在主线程发送事件，则直接在主线程通过反射处理事件
                     invokeSubscriber(subscription, event);
                 } else {
                     // 如果是在子线程发送事件，则将事件入队列，通过Handler切换到主线程执行处理事件
                     mainThreadPoster.enqueue(subscription, event);
                 }
                 break;
             case MAIN_ORDERED:
                 // 无论在那个线程发送事件，都先将事件入队列，然后通过 Handler 切换到主线程，依次处理事件。mainThreadPoster不会为null
                 if (mainThreadPoster != null) {
                     mainThreadPoster.enqueue(subscription, event);
                 } else {
                     // temporary: technically not correct as poster not decoupled from subscriber
                     invokeSubscriber(subscription, event);
                 }
                 break;
             case BACKGROUND:
                 if (isMainThread) {
                     // 如果在主线程发送事件，则先将事件入队列，然后通过线程池依次处理事件
                     backgroundPoster.enqueue(subscription, event);
                 } else {
                     // 如果在子线程发送事件，则直接在发送事件的线程通过反射处理事件
                     invokeSubscriber(subscription, event);
                 }
                 break;
             case ASYNC:
                 // 无论在那个线程发送事件，都将事件入队列，然后通过线程池处理。
                 asyncPoster.enqueue(subscription, event);
                 break;
             default:
                 throw new IllegalStateException("Unknown thread mode: " + subscription.subscriberMethod.threadMode);
         }
    }

     // 1.1.2.1.1 用反射来执行订阅事件的方法，这样发送出去的事件就被订阅者接收并做相应处理
    void invokeSubscriber(Subscription subscription, Object event) {
        try {
            subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
        } catch (InvocationTargetException e) {
            handleSubscriberException(subscription, event, e.getCause());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unexpected exception", e);
        }
    }
```
总结：
1. 获取当前线程的事件队列，将要发布的事件加入到队列中。然后遍历整个队列，边移除遍历的当前事件，边发布当前事件。
2. 获取事件的Class对象，找到当前的event的所有父类和实现的接口的class集合。遍历这个集合，发布集合中的每一个事件。
3. 通过事件类型，获取 订阅者和订阅者方法集合映射的封装类 集合，遍历集合，将事件发送给订阅者。
4. 发送给订阅者时，根据订阅者的订阅方法注解中的线程模式，判断是否需要线程切换，若需要则切换线程进行调用，否则直接执行发布。
5. 用反射来执行订阅事件的方法，这样发送出去的事件就被订阅者接收并做相应处理。

若是粘性事件，则先添加到粘性事件集合中，再发布事件：
```
// EventBus类：
    public void postSticky(Object event) {
        synchronized (stickyEvents) {
            // 添加到粘性事件集合，以备事件发布之后，订阅该事件的订阅者注册该事件时，依旧能够及时收到该事件
            // 具体可回顾【注册EventBus—register(this)--遍历集合按个订阅】部分，查看如何注册后即可及时收到粘性事件
            stickyEvents.put(event.getClass(), event);
        }
        // 调用普通事件的发布方法，及时发布
        post(event);
    }
```
### EventBus-unregister(subscriber)
```
// EventBus类：
    // 从所有事件类中注销给定的订阅者
    public synchronized void unregister(Object subscriber) {
        // 获取订阅者订阅的所有事件
        List<Class<?>> subscribedTypes = typesBySubscriber.get(subscriber);
        if (subscribedTypes != null) {
            // 遍历订阅类型集合，释放之前缓存的当前类中的Subscription
            for (Class<?> eventType : subscribedTypes) {
                unsubscribeByEventType(subscriber, eventType);
            }
            // 删除以subscriber为key的键值对，更新typesBySubscriber
            typesBySubscriber.remove(subscriber);
        } else {
            logger.log(Level.WARNING, "Subscriber to unregister was not registered before: " + subscriber.getClass());
        }
    }

    // 仅更新subscriptionsByEventType，而不更新typesBySubscriber
    private void unsubscribeByEventType(Object subscriber, Class<?> eventType) {
        // 得到当前参数类型对应的Subscription集合
        List<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        if (subscriptions != null) {
            int size = subscriptions.size();
            // 遍历Subscription集合
            for (int i = 0; i < size; i++) {
                Subscription subscription = subscriptions.get(i);
                 // 如果当前subscription对象对应的注册类对象 和 要取消注册的注册类对象相同，则删除当前subscription对象
                if (subscription.subscriber == subscriber) {
                    subscription.active = false;
                    subscriptions.remove(i);
                    i--;
                    size--;
                }
            }
        }
    }
```
总结：unregister中释放了typesBySubscriber、subscriptionsByEventType中缓存的资源。

## Subscriber 索引
EventBus主要是在项目运行时通过反射来查找订事件的方法信息，如果项目中有大量的订阅事件的方法，必然会对项目运行时的性能产生影响。其实除了在项目运行时通过反射查找订阅事件的方法信息，EventBus在3.0之后增加了注解处理器，在程序的编译时候，就可以根据注解生成相对应的代码，相对于之前的直接通过运行时反射，大大提高了程序的运行效率，但是在3.0默认的是通过反射去查找用@Subscribe标注的方法，来生成一个辅助的索引类来保存这些信息，这个索引类就是Subscriber Index，和 ButterKnife 的原理是类似的。

### SubscriberInfoIndex使用(以【EventBus使用为例】)
1. App build.gradle依赖：
```
dependencies {
    compile 'org.greenrobot:eventbus:3.1.1'
    // 引入注解处理器
    annotationProcessor 'org.greenrobot:eventbus-annotation-processor:3.1.1'
}
```
2. App build.gradle配置注解生成类选项：
```
android {
    defaultConfig {
        ...
        javaCompileOptions {
            annotationProcessorOptions {
                // 根据项目实际情况，指定辅助索引类的名称和包名
                arguments = [ eventBusIndex : 'com.android.framework.CustomEventBusIndex' ]
            }
        }
    }
    ...
```
3. Application中配置：
```
// CustomEventBusIndex此类由EventBus注解处理器，在项目编译阶段动态生成
EventBus.builder().addIndex(new CustomEventBusIndex()).installDefaultEventBus();

public class CustomEventBusIndex implements SubscriberInfoIndex {
    // 保存当前注册类的 Class 类型和其中事件订阅方法的信息
    private static final Map<Class<?>, SubscriberInfo> SUBSCRIBER_INDEX;

    static {
        SUBSCRIBER_INDEX = new HashMap<Class<?>, SubscriberInfo>();

        /*
         * 添加订阅者信息索引——SimpleSubscriberInfo参数：
         * subscriberClass：订阅者Class类
         * shouldCheckSuperclass：是否检查父类Class
         * methodInfos(数组)：订阅方法数组
         *
         * SubscriberMethodInfo构造参数：1. 方法名  2. 方法唯一参数类型，即事件类型
         */
        putIndex(new SimpleSubscriberInfo(com.android.framework.launch.activity.EventActivity1.class, true,
                new SubscriberMethodInfo[] {
            new SubscriberMethodInfo("onMessageEventPosting", com.android.baselibrary.bean.EventBean.class),
            new SubscriberMethodInfo("onMessageEventMain", com.android.baselibrary.bean.EventBean.class,
                    ThreadMode.MAIN, 2, false),
            new SubscriberMethodInfo("onMessageEventMainOrdered", com.android.baselibrary.bean.EventBean.class,
                    ThreadMode.MAIN_ORDERED),
            new SubscriberMethodInfo("onMessageEventBackground", com.android.baselibrary.bean.EventBean.class,
                    ThreadMode.BACKGROUND),
            new SubscriberMethodInfo("onMessageEventAsync", com.android.baselibrary.bean.EventBean.class,
                    ThreadMode.ASYNC),
        }));

    }

    private static void putIndex(SubscriberInfo info) {
        SUBSCRIBER_INDEX.put(info.getSubscriberClass(), info);
    }

    @Override
    public SubscriberInfo getSubscriberInfo(Class<?> subscriberClass) {
        SubscriberInfo info = SUBSCRIBER_INDEX.get(subscriberClass);
        if (info != null) {
            return info;
        } else {
            return null;
        }
    }
}
```
### 使用SubscriberInfoIndex时EventBus的注册流程
1. 首先看看Application中配置的代码EventBus.builder().addIndex(new CustomEventBusIndex())
    ```
    // EventBusBuilder类：
        public EventBusBuilder addIndex(SubscriberInfoIndex index) {
            if (subscriberInfoIndexes == null) {
                subscriberInfoIndexes = new ArrayList<>();
            }
            subscriberInfoIndexes.add(index);
            return this;
        }
    ```
2. addIndex把生成的索引类的实例保存在subscriberInfoIndexes集合中，然后用installDefaultEventBus()创建默认的 EventBus实例：
    ```
    // EventBusBuilder类：
        public EventBus installDefaultEventBus() {
            synchronized (EventBus.class) {
                if (EventBus.defaultInstance != null) {
                    throw new EventBusException("Default instance already exists." +
                        " It may be only set once before it's used the first time to ensure consistent behavior.");
                }
                // 给默认的EventBus单例对象赋初值
                EventBus.defaultInstance = build();
                return EventBus.defaultInstance;
            }
        }
    
    // EventBus类：  
        public static EventBus getDefault() {
            if (defaultInstance == null) {
                synchronized (EventBus.class) {
                    if (defaultInstance == null) {
                        defaultInstance = new EventBus();
                    }
                }
            }
            return defaultInstance;
        }
    ```
    即用当前EventBusBuilder对象创建一个 EventBus 实例，这样我们通过EventBusBuilder配置的 SubscriberInfoIndex 也就传递到了EventBus实例中，然后赋值给EventBus的 defaultInstance成员变量。所以在 Application 中生成了 EventBus 的默认单例，这样就保证了在项目其它地方执行EventBus.getDefault()就能得到唯一的 EventBus 实例！
3. subscriberMethodFinder 注解方法找寻器：从第一步addIndex中，我们知道了subscriberInfoIndexes此时已经不为null，那么我们回到EventBus构造方法中看一个变量：
    ```
    EventBus(EventBusBuilder builder) {
        indexCount = builder.subscriberInfoIndexes != null ? builder.subscriberInfoIndexes.size() : 0;
        // @Subscribe注解方法找寻器
        subscriberMethodFinder = new SubscriberMethodFinder(builder.subscriberInfoIndexes,
                builder.strictMethodVerification, builder.ignoreGeneratedIndex);
    }
    
    // 注意此字段
    private List<SubscriberInfoIndex> subscriberInfoIndexes;
    SubscriberMethodFinder(List<SubscriberInfoIndex> subscriberInfoIndexes, boolean strictMethodVerification,
                           boolean ignoreGeneratedIndex) {
        this.subscriberInfoIndexes = subscriberInfoIndexes;
        this.strictMethodVerification = strictMethodVerification;
        this.ignoreGeneratedIndex = ignoreGeneratedIndex;
    }
    ```
    我们在上面【查找订阅方法集合】部分提到过一个方法：getSubscriberInfo，此方法默认返回值为null，因此后面查找订阅方法为：运行期通过反射查找。而若配置了Subscriber索引，则getSubscriberInfo返回不为null：
    ```
    // SubscriberMethodFinder类：
        private SubscriberInfo getSubscriberInfo(FindState findState) {
            if (findState.subscriberInfo != null && findState.subscriberInfo.getSuperSubscriberInfo() != null) {
                SubscriberInfo superclassInfo = findState.subscriberInfo.getSuperSubscriberInfo();
                if (findState.clazz == superclassInfo.getSubscriberClass()) {
                    return superclassInfo;
                }
            }
            
            // 上一段代码中，我们说过注意此字段，此时不为null
            if (subscriberInfoIndexes != null) {
                for (SubscriberInfoIndex index : subscriberInfoIndexes) {
                    // 根据注册类的 Class 类查找SubscriberInfo
                    SubscriberInfo info = index.getSubscriberInfo(findState.clazz);
                    if (info != null) {
                        return info;
                    }
                }
            }
            return null;
        }
    
    // index.getSubscriberInfo此方法就是addIndex传入的 CustomEventBusIndex实现的方法：
        @Override
        public SubscriberInfo getSubscriberInfo(Class<?> subscriberClass) {
            SubscriberInfo info = SUBSCRIBER_INDEX.get(subscriberClass);
            if (info != null) {
                return info;
            } else {
                return null;
            }
        }
    ```
    因此，findUsingInfo方法中，无需再通过反射单个查找订阅方法。
    ```
    private List<SubscriberMethod> findUsingInfo(Class<?> subscriberClass) {
        FindState findState = prepareFindState();
        findState.initForSubscriber(subscriberClass);
        while (findState.clazz != null) {
            findState.subscriberInfo = getSubscriberInfo(findState);
            // 此时不为null
            if (findState.subscriberInfo != null) {
                SubscriberMethod[] array = findState.subscriberInfo.getSubscriberMethods();
                for (SubscriberMethod subscriberMethod : array) {
                    // 检查添加
                    if (findState.checkAdd(subscriberMethod.method, subscriberMethod.eventType)) {
                        findState.subscriberMethods.add(subscriberMethod);
                    }
                }
            } else {
                findUsingReflectionInSingleClass(findState);
            }
            findState.moveToSuperclass();
        }
        return getMethodsAndRelease(findState);
    }
    ```
 Subscriber Index 的核心就是项目编译时使用注解处理器生成保存事件订阅方法信息的索引类，然后项目运行时将索引类实例设置到 EventBus 中，这样当注册 EventBus 时，从索引类取出当前注册类对应的事件订阅方法信息，以完成最终的注册，避免了运行时反射处理的过程，所以在性能上会有质的提高。项目中可以根据实际的需求决定是否使用 Subscriber Index。 

## 参考链接

https://www.jianshu.com/p/d9516884dbd4

https://juejin.im/post/5db7d789f265da4cf1583315

https://blog.csdn.net/lufeng20/article/details/24314381

https://segmentfault.com/a/1190000020052249

...

<font color="#ff0000">注：若有什么地方阐述有误，敬请指正。**期待您的点赞哦！！！**</font> 