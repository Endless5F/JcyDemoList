## 一、App启动优化介绍
### 背景
    用户的第一体验，八秒定律（70%用户放弃等待）
### 启动分类
    官方文档：App startup time官方教程（https://developer.android.google.cn/topic/performance/vitals/launch-time ）

1. 冷启动：耗时最多，衡量标准(启动优化以冷启动作为标准)

        冷启动的步骤：ClickEvent(用户点击) -> IPC（触发ipc）
        -> Process.start（进程的创建） -> ActivityThread（单独进程app的入口类似main）
        -> bindApplication（通过反射创建Application） -> LifeCycle（我们的activity） -> ViewRootImpl（界面的绘制）
2. 热启动：后台 -> 前台（最快）
3. 温启动：LifeCycle（只重走activity的生命周期，较快）
### 相关任务
1. 冷启动之前：启动app、加载空白window、创建进程（这三个都是系统行为，无法多做干涉）
2. 创建Application、创建主线程、创建MainActivity、加载布局、布置屏幕、首帧绘制
### 优化方向
    Application和Activity的生命周期
## 二、启动时间测量方式
1. adb命令方式：adb shell am start -W packagename/首屏Activity(完整路径)

        ThisTime：最后一个Activity的启动耗时
        TotalTime：所有Activity的启动耗时
        WaitTime：AMS启动Activity的总耗时
   特点：线下使用方便，不能带到线上、非严谨精确时间
2. 手动打点方式：可通过LaunchTimerUtil工具类进行打点计算

        启动位置放置在Application的attachBaseContext中，结束位置放置在MainActivity中
    特点：精确，可带到线上，推荐使用
    误区：onWindowFocusChange只是首帧时间
    正解：真实数据展示，首页第一条数据展示
## 三、启动优化工具选择
1. traceview：图形形式展示执行时间、调用栈等，信息全面，包含所有线程

    使用方式：

        第一种：使用Android studio的Android profiler点击cpu就可以查看
        第二种：嵌入代码
            Debug.startMethodTracing("文件名");//开始
            Debug.stopMethodTracing();//结束
          生成文件在sd卡：Android/data/packagename/files可以直接DeviceFileExplorer中找到
          或者通过adb pull /mnt/sdcard/文件名.trace 指定文件目录中.将trace导出指定的文件夹中

    参考文档：https://www.jianshu.com/p/7e9ca2c73c97 （Android性能优化—TraceView的使用）
    总结：traceview运行时开销严重，整体都会变慢（会抓取所有线程所有函数的状态和时间），可能会带偏优化方向（影响某些函数），可在代码中埋点使用（使用方便）
2. systrace：结合Android内核的数据，生成Html报告。API18以上使用，推荐TraceCompat

    使用方式：

        python systrace.py -t 10 [other-options][categories]     python脚本
        参数说明：https://developer.android.google.cn/studio/command-line/systrace

        示例(Application的OnCreate方法中)：
        TraceCompat.beginSection("AppOnCreate");//开始
        // TODO 一系列操作
        TraceCompat.endSection();//结束
        命令行：python systrace.py -b 32768 -t 5 -a com.android.performanceanalysis -o performance.html
        python命令说明： -b：buffer(TraceView收集的大小，默认8MB)  -t：时间  -a：监测的包名  -o：output(导出的文件名)
    参考文档：https://blog.csdn.net/itfootball/article/details/48915935 （Android性能专项测试之Systrace工具）

        cputime与walltime的区别（cputime是优化的核心）
        walltime：是代码执行的时间
        cputime：是代码消耗cpu的时间（重点指标）
        举例：锁冲突
    总结：轻量级，开销小。直观反映cpu利用率。
## 四、优雅获取方法耗时
### 常规方法
手动埋点(侵入性强，工作量大)：

        开始： long time = System.currentTimeMillis();
        结束： long cost = System.currentTimeMillis()-time;
