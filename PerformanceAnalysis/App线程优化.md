## 一、Android线程调度原理剖析
1. 线程调度原理

        任意时刻，只有一个线程占用CPU，处于运行状态。
        多线程并发：轮流获取cpu使用权。
        JVM负责线程调度：按照特定机制分配CPU使用权。
    线程调度模型：

        分时调度模型：轮流获取、均匀分配cpu时间
        抢占式调度模型：优先级高的获取，JVM采用
2. Android线程调度

    nice值：

        Process中定义
        值越小，优先级越高
        默认是Process.THREAD_PRIORITY_DEFAULT，0
        问题：只有nice值并不足够，例如app又一个前台的UI线程，有10个后台线程（优先级低，数量多），后台线程合起来也会影响前台线程

    cgroup(补充nice值不足的问题)：

        更严格的群组调度策略（后台线程被放入后台group中，只有很小的几率使用cpu）
        保证前台线程可以获取到更多的CPU资源

    注意点：

        线程过多会导致cpu频繁切换，降低线程运行效率（不能无限制的添加子线程）
        正确认识任务重要性决定任务的优先级（工作量越大，优先级越低）
        优先级具有继承性（A线程中创建B线程——不指定B的优先级的化，B的优先级与A一样，在UI中创建线程要指定优先级，不然会抢占资源概率降低）

## 二、Android异步方式汇总
1. 异步方式

    * Thread：
        1. 最简单、常见的方式；
        2. 不易复用，频繁创建销毁开销大；
        3. 复杂场景不易使用
    * HandlerThread：
        1. 自带消息循环的线程
        2. 串行执行
        3. 长时间运行，不断从队列中获取任务
    * IntentService：
        1. 继承自Service在内部创建HandlerThread
        2. 异步、不占用主线程
        3. 优先级高，不易被系统kill
    * AsyncTask：
        1. Android提供的工具类
        2. 无需自己处理线程切换
        3. 需要注意版本不一致问题
    * 线程池：
        1. java提供的线程池
        2. 易复用，减少频繁创建、销毁的时间
        3. 功能强大：定时、任务列队、并发数控制等
    * RxJava：
        1. 由强大的Scheduler集合提供
        2. 不同类型的区分：IO密集型、CPU密集型
        3. https://www.jianshu.com/p/b742526c7dec （深入理解 RxJava2：Scheduler（2））
2. 总结：推荐度：从前往后排列、正确场景选择正确的方式

## 三、线程使用准则
1. 严禁使用new Thread
2. 提供基础线程池供各个业务栈使用（避免各个业务栈各自维护一套线程池，导致线程数过多）
3. 根据任务类型选择合适的异步方式（优先级低长时间执行-HandlerThread，定时执行-线程池）
4. 创建线程必须命名（方便定位线程归属，运行期Thread.currentThread().setName修改名字）
5. 关键异步任务监控（异步不等于不耗时，AOP的方式来做监控）
6. 重视优先级的设置（Process.setThreadPriority(),可以设置多次）
## 四、如何锁定线程创建者
1. 锁定线程创建背景
    1. 问题：

            项目变大之后收敛线程
            项目源码、三方库、aar中都有线程的创建
            避免恶化的一种监控预防手段（避免私自创建线程）
    2. 分析：创建线程的位置获取堆栈信息，而所有的异步方式，都会走到new Thread()
2. 锁定线程创建方案：适合使用Hook手段(Hook点：构造函数或者特定方法，而此场景hook点适合为Thread的构造函数)

        // 线程优化
        // hook Thread的构造方法，然后打印Thread初始化时的堆栈信息，就可以了解到当前Thread被调用的位置
        DexposedBridge.hookAllConstructors(Thread.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Thread thread = (Thread) param.thisObject;
                LogUtils.i(thread.getName()+" stack "+Log.getStackTraceString(new Throwable()));
            }
        });
        // 拦截Thread 类以及 Thread 类所有子类的 run方法，在 run 方法开始执行和退出的时候进行拦截，
        // 就可以知道进程内部所有Java线程创建和销毁的时机；更进一步，你可以结合Systrace等工具，来生成整个过程的执行流程图
        DexposedBridge.findAndHookMethod(Thread.class, "run", new ThreadMethodHook());
## 五、线程收敛优雅实践初步
1. 线程收敛常规方案

    根据线程创建堆栈考量合理性，各业务线下掉自己的线程库，使用统一线程库

    基础库怎么使用线程：直接依赖统一的线程库。缺点：线程库的更新可能会导致基础库更新
2. 线程收敛优雅方案

    基础库优雅使用线程：基础库内部暴露API：setExecutor，初始化的时候注入统一的线程库

        // 示例代码(比如LogUtils内部有部分异步操作，之前都是自己处理，优雅使用如下)：
        public class LogUtils {

            private static ExecutorService sExecutorService;

            public static void setExecutor(ExecutorService executorService){
                sExecutorService = executorService;
            }

            public static final String TAG = "performance";

            public static void i(String msg){
                if(Utils.isMainProcess(PerformanceApp.getApplication())){
                    Log.i(TAG,msg);
                }
                // 异步
                if(sExecutorService != null){
                    sExecutorService.execute(...);
                }
            }

        }
    统一线程库：

        区分任务类型：IO、CPU密集型
        IO密集型任务不消耗CPU，核心池可以很大
        CPU密集型任务：核心池大小和CPU核心数相关（并发数超过CPU核心数会导致CPU频发切换，降低执行效率）
        https://blog.csdn.net/youanyyou/article/details/78990156 (什么是CPU密集型、IO密集型？)
    统一线程库示例代码：

        public class ThreadPoolUtils {

            private int CPUCOUNT = Runtime.getRuntime().availableProcessors();

            private ThreadPoolExecutor cpuExecutor = new ThreadPoolExecutor(CPUCOUNT, CPUCOUNT,
                    30, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), sThreadFactory);

            private ThreadPoolExecutor iOExecutor = new ThreadPoolExecutor(64, 64,
                    30, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), sThreadFactory);

            private static final ThreadFactory sThreadFactory = new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);

                public Thread newThread(Runnable r) {
                    return new Thread(r, "ThreadPoolUtils #" + mCount.getAndIncrement());
                }
            };

            public static ExecutorService getService() {
                return sService;
            }

            private static ExecutorService sService = Executors.newFixedThreadPool(5, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "ThreadPoolUtils");
                    // 此Process为android.os包下，而非java.lang包
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    return thread;
                }
            });
        }