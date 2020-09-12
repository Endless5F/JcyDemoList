此模块的学习以及笔记整理——基于性能优化视频以及相关一系列文章

## Android Studio 点击Build后的故事：
* 首先aapt工具会将资源文件进行转化，生成对应资源ID的R文件和资源文件。
* adil工具会将其中的aidl接口转化成Java的接口
* 至此，Java Compiler开始进行Java文件向class文件的转化，将R文件，Java源代码，由aidl转化来的Java接口，统一转化成.class文件。
* 通过dx工具将class文件转化为dex文件。
* 此时我们得到了经过处理后的资源文件和一个dex文件，当然，还会存在一些其它的资源文件，这个时候，就是将其打包成一个类似apk的文件。但还并不是直接可以安装在Android系统上的APK文件。
* 通过签名工具对其进行签名。
* 通过Zipalign进行优化，提升运行速度（原理后文会提及）。
* 最终，一个可以安装在我们手机上的APK了。

## 性能优化工具
### 启动优化
1. Traceview：图形形式展示执行时间、调用栈等，信息全面，包含所有线程
2. Systrace：结合Android内核的数据，生成Html报告。API18以上使用，推荐TraceCompat
3. AOP之Aspectj：面向切片编程，针对同一类问题的同一处理，无侵入添加代码
### 内存优化
1. Memory Profiler：Android Profiler 中的一个组件，可帮助你分析应用卡顿、崩溃、内存泄漏等原因。 它显示一个应用内存使用量的实时图表，让您可以捕获堆转储、强制执行垃圾回收以及跟踪内存分配。
2. Memory Analyzer（MAT）：强大的java heap分析工具、查找内存泄漏及内存占用，生成整体报告、分析问题等
3. ARTHooK(Epic)：pic是一个虚拟机层面、以java Method为粒度的运行时Hook框架，支持Android 4.0-10.0机型。
4. LeakCanary：自动内存泄漏检测
### 卡顿优化
1. CPU Profiler：Android Profiler 中的一个组件，图形的形式展示执行时间、调用栈等，信息全面，包含所有线程，运行时开销严重，整体都会变慢
2. Systrace：结合Android内核的数据，生成Html报告。API18以上使用，推荐TraceCompat
3. StrictMode：严苛模式，Android提供的一种运行时检测机制，方便强大，包含：线程策略和虚拟机策略检测
4. AndroidPerformanceMonitor实战：非侵入式的性能监控组件，通知形式弹出卡顿信息
5. FileObserver监控文件(data/anr/traces.txt)变化，高版本可能存在权限问题
6. ANR-WatchDog原理及实战：非侵入式的ANR监控组件，补充FileObserver监控文件高版本可能存在权限问题
7. AOP之Lancet(界面秒开)：Android 轻量级 AOP框架。优点：编译速度快，支持增量编译、API简单，没有任何多余代码插入apk。
### 布局优化
1. Systrace来进行布局优化
2. Layout Inspector来查看视图层级结构
3. 采用Choreographer来获取FPS以及自动化测量 UI 渲染性能的方式（gfxinfo、SurfaceFlinger等dumpsys命令）。
4. 使用AOP的方式去获取界面加载的耗时
5. 利用LayoutInflaterCompat.setFactory2去监控每一个控件加载的耗时。
### 电量优化
1. Battery Historain：Google推出的一款Android系统电量分析工具，支持5.0(API21)及以上系统的电量分析。可视化的展示指标:耗电比例、执行时间、次数，适合线下使用
### 线程优化
1. ARTHooK(Epic)
2. Android线程优先级(-20-19)Process.setThreadPriority(-19);
3. Java线程优先级(1-10)：thread.setPriority(6);
### 网络优化
1. NetWork Profiler：显示实时网络活动：发送、接收数据及连接数，需要启用高级分析，只支持HttpURLConnection和Okhttp网络库
2. 抓包工具：Charles、Fiddler、Wireshark、TcpDump
3. Stetho：应用调试桥，连接Android和Chrome，网络监控、视图查看、数据库查看、命令行扩展等
### AndroidPerformanceMonitor和ANR-WatchDog区别
AndroidPerformanceMonitor：监控主线程每一个Msg的执行
WatchDog：只看最总结果
前者适合监控卡顿，后者适合补充ANR监控
### Lancet和AspectJ对比
#### Lancet相对于AspectJ的优点
1. Lancet轻量级的框架，编译速度快，支持增量编译
2. Lancet语法简单，易于上手。AspectJ需要学习的语法比较多。
#### Lancet相对于AspectJ的缺点
Lancet仅支持hook具体的方法，不能像AspectJ一样根据自定义的注解来Hook一个类或者任意的方法。
#### 使用场景建议
1. 如果只是相对特定的函数，aar中函数、项目中的函数、Android系统源码中的函数进行Hook，可以选择使用Lancet。
2. 如果需要使用注解对某一类操作进行Hook时，例如，权限检查、性能检测等函数，可以使用AspectJ。
### LeakCanary 与 鹅场Matrix ResourceCanary对比分析
https://www.cnblogs.com/sihaixuan/p/11140479.html

对比图：PerformanceAnalysis/性能分析相关图/LeakCanary 与 鹅场Matrix ResourceCanary对比分析.png
## 开源库
一个简单易用的android apm框架（工具）：https://github.com/SusionSuc/rabbit-client

性能优化：https://jsonchao.github.io/categories/%E6%80%A7%E8%83%BD%E4%BC%98%E5%8C%96/