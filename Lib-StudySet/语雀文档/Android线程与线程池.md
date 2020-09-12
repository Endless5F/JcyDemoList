# Android线程与线程池

## AsyncTask
### AsyncTask的使用

- 一个`Android` 已封装好的轻量级异步类
```
/**
 * 步骤1：创建AsyncTask子类
 * 注： 
 *   a. 继承AsyncTask类
 *   b. 为3个泛型参数指定类型；若不使用，可用java.lang.Void类型代替
 *   c. 根据需求，在AsyncTask子类内实现核心方法
 */
 private class MyTask extends AsyncTask<Params, Progress, Result> {
	....

	// 方法1：onPreExecute（）
  // 作用：执行 线程任务前的操作
  // 注：根据需求复写
  @Override
  protected void onPreExecute() {
  	  ...
  }

  // 方法2：doInBackground（）
  // 作用：接收输入参数、执行任务中的耗时操作、返回 线程任务执行的结果
  // 注：必须复写，从而自定义线程任务
  @Override
  protected String doInBackground(String... params) {
  	  ...// 自定义的线程任务
		  // 可调用publishProgress（）显示进度, 之后将执行onProgressUpdate（）
      publishProgress(count);
  }

  // 方法3：onProgressUpdate（）
  // 作用：在主线程 显示线程任务执行的进度
  // 注：根据需求复写
  @Override
  protected void onProgressUpdate(Integer... progresses) {
  	  ...
  }

  // 方法4：onPostExecute（）
  // 作用：接收线程任务执行结果、将执行结果显示到UI组件
  // 注：必须复写，从而自定义UI操作
  @Override
  protected void onPostExecute(String result) {
      ...// UI操作
  }

  // 方法5：onCancelled()
  // 作用：将异步任务设置为：取消状态
  @Override
  protected void onCancelled() {
    	...
  }
}

/**
 * 步骤2：创建AsyncTask子类的实例对象（即 任务实例）
 * 注：AsyncTask子类的实例必须在UI线程中创建
 */
MyTask mTask = new MyTask();

/**
 * 步骤3：手动调用execute(Params... params) 从而执行异步线程任务
 * 注：
 *    a. 必须在UI线程中调用
 *    b. 同一个AsyncTask实例对象只能执行1次，若执行第2次将会抛出异常
 *    c. 执行任务中，系统会自动调用AsyncTask的一系列方法：onPreExecute() 、doInBackground()、onProgressUpdate() 、onPostExecute() 
 *    d. 不能手动调用上述方法
 */
mTask.execute()；
```
### AsyncTask的核心方法

- `AsyncTask` 核心 & 常用的方法如下：

