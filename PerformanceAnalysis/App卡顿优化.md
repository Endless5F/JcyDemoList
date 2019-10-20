## 一、卡顿介绍及优化工具选择
1. 背景

        对用户来说：很多性能问题不易被发现，但是卡顿很容易被直观感受
        对开发者来说：卡顿问题难以定位

        卡顿问题难在哪里：
            产生的原因错综复杂：代码、内存、绘制、IO？
            不易复现：与用户当时场景相关
2. CPU Profiler

        图形的形式展示执行时间、调用栈等
        信息全面，包含所有线程
        运行时开销严重，整体都会变慢

        // 若想具体监控某部分代码则需要如下设置：
        Debug.startMethodTracing(“文件名”);//使用
        Debug.stopMethodTracing();//结束
        生成文件在sd卡：Android/data/packagename/files可以直接DeviceFileExplorer中找到
        或者通过adb pull /mnt/sdcard/文件名.trace 指定文件目录中.将trace导出指定的文件夹中
3. Systrace：结合Android内核的数据，生成Html报告。API18以上使用，推荐TraceCompat

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
    优点：轻量级，开销小，直观反映CPU使用率，给出建议。
4. StrictMode：严苛模式，Android提供的一种运行时检测机制，方便强大，包含：线程策略和虚拟机策略检测

        线程策略：
            自定义的耗时调用：detectCustomSlowCalls
            磁盘读写操作：detectDiskReads、detectDiskReads
            网络操作：detectDiskWrites
        虚拟机策略
            Activity泄漏：detectActivityLeaks
            sqlite对象泄漏：detectLeakedSqlLiteObjects
            检测实例数量：setClassInstanceLimit
        示例代码：
            private void initStrictMode() {
                if (BuildConfig.DEBUG) {
                    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                            .detectCustomSlowCalls() //API等级11，使用StrictMode.noteSlowCode
                            .detectDiskReads() // 磁盘读取
                            .detectDiskWrites() // 磁盘写入
                            .detectNetwork()// or .detectAll() for all detectable problems
                            .penaltyLog() //在Logcat 中打印违规异常信息，Log通过StrictMode字段查看
                            .build());
                    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                            .detectLeakedSqlLiteObjects()
                            .setClassInstanceLimit(HomeData.class, 1) // 限制某类的数量
                            .detectLeakedClosableObjects() //API等级11
                            .penaltyLog()
                            .build());
                }
            }
## 二、自动化卡顿检测方案及优化
1. 自动化卡顿检测原理

        为什么需要自动化检测方案？
            系统工具只适合线下针对行分析
            线上及测试环境需要自动化检测方案
        方案原理：
            消息处理机制，一个线程只有一个Looper
            mLogging对象在每个message处理前后被调用
            主线程发生卡顿，是在dispatchMessage执行耗时操作
        具体实现：
            Looper.getMainLooper().setMessageLogging();设置我们的message
            匹配>>>>> Dispatching，在阀值时间后主线程开始执行任务（获取堆栈或者场景信息，比如：内存大小、网络状态等）
            匹配<<<<< Finished，任务启动之前取消掉
2. AndroidPerformanceMonitor实战：非侵入式的性能监控组件，通知形式弹出卡顿信息

        使用方式：
            第一步：implementation 'com.github.markzhai:blockcanary-android:1.5.0'
            Github地址：https://github.com/markzhai/AndroidPerformanceMonitor
            第二步：BlockCanary.install(this, new AppBlockCanaryContext()).start();
        示例代码详见：com.android.performanceanalysis.blockcanary.AppBlockCanaryContext
    方案总结：非侵入式，方便精准，定位到代码某一行
3. 问题及优化

    自动检测方案问题：确实卡顿了，但卡顿堆栈可能不准确。和OOM一样，最后堆栈只是表象，不是真正的问题（我们是在T2时刻获取的信息，但是其实这个已经晚了，实际的卡顿发生在T2之前）


    自动检测方案优化：

        获取监控周期内的多个堆栈，而不是最后一个
        startMonitor -> 高频采集堆栈 -> endMonitor
        记录多个堆栈 -> 上报
    海量卡顿堆栈处理：

        高频卡顿上报量太大，服务端有压力
        分析：一个卡顿下有多个堆栈大概率有重复
        解决：对一个卡顿下堆栈进行hash排重，找出重复的堆栈
        效果：极大的减少展示量，同时更高效找到卡顿堆栈
## 三、ANR分析与实战
1. ANR介绍与实战

    ANR介绍:

        keyDispatchTimeOut(按键或者触摸事件)，5s
        BroadCastTimeOut(广播),前台10s，后台60s
        ServiceTimeOut(服务),前台20s，后台200s
    ANR执行流程：发生ANR -> 进行接收异常终止信号，开始写入进行anr信息 -> 弹出ANR提示框(Rom表现不一)

    ANR解决套路：

        adb pull data/anr/traces.txt 存储路径
        详细分析：CPU、IO、锁
        https://www.jianshu.com/p/3959a601cea6 （ANR问题一般解决思路）
    线上ANR监控方案：

        通过FileObserver监控文件(data/anr/traces.txt)变化，高版本可能存在权限问题
        由于权限问题可能监控不到，这时就需要ANR-WatchDog
2. ANR-WatchDog原理及实战：非侵入式的ANR监控组件

    ANR-WatchDog原理：ANRWatchDog 就是一个Thread 在线程中通过主线程的handler发送一条信息进行+1的操作，接下来这个线程Sleep一段时间，Sleep后检测+1的操作是否被执行，执行了没有卡顿，没执行就是卡顿状态。

        使用方式：
            第一步：implementation 'com.github.anrwatchdog:anrwatchdog:1.4.0'
            第二步：new ANRWatchDog().start();
        Github地址：https://github.com/SalomonBrys/ANR-WatchDog
