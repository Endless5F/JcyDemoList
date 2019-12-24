## Eventbus使用
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
        // 事件对应的订阅者存储
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