![944365-153fb37764704129.png](https://cdn.nlark.com/yuque/0/2020/png/754789/1579847305914-624252bf-c657-4502-94ae-a36fc69f8879.png#align=left&display=inline&height=600&name=944365-153fb37764704129.png&originHeight=600&originWidth=1130&size=124376&status=done&style=none&width=1130)

- 方法执行顺序如下：

![944365-31df794006c69621.png](https://cdn.nlark.com/yuque/0/2020/png/754789/1579847316835-b1def101-89be-4b62-ad7d-b0be83bbe864.png#align=left&display=inline&height=320&name=944365-31df794006c69621.png&originHeight=320&originWidth=1140&size=33479&status=done&style=none&width=1140)
## HandlerThread
### HandlerThread的使用

- `HandlerThread`的本质：继承`Thread`类 & 封装`Handler`类
```
// 步骤1：创建HandlerThread实例对象
// 传入参数 = 线程名字，作用 = 标记该线程
   HandlerThread mHandlerThread = new HandlerThread("handlerThread");

// 步骤2：启动线程
   mHandlerThread.start();

// 步骤3：创建工作线程Handler & 复写handleMessage（）
// 作用：关联HandlerThread的Looper对象、实现消息处理操作 & 与其他线程进行通信
// 注：消息处理操作（HandlerMessage（））的执行线程 = mHandlerThread所创建的工作线程中执行
  Handler workHandler = new Handler( handlerThread.getLooper() ) {
            @Override
            public boolean handleMessage(Message msg) {
                ...//消息处理
                return true;
            }
        });

// 步骤4：使用工作线程Handler向工作线程的消息队列发送消息
// 在工作线程中，当消息循环时取出对应消息 & 在工作线程执行相关操作
  // a. 定义要发送的消息
  Message msg = Message.obtain();
  msg.what = 2; //消息的标识
  msg.obj = "B"; // 消息的存放
  // b. 通过Handler发送消息到其绑定的消息队列
  workHandler.sendMessage(msg);
  // 步骤5：结束线程，即停止线程的消息循环
  mHandlerThread.quit();
```
### 内存泄漏 & 连续发送消息

- 内存泄漏：参考Handler内存泄漏
- 连续发送消息：使用`HandlerThread`时只是开了一个工作线程，当你点击了`n`下后，只是将`n`个消息发送到消息队列`MessageQueue`里排队，等候派发消息给Handler再进行对应的操作。
## IntentService
### IntentService使用步骤
步骤1：定义 `IntentService`的子类，需复写`onHandleIntent()`方法
步骤2：在`Manifest.xml`中注册服务
步骤3：在`Activity`中开启`Service`服务
### IntentService源码分析
```
// IntentService源码中的 onCreate() 方法
@Override
public void onCreate() {
    super.onCreate();
    // HandlerThread继承自Thread，内部封装了 Looper
    //通过实例化andlerThread新建线程并启动
    //所以使用IntentService时不需要额外新建线程
    HandlerThread thread = new HandlerThread("IntentService[" + mName + "]");
    thread.start();

    //获得工作线程的 Looper，并维护自己的工作队列
    mServiceLooper = thread.getLooper();
    //将上述获得Looper与新建的mServiceHandler进行绑定
    //新建的Handler是属于工作线程的。
    mServiceHandler = new ServiceHandler(mServiceLooper);
}

// 默认已实现，源码可以看到onStart方法是在该方法中被调用的。
// 每次组件通过startService方法启动服务时调用一次，两个int型参数，一个是组标识符，一个是启动ID，
// 组标识符用来表示当前Intent发送,是一次重新发送、还是一次从没成功过的发送。每次调用onStartCommand方法时启动ID都不同，启动ID也用来区分不同的命令;
@Override
public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
		// 将任务添加至队列中
	  onStart(intent, startId);
    // mRedelivery不同，会返回两个不同的标志START_REDELIVER_INTENT 和START_NOT_STICKY，那么他们有什么不同呢？
    // 区别就在于如果系统在服务完成之前关闭它，则两种类型就表现出不同了：
    // 	START_NOT_STICKY型服务会直接被关闭，而START_REDELIVER_INTENT 型服务会在可用资源不再吃紧的时候尝试再次启动服务。
    //	我们的操作不是十分重要的时候，我们可以选择START_NOT_STICKY，这也是IntentService的默认选项，反之则选择START_REDELIVER_INTENT 型服务。
    return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
}

@Override
public void onStart(@Nullable Intent intent, int startId) {
		// 消息会在HandlerThread中处理，mServiceHandler收到消息后，
    // 会将Intent对象传递给onHandlerIntent方法去处理，
    // 注意这个Intent对象的内容和外界的startService中的intent是完全一致的，
    // 通过这个Intent对象可以解析出外界启动IntentService所传递的参数，
    // 通过这些参数可以区分具体的任务，当onHandleIntent方法结束之后，IntentService会通过stopSelf方法来尝试停止服务。
		Message msg = mServiceHandler.obtainMessage();
 		msg.arg1 = startId;
		msg.obj = intent;
		mServiceHandler.sendMessage(msg);
}

private final class ServiceHandler extends Handler {
    public ServiceHandler(Looper looper) {
        super(looper);
    }

		//IntentService的handleMessage方法把接收的消息交给onHandleIntent()处理
		//onHandleIntent()是一个抽象方法，使用时需要重写的方法
    @Override
    public void handleMessage(Message msg) {
        // onHandleIntent 方法在工作线程中执行，执行完调用 stopSelf() 结束服务。
        onHandleIntent((Intent)msg.obj);
        // onHandleIntent 处理完成后 IntentService会调用 stopSelf() 自动停止。
        // 这里之所以采用stopSelf（int id）而不会stopSelf是因为stopSelf会立刻停止服务，
        // 而这个时候可能还有任务没有执行完成，stopSelf（int id）会让所有任务都完成后才执行的，
        // 这个策略可以从AMS的stopServiceToken方法的实现来找到依据。
        stopSelf(msg.arg1);
    }
}

// onHandleIntent()是一个抽象方法，使用时需要重写的方法
// 在这里添加我们要执行的代码，Intent中可以保存我们所需的数据
@WorkerThread
protected abstract void onHandleIntent(Intent intent);
```
## Android中的线程池
### ThreadPoolExecutor
Android中的线程池的概念来源于JAVA中的Executor，Executor是一个接口，真正的线程池实现为ThreadPoolExecutor，ThreadPoolExecutor提供了一系列参数来配置线程池，通过不同的参数可以创建不同的线程池，从线程池的功能特性上来说，Android的线程池主要分为四类，都是通过Executors提供的工厂方法得到的。
#### 配置参数：
**corePoolSize：**线程池的核心线程数，默认情况下，核心线程会在线程池中一直存活，即使他们处于闲置状态，如果将ThreadPoolExecutor的allowCoreThreadTimeOut属性设置为true,那么闲置的核心线程在等待新任务到来时就会有超市策略，这个间隔由keepAliveTime指定，当等待时间超过keepAliveTime所指的时长后，核心线程就会被终止
**maximumPoolSize：**线程池所容纳最大的线程数，当活动线程数达到这个数值后，后续的新任务将会被阻塞
**keepAliveTime：**非核心线程闲置时的超时时长，超过这个时长，非核心线程就会被回收，当ThreadPoolExecutor的allowCoreThreadTimeOut属性设置为true时，keepAliveTime同样会作用于核心线程
**unit：**用于指定keepAliveTime参数的时间单位，这是一个枚举，常用的有TimeUnit,毫秒，秒，分钟等
**workQueue：**线程池的任务队列，通过线程池的execute方法提交的Runnable对象会存储在这个参数中
**threadFactory：**线程工厂，为线程池提供创建新线程的功能，ThreadFactory是一个接口，他只有一个方法，Thread new Thread(Runnable r)
#### ThreadPoolExecutor执行任务时大致规则

1. 如果线程池中的线程数量未达到核心线程的数量，那么直接启动一个核心线程来执行任务
1. 如果线程池中的线程数量已经达到或者超过核心线程的数量，那么任务会被插入到任务队列中排队等待执行
1. 如果在步骤2中无法将任务插入到任务队列中，这往往是由于任务队列已满，这个时候如果线程数量未达到线程池规定的最大值，那么会立刻启动一个非核心线程来执行任务。
1. 如果步骤3中线程数量已经达到线程池规定的最大数量，那么就会拒绝执行此任务，ThreadPoolExecutor会调用RejectedExecutionHandler的rejectedExecution方法来通知调用者。
#### AsyncTask的线程池的配置情况：
```
private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
private static final int KEEP_ALIVE = 1;

private static final ThreadFactory sThreadFactory = new ThreadFactory() {

    private final AtomicInteger mCount = new AtomicInteger(1);

    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, "AsyncTask #" + mCount.getAndIncrement());
    }
};
private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingDeque<Runnable>(128);
public static final Executor THREAD_POOL_EXECUTOR =
        new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
                TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);
```
AsyncTask对THREAD_POOL_EXECUTOR这个线程池进行了配置，配置后的线程池规格如下：

- 核心线程数等于CPU核心数 + 1
- 线程池的最大线程数为CPU核心数的2倍+1
- 核心线程无超时机制，非核心线程的限制时超时时间为1s
- 任务队列的容量为128
### 线程池分类：
#### FixedThreadPool 
通过Executors的newFixedThreadPool方法来创建，他是一种线程数量固定的线程池，当线程池处于空闲的状态时，他们并不会被回收，除非线程池被关闭了，当所有的线程池都处于活动状态时，新任务都处于等待状态，直到有线程空闲出来，由于FixedThreadPool只有核心线程并且只有核心线程不会被回收，这意味着他能够更快加速的相应外界的请求，newFixedThreadPool方法的实现如下，可以发现FixedThreadPool中只有核心线程并且这些核心线程没有超时机制，另外任务大小也是没有限制的。
#### CachedThreadPool
通过Executors的newCachedThreadPooll来创建，这是一种线程数量不定的线程池，他只有非核心线程，并且其最大线程数为Integer.MAX_VALUE,由于Integer.MAX_VALUE是一个很大的数，实际上就相当于最大线程数可以任意大，当线程池中的线程都处于活动状态的时候，线程池会创建新的线程来处理新任务，否则就会利用空闲的线程来处理新任务，线程池的空闲线程都有超时机制，这个超时机制为60秒，超过就会被回收，和上面不同的事，CachedThreadPool的任务队列其实相当于一个空集合，这就导致任何任务都会立即被执行，因为在这种场景下，SynchronousQueue是无法插入任务的，SynchronousQueue是一个非常特殊的队列，在很多情况下可以把他简单理解为一个无法存储的元素队列，由于他在实际中较少使用，这里就不深入探讨它了，从SynchronousQueue的特性来看，这类线程池比较适合执行大量的耗时较少的任务，当整个线程池属于闲置状态的时候，线程池中的线程都会超时而被停止，这个时候CachedThreadPool之中实际上是没有任何线程，他几乎是不占用任何资源的。
#### ScheduleThreadPool
通过Executors的newScheduleThreadPool(int corePoolSize)方法来创建，他的核心线程数量是固定的，而非核心线程是没有限制id，并且当非核心线程闲置时会立即回收，ScheduleThreadPool这类主要用于定时任务和具有固定周期的重复任务。
#### SingleTheardExecutor
通过Executors的newSingleTheardExecutor创建，内部只有一个核心线程，确保所有的任务都在同一个线程按顺序执行，SingleTheardExecutor的意义是统一所有的外界任务到同一个线程中，这使得这些任务之间不需要处理线程同步的问题。
## [**线程安全(高并发)面试笔记**](https://github.com/Endless5F/JcyDemoList/blob/master/Lib-StudySet/面试相关总结/2.%20线程安全%28高并发%29笔记.md)
## 参考链接
[Android 多线程：手把手教你使用AsyncTask](https://www.jianshu.com/p/ee1342fcf5e7)

[Android多线程：这是一份全面 & 详细的HandlerThread学习指南](https://www.jianshu.com/p/540f0c6c7bd1)