3. AndroidPerformanceMonitor和ANR-WatchDog区别

        AndroidPerformanceMonitor：监控主线程每一个Msg的执行
        WatchDog：只看最总结果
        前者适合监控卡顿，后者适合补充ANR监控
## 四、卡顿单点问题检测方案
1. 背景介绍

        自动卡顿检测方案并不够（如很多msg要执行，但是每一个msg都没有超过阀值，但是用户能感知到）
        建立体系化解决方案，就务必尽早暴露问题
        单点问题：主线程IPC、DB
2. IPC问题监测

        监测指标：
            IPC调用类型(比如：PackageManager)
            调用次数、耗时
            调用堆栈、发生线程
        常规方案：
            IPC前后加埋点
            不优雅、容易忘记
            维护成本大
        IPC问题监测技巧：
            adb命令：
                ipc监控开始：adb shell am trace-ipc start
                ipc监控结束并保存：adb shell am trace-ipc stop ——dump-file/data/local/tmp/ipc-trace.txt
                ipc监控文件导出：adb pull /data/local/tmp/ipc-trace.txt

        优雅方案：
            ARTHook还是AspectJ？
            ARTHook：可以Hook系统方法
            AspectJ: 非系统方法
          我们需要监控的是系统的，因此需要使用ARTHook。
          示例代码(详见：com.android.performanceanalysis.LaunchApplication)：
            // IPC监控
            // 所有的ipc操作都走BinderProxy的  https://www.jianshu.com/p/afa794939379 （震惊！Binder机制竟然恐怖如斯！）
            try {
                DexposedBridge.findAndHookMethod(Class.forName("android.os.BinderProxy"), "transact",
                        int.class, Parcel.class, Parcel.class, int.class, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                LogUtils.i( "BinderProxy beforeHookedMethod " + param.thisObject.getClass().getSimpleName()
                                        + "\n" + Log.getStackTraceString(new Throwable()));
                                super.beforeHookedMethod(param);
                            }
                        });
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
3. 单点问题监测方案

利用ARTHook完善线下工具，开发阶段Hook相关操作、暴露、分析问题

单点问题监控维度：IPC、IO、DB、View绘制

## 五、如何实现界面秒开
1. 界面秒开实现：界面秒开就是一个小的启动优化，可以借鉴启动优化及布局优化章节

        SysTrace：优雅异步+优雅延迟初始化(跑满cpu)
        异步Inflate、X2C、绘制优化
        提前获取页面数据(唯一路径界面)
2. 界面秒开率统计：onCreate到onWindowFocusChange的时间作为统计时间或者在Activity中设置特定接口进行统计

    Lancet介绍：Android 轻量级 AOP框架。优点：编译速度快，支持增量编译、API简单，没有任何多余代码插入apk。

        使用方式：
            dependencies{
                classpath 'me.ele:lancet-plugin:1.0.5'
            }
            插件 apply plugin: 'me.ele.lancet'
            依赖 provided 'me.ele:lancet-base:1.0.5'
        使用注解：
            @Proxy 通常用与对系统API调用的Hook
            @Insert 常用于操作App与library的类
        Github地址：https://github.com/eleme/lancet
3. 界面秒开监控纬度：总体耗时、生命周期耗时、生命周期间隔耗时

## 六、优雅监控耗时盲区
1. 耗时盲区(容易被忽视)监控背景介绍

        盲区1：生命周期间隔
        盲区2：onResume到Feed展示的间隔
        举例：在Activity中postMessage，很可能在某条真实数据展示出来之前执行
            // 以下代码是为了演示Msg导致的主线程卡顿
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    LogUtils.i("Msg 执行");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
    耗时盲区监控难点：只知道盲区时间，不清楚具体在做什么(onResume -> 某条真实数据展示出来之前干了什么，包括不同人添加的内容及第三方做的事情)、线上盲区无从排查
2. 耗时盲区监控方案

    耗时盲区监控线下方案：TraceView，特别适合一段时间内的盲区监控，线程具体时间做了什么，一目了然
    耗时盲区监控线上方案：

        思考分析：
            所有方法都是Msg，上面了解到AndroidPerformanceMonitor，是否可以通过mLogging来进行盲区检测？没有Msg具体堆栈（mLogging只知道主线程发生了Msg，但是不知道调用栈信息，它所知道的调用栈信息都是系统调用它的）
            AOP切Handler方法(sendMessage或者sendMessageDelayed)？此方式可知道发送Msg时的堆栈信息，但此方式只知道发送时间，不清楚准确执行时间
        方案：使用统一的Handler：定制具体方法（sendMessageAtTime和dispatchMessage）、定制gradle插件，编译期动态替换

## 七、卡顿优化技巧总结
1. 卡顿优化实践经验

        耗时操作：异步、延迟
        布局优化：异步inflate、X2C、重绘解决
        内存：降低内存占用，减少GC时间
2. 卡顿优化工具建设

        系统工具的认识、使用（Systrace-cpu使用、TraceView-每个线程在做什么、StrictMode）
        自动化监控及优化（AndroidPerformanceMonitor、ANR-WatchDog、高频采集，找出重复率高的堆栈）
        卡顿监控工具（单点问题：AOP、Hook，盲区监控：gradle编译期替换）
        卡顿监控指标(卡顿率、ANR率、界面秒开率，交互时间、生命周期时间，上报环境、场景信息)