### AOP
1. 介绍：

    Aspect Oriented Programming 面向切片编程：针对同一类问题的同一处理，无侵入添加代码

        AspectJ使用：
            最外层build.gradle：classpath 'com.hujiang.aspectjx:gradle-android-plugin-aspectjx:2.0.6'
            module build.gradle：apply plugin: 'android-aspectjx'
            module build.gradle：implementation 'org.aspectj:aspectjrt:1.8.4'
    几个主要概念：

    * Join Points：程序运行时的执行点，可以作为切面的地方
            比如：函数调用、执行/获取或者设置变量/类初始化
    * PointCut：带条件的JoinPoints
    * Advice：一种Hook，要插入代码的位置
        * before：PointCut之前执行
        * after：PointCut之后执行
        * Around：PointCut之前之后分别执行
    * 语法介绍：

        @Before("execution(* android.app.Activity.on**(…))")
        public void onActivityCalled(JoinPoints joinPoints)throw Throwable{ ... }
        * @Before：Advice，具体插入位置
        * execution：处理Join Point的类型，call、execution
        * (* android.app.Activity.on**(…)) 匹配规则
        * onActivityCalled：aop要插入的代码

    参考文档：https://blog.csdn.net/yxhuang2008/article/details/94193201
 2. 实战：

        /**
         * classpath 'com.hujiang.aspectjx:gradle-android-plugin-aspectjx:2.0.0'
         * implementation 'org.aspectj:aspectjrt:1.8+'
         * apply plugin: 'android-aspectjx'
         * <p>
         * 此版本可以如下使用：
         *
         * @Around("call(* com.android.performanceanalysis.LaunchApplication.**(..))")
         *
         * 新版本需要使用如下方式：
         */
        @Around("call(* com.android.performanceanalysis.LaunchApplication.init**(..))")
        public void getTime(ProceedingJoinPoint joinPoint) {
            // 获取切点处的签名
            Signature signature = joinPoint.getSignature();
            // 获取切点处的方法名
            String name = signature.toShortString();
            long time = System.currentTimeMillis();
            try {
                joinPoint.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            LogUtils.i("getTime " + name + " cost " + (System.currentTimeMillis() - time));
        }
## 五、异步优化详解
1. 优化技巧：Theme切换，感官上快

        // 1.自定义主题，防止启动页白屏
        <style name="AppTheme.Launcher">
            <item name="android:windowBackground">@mipmap/ic_launcher</item>
        </style>

        // 2.AndroidManifest.xml中设置启动页的主题为 1
        <activity
            android:name=".activity.MainActivity"
            android:theme="@style/AppTheme.Launcher">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        // 3. 在启动页onCreate方法的super.onCreate前重新设置回该有的主题
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
        }
2. 异步优化

        // 获取CPU数量
        private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
        // 根据CPU数量初始化核心线程数量
        private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
        // 使用线程池
        ExecutorService service = Executors.newFixedThreadPool(CORE_POOL_SIZE);
        service.submit(new Runnable() {
            @Override
            public void run() {
                //子线程中执行一个初始化的任务

            }
        });

        这种使用会出现application走完了但是线程中初始化并没有完成而出现错误
        解决办法：初始化一个倒计数器
        private CountDownLatch mCountDownLatch = new CountDownLatch(1);//表示CountDownLatch需要被满足一次，具体多少次自己根据场景进行设置

        // 示例代码：
        @Override
        public void onCreate() {
        super.onCreate();
            ExecutorService service = Executors.newFixedThreadPool(CORE_POOL_SIZE);
            service.submit(new Runnable() {
                @Override
                public void run() {
                    //子线程中执行一个初始化的任务

                }
            });

            service.submit(new Runnable() {
                @Override
                public void run() {
                    //子线程中执行一个初始化的任务
                    initWeex();
                    //在需要等待初始化完成的任务中调用，满足一次
                    mCountDownLatch.countDown();
                }
            });

            try {
                //mCountDownLatch在onCreate中最后一步进行等待
                mCountDownLatch.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    注意事项：

        1.不符合异步要求：可能某部分代码不可在异步中执行
        2.需要在某阶段必须完成：可以采用倒计时CountDownLatch的方式
        3.区分任务类型(CPU密集型和IO密集型)

## 六、异步初始化最优解-启动器
核心思想：充分利用cpu多核，自动梳理

    // 流程图查看性能分析包下：启动器流程图.png
    代码Task化，启动逻辑抽象为Task
    根据所有任务依赖关系排序生成一个有向无环图
    多线程按照排序后的优先级依次执行
具体代码以及示例可查看代码：com.android.performanceanalysis.launchstarter包和com.android.performanceanalysis.task包下Java文件

## 七、更优秀的延迟初始化方案
1. 常规方案：不需要在application中初始化的可以在首页展示后调用，new Handler().postDelayed()  缺点：时机不便控制、导致界面卡顿
2. 更优方案
    核心思想：对延迟的任务分批初始化，利用IdleHandler的特性，空闲执行
    具体代码详见：com.android.performanceanalysis.launchstarter.DelayInitDispatcher
## 八、启动优化总结
1. 优化总方针：异步、延迟、懒加载（如高德地图初始化只需要在使用的界面进行初始化即可），而且技术、业务要相结合
2. 通过命令获取应用的启动时间会活动两个时间：

    walltime(启动的总时间)和cputime(启动过程cpu执行的时间)，因此cputime才是我们优化的方向，按照systrace及cputime跑满cpu，使cpu无浪费
3. 注意事项：监控的完善、线上监控多阶段时间（App、Activity、生命周期间隔时间）、处理聚合看趋势
## 九、其他方案
1. 提前加载SharePreferences(在Multidex之前加载，充分利用此阶段的cpu)；若此时需要用getApplicationContext()，而此时该方法返回null，因此需要覆写getApplicationContext()返回this
2. 启动阶段不启动子进程，子进程会共享cpu资源，导致主进程cpu资源紧张
3. 类加载优化：提前异步类加载

        Class.forName()只加载类本身及其静态变量的引用类
        new 类实例 可以额外加载类成员变量的引用类
        哪些类需要提前异步类加载呢？（替换系统的ClassLoader，自定义的ClassLoader中添加log打印出所有的类就是需要处理的）
4. 黑科技系列

        启动阶段抑制GC（NativeHook的方案）
        CPU锁频（可能会引起其他问题，比如耗电量增加）
