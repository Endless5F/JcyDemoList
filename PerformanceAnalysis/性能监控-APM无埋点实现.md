APM 全称 Application Performance Management & Monitoring (应用性能管理/监控)
## 数据的价值
### APM
1. 目标：对应用的性能、业务可靠性进行线上的监控和预警
2. 采集内容： 系统指标,应用性能指标,Crash,自定义日志等
### 用户行为
0. 概念：用户行为可以用5W2H来总结：Who(谁)、What(做了什么行为)、When(什么时间)、Where(在哪里)、Why(目的是什么)、How(通过什么方式)，Howmuch(用了多长时间、花了多少钱)。
1. 目标：精细化运营

    用户行为分析就是通过对这些数据进行统计、分析，从中发现用户使用产品的规律，并将这些规律与网站的营销策略、产品功能、运营策略相结合，发现营销、产品和运营中可能存在的问题，解决这些问题就能优化用户体验、实现更精细和精准的运营与营销，让产品获得更好的增长。
2. 采集内容
    1. 从用户属性——性别、地域、收入、家庭状况
    2. 从用户生命周期——注册、活跃、流失
    3. 从用户行为——功能、内容、产品的喜好等
3. 技术手段详见：PerformanceAnalysis/性能分析相关图/APM性能监控数据获取技术手段图示.png
4. Java层实现功能
    1.  自定义业务数据链路化
    2.  内存指标
    3.  CPU指标
    4.  FPS 指标
    5.  ANR日志
    6.  卡顿检测
    7.  GC日志
    8.  Crash日志
    9.  Http指标数据(暂时只支持OkHttp）
    10. 电量指标
    11. MAOP，使用注解和配置文件AOP指定方法执行前，执行后，异常插入指定代码的功能（项目也实现动态日志功能，但是好像没有很好的使用场景）
    12. Remote下发命令，执行shell和动态执行代码功能
    13. 交互分析：分析Activity生命周期耗时
## Android AOP
在Android编译过程中，使用自定义的gradle插件，注册新的tranform任务,在java compile之后，修改编译后class文件的内容，在切入点增加相应的代码来实现AOP，实现无埋点功能。
### Android的编译流程
详见：PerformanceAnalysis/性能分析相关图/Android APK的编译流程.png
### Gradle
1. Transform API

        Android Gradle 工具在 1.5.0 版本后提供了 Transfrom API, 允许第三方 Plugin 在打包 dex 文件之前的编译过程中操作 .class 文件。
        目前 jarMerge、proguard、multi-dex、Instant-Run 都已经换成 Transform 实现，我们注册自己的transform实现对class文件的修改。

        Transform具体的详解：https://www.jianshu.com/p/37df81365edf
2. 自定义Gradle插件

        自定义Gradle Plugin使用的是Groovy语言，需要对groovy有一定的了解，着重了解它的闭包概念，和各种省略写法规则。

        深入理解Android之Gradle：https://blog.csdn.net/innost/article/details/48228651
        AndroidStudio中自定义Gradle插件：https://blog.csdn.net/huachao1001/article/details/51810328
3. 调试gradle插件
    1. 创建新的Configuration,Run->Edit Configurations->Remote。图示详见：PerformanceAnalysis/性能分析相关图/调试自定义gradle插件-创建新的Configuration.png
    2. 终端(Terminal，Studio自带)调用命令

            ./gradlew clean
            ./gradlew assembleDebug -Dorg.gradle.daemon=false -Dorg.gradle.debug=true
    3. App栏选择gradleDebug,点击debug按钮,AS会断点的地方停下来等待调试
### Class文件格式
图示详见：PerformanceAnalysis/性能分析相关图/Class文件格式部分图示.png
### Java AOP
常见的字节码生成工具有: ASM, AspectJ,Javassit ，Javapoet ,Spring,CGLib等，Spring,CGLib这些应为他们的作用机制原因，在 Android中无法使用，ASM可对指令流程有很好的分析能力，非常适用我们的场景，最终选择了ASM，其他的工具都都有各自的使用场景。
### ASM
ASM 是一个 Java 字节码操控框架。它能被用来动态生成类或者增强既有类的功能。ASM 可以直接产生二进制 class 文件，也可以在类被加载入 Java 虚拟机之前动态改变类行为。Java class 被存储在严格格式定义的 .class 文件里，这些类文件拥有足够的元数据来解析类中的所有元素：类名称、方法、属性以及 Java 字节码（指令）。ASM 从类文件中读入信息后，能够改变类行为，分析类信息，甚至能够根据用户要求生成新类,ASM通过 “Visitor” 模式将 “.class” 类文件的内容从头到尾扫描一遍.

ASM4使用指南：https://github.com/SusionSuc/AdvancedAndroid/blob/master/gradle插件与字节码注入/ASM4使用指南.pdf

### ASM开发的高效工具
1. Decompiler：字节码反编译的工具，as自带，用来检查生成的代码是否满足class 文件格式
2. Classpy：图形化的class 文件分析工具，功能和 javap类似，界面参考了Java Class Viewer。Github地址：https://github.com/zxh0/classpy
3. ASM Bytecode Outline：IDEA Plugin —— https://plugins.jetbrains.com/plugin/5918-asm-bytecode-outline

    它有一个Bytecode视图，该视图可以查看当前激活的源码视图所对应的类 用asm应该如何生成。也就是说，如果你知道改造后的类的字节码，就可以通过该视图得到改造的过程，asm的调用语句应该怎么写。研究asm时特别有用，值得推荐。

4. dx命令：dx --debug --dex —output=～/Desktop/out.dex  ～/Desktop/dex

    强烈推荐，当你ASM写完之后，就是dx不过时，可以使用这个命令，它可以提示你具体哪条指令出现了什么问题，非常提高效率。

### 函数插桩（Gradle + ASM）：https://www.jianshu.com/p/16ed4d233fd1

### MAOP
提供简单使用配置文件或者使用注解指定哪个类的哪个方法需要在方法执行前，执行后，执行异常时进行拦截，实现日志代码和业务代码的分离，如果业务觉得匹配规则不够灵活和丰富，也可以选择匹配语法和场景更多的AspectJ，同时需要一定学习成本。

## 交互分析
一般的交互分析需求，一般业务方法中插入代码或者监听系统提供的回调接口就能实现，Mas实现了：App启动，App结束，页面浏览，OnClick几种基本事件监控。
1. 点击事件：通过Aop在View.OnClickListener.onClick插入代码实现,具体实现查看项目 plugin下的 MasAnalyticsClassVisitor实现
2. App启动，App结束，页面浏览：Activity页面浏览都可以通过 Application.ActivityLifecycleCallbacks来实现，具体查看项目中的MasDataActivityLifecycleCallbacks
3. Crash：通过Thread.setDefaultUncaughtExceptionHandler(this)获取
4. 内存监控：
    1. 实现监控内存随使用时间改变的曲线：每隔一段时间获取堆内存和进程内存的使用情况,从而获得随使用时间而变化的内存曲线.
    2. OOM事件时检测fd,thread数目
    3. Activity泄漏的检测
    4. 低内存状态检测
    5. 注：详细解析：https://www.jianshu.com/p/6e1b5423ad68
5. Cpu：通过读取/proc/stat和/proc/pid/stat文件获取cpu数据进行计算，具体计算方法可以参考项目里CpuSnapshot类和CpuMonitor类实现
6. GC日志：GC日志暂且只找到通过Runtime.exc执行"logct -v time",抓取logicat日志匹配的方法，运行时可以捕捉自己进程打印的logcat日志，gc日志捕捉应该还有更好的方式实现，查看dalvik和art的源码可以知道，Gc打印都会调用系统的logcat接口，如果能在native层hook住方法调用，是否是更好的实现，后面继续研究下native inline和PLT hook的技术实现,现有实现具体查看MemMonitor类
7. 电量：通过注册系统recevier，具体可查看BatteryMonitor类,但是只能知道系统电量的变化，对于应用电量消耗并不能很好的描述，如何描述应用电量的消耗还需后续实现，Google之后发现，Android4.4之后，系统将电量权限收掉之后参考其他项目的实现，好像都是模仿整个Android系统计算电量的公式重新做了一遍，这个还需要学习系统如何计算电量的。
8. ANR日志：通过FileObserver监听/data/anr/traces.txt读写事件，根据anr日志的固定格式解析出属于当前包名的Anr日志， anr数据一般保持最后一次数据, 但是 Huawei 有些机型最新的anr是写在最后的，采集的时候需要采集最新的ANR,具体实现查看ANR类日志内容。实践中发现FileObserver只能在第一次Anr发生的时候被通知，再次发生不会有通知，增加日志里获取 Wrote stack traces to '/data/anr/traces.txt' 内容来获取通知。
9. 卡顿检测：使用Looper的print机制，计算当前主线操作执行的时间来确定是否卡顿，实现时可通过watchdog实现，但是正确的堆栈数据比较难获取，通过多次获取MainThread的堆栈来增加准确性，具体查看UIMonitor类
10. FPS：通过Choreographer.getInstance().postFrameCallback(frameCallback),设置回调来统计帧数，不过并不是十分准确，具体查看Fps类，在只有进程当下的权限的下还没有找到更好的方式，有更高权限的情况下可通过gxinfo实现。
11. Http指标数据：暂时只支持OkHttp3，不过其他httpclient实现原理也大致相同。
    项目当前实现的http 请求耗时，异常，数据大小,状态码 的获取，直接使用前面实现的MAOP，
    拦截OkHttpClient.Builder的build方法加入统计Interceptor ,DNSLookUp  耗时，连接耗时，ssl耗时，
    通过OkhttpClient 3.10之后设置EventListener.Factory，可以直接收集，
    首包时间需要拦截OkHttp读请求数据的方法来实现，OKHttpClient 最终调用CallServerInterceptor,关键代码就是读取readResponseHeaders的时机。
    详细请参考基于OkHttp的Http监控这篇文章：https://www.jianshu.com/p/60cfd0282930
12. Remote执行：云端下发命令，执行shell和动态执行代码功能

## 开源APM
* Matrix（微信）：https://github.com/Tencent/matrix
* booster（滴滴）：https://github.com/didi/booster











