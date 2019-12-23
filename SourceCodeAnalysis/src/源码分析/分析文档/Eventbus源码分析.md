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