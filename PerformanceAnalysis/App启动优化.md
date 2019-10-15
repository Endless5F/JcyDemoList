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
3. LifeCycle（只重走activity的生命周期，较快）
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

    参考文档：
    https://www.jianshu.com/p/f577aec99e17 （关于android中使用AspectJ）
    https://blog.csdn.net/qq_30682027/article/details/82493913 (AspectJ详解)
    https://www.jianshu.com/p/27b997677149 (AspectJ基本用法)
 2. 实战：
