## 一、电量优化介绍及方案选择
1. 正确认识
    1. 电量重视度不够：开发中一直连着手机
    2. 电量消耗线上难以量化
2. 方案介绍：
    1. 设置界面-耗电排行：优点是直观，但是没有详细数据，对解决问题没有太多帮助。因此我们需要找特定场景专项测试(如在详情页中进行一段时间的操作，再看设置中的耗电排行，来简单判断)
    2. 注册电量相关的广播ACTION_BATTERY_CHANGED：获取电池电量、充电状态、电池状态等信息，价值不大：针对手机整体的耗电量。而非特定的App

            //示例代码
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            Intent intent = registerReceiver(null, filter);
            LogUtils.i("battery " + intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1));
    3. Battery Historain：功能强大，推荐使用

        Google推出的一款Android系统电量分析工具，支持5.0(API21)及以上系统的电量分析。可视化的展示指标:耗电比例、执行时间、次数，适合线下使用
3. 电量相关测试
    1. 耗电场景测试：复杂运算、视频播放
    2. 传感器相关：使用时长、耗电量、发热
    3. 后台静默测试(app退至后台一段时间，查看耗电量)
## 二、Battery Historain实战分析
1. 安装

        Github地址：https://github.com/google/battery-historian
        简单安装步骤：
            1. 安装Docker软件（http://www.runoob.com/docker/docker-tutorial.html 教程）
            2. 命令行执行 docker – run -p :9999 gcr.io/android-battery-historian/stable:3.0 --port 9999
2. 导出电量信息相关命令

        电量重置：adb shell dumpsys batterystats --reset
        开始记录电量信息：adb shell dumpsys batterystats --enable full-wake-history
        导出电量信息(此过程可能有点慢，等待即可)：adb shell bugreport bugreport.zip
3. 上传分析

        上传分析前提：Battery Historain安装完成
        上传分析步骤：
            1. 浏览器输入：http://localhost:9999
            2. 上传bugreport文件即可
        备用(若安装有问题，可上传至此备用网站分析，需翻墙哦)：https://bathist.ef.lc/
     说明一下Battery Historain的相关参数信息：
     1. 横坐标： 横坐标是一个时间范围。以重置为起点，获取bugreport内容时刻为终点。坐标的间隔，会随着时间的长度发生改标。
     2. 纵坐标： 重要参数：wake_lock、plugged、battery_level、screen

        纵坐标的相关参数：

            1. CPU running：CPU的运行状态，黑色是running状态，空白表示CPU处于空闲状态。
            2. Kernel only uptime：kernel运行时间。
            3. Userspace wakelock：代表应用持有wakelock锁的总时间，但是不会精确到某个app持有wakelock的时间。
               ps：系统为了节省电量，CPU在没有任务忙的时候就会自动进入休眠。有任务需要唤醒CPU高效执行的时候，就会给CPU加wake_lock锁。
               注：kernel wakelock和userspace wakelock都有可能阻止系统睡眠。
            4. Long Wakelocks：如果已将唤醒锁保持一分钟以上，则当前将其记录为已保留很长时间。此处显示的时间是保持唤醒锁的总量。
            5. Screen：屏幕状态——红色：亮屏，空白：灭屏。（这一点，可以用于考虑睡眠状态和点亮状态下电量的使用信息。）
            6. Top app：显示当前时刻哪个app处于最上层，就是当前手机运行的app。（用来判断某个app对手机电量的影响，这样也能判断出该app的耗电量信息。该栏记录了应用在某一个时刻启动，以及运行的时间，这对我们比对不同应用对性能的影响有很大的帮助。）
            7. Activity Manager Proc：activity的进程信息，包括该进程的开始时间和结束时间。
            8. Doze：Doze模式的状态（Android 6.0引入的Doze机制在于节省系统耗电量，保护电池，延长电池的使用时间。当设备未连接至电源，且长时间处于闲置状态时，系统会将应用进入Doze，置于App Standby模式。而最终的效果，能提升30%的电量续航能力。
               ps：Android中的Doze模式 —— https://www.jianshu.com/p/d62d58d6ba5a
            9. JobScheduler：JobScheduler是Android L版本新引入的API，JobScheduler，顾名思义，是用来调度工作。工作被调度的条件包括网络变化，充电插拔，周期执行等。使用场景包括wifi条件下数据下载上传等等。
            10.GPS：GPS的使用情况，红色：使用，空白：闲置。
            11.Network connectivity：网络连接状态（wifi、mobile是否连接）
            12.Mobile signal strength：移动信号——绿色：great，黄色：good，橘色：moderate，红色：poor，空白：none。
            13.Wifi scan：Wifi扫描状态——绿色：在扫描，空白：没有扫描。
            14.Wifi supplicant：无线客户端，底层的一个demo，在wifi运行的时候，这个也会运行。
            15.Wifi running：wifi组件是否在工作(未传输数据)
            16.Wifi on：wifi是否开启
            17.Package install：是否在进行包安装
            18.Battery level：电池电量水平，可以看出电量的变化。
            19.Plugged：充电状态，这一栏显示是否进行充电，以及充电的时间范围。
            20.Charging on：是否在充电，充电时段
            21.Temperature：电池温度
            22.health：电池健康状态的信息，这个信息一定程度上反映了这块电池使用了多长时间。这一栏记录电池状态在何时发生改变

        简洁介绍：

            纵坐标	                解释
            CPU runing	            cpu运行的状态，是否被唤醒
            Kernel only uptime	    只有内核运行时间
            Activity Manager Proc	活跃的用户进程
            Mobile network type	    网络类型
            Mobile radio active	    移动蜂窝信号 BP侧耗电
            Crashes(logcat)	        某个时间点出现crash的应用
            Doze	                是否进入doze模式
            Device active	        和Doze相反
            JobScheduler	        异步作业调度
            SyncManager	            同步操作
            Temp White List	        电量优化白名单
            Phone call	            是否打电话
            GPS	                    是否使用GPS
            Network connectivity	网络连接状态（wifi、mobile是否连接）
            Mobile signal strength	移动信号强度（great\good\moderate\poor）
            Wifi scan	            是否在扫描wifi信号
            Wifi supplicant	        是否有wifi请求
            Wifi radio	            是否正在通过wifi传输数据
            Wifi signal strength	wifi信号强度（great\good\moderate\poor）
            Wifi running	        wifi组件是否在工作(未传输数据)
            Wifi on	                同上
            Audio	                音频是否开启
            Camera	                相机是否在工作
            Video	                是否在播放视频
            Foreground process	    前台进程
            Package install	        是否在进行包安装
            Package active	        包管理在工作
            Battery level	        电池当前电量
            Temperature	            电池温度
            Charging on	            在充电
            Logcat misc	            是否在导出日志
        图示详见：PerformanceAnalysis/性能分析相关图/Battery Historain电量消耗分析.png
## 三、电量辅助监控
1. 运行时能耗

        获取各个模块的能耗(如屏幕、wifi，bluetooth等)
        adb pull /system/framework/framework-res.apk
        反编译，xml ————> power_profile.xml
        文件内容图示详见：PerformanceAnalysis/性能分析相关图/Battery Historain电量消耗分析.png
    通过反编译出来的文件，可以帮助我们在app测试以及开发过程当中，知道在此手机上哪个模块比较耗电，以及在什么状态下此模块耗电量高

2. 运行时获取使用时长——Aop辅助统计：次数、时间

    以WakeLock为例子：

        https://www.jianshu.com/p/67ccdac38271 (Android 功耗分析之wakelock)
        https://www.cnblogs.com/leipDao/p/8241468.html (安卓电量优化之WakeLock锁机制全面解析)
    代码详见：com.android.performanceanalysis.utils.WakeLockUtils 和 com.android.performanceanalysis.hook.ActivityHooker
3. 线程运行时长：超过阀值预警(运行时间过长可以停止)，代码详见：com.android.performanceanalysis.hook.ActivityHooker

## 四、电量优化套路总结
1. CPU时间片：获取运行过程中CPU的消耗，定位CPU占用率异常位置。减少后台应用的主动运行
2. 网络相关：发起网络请求时机及次数，数据压缩、减少时间，禁止使用轮询功能进行业务操作
3. 定位相关：根据场景谨慎选择定位模式（精度，频率），考虑网络定位代替GPS，使用后及时关闭以及减少更新。
4. 界面相关：离开界面后停止相关活动，耗电操作判断前后台(比如说：执行的动画，可见时开始，不可见时结束)。
5. WakeLock相关：注意成对出现：acquire与release，使用带参数的acquire，finally确保一定会被释放。常亮场景使用keepScreenOn即可
6. JobScheduler

        在符合某些条件时创建执行在后台的任务
        把不紧急的任务放到更合适的时机批量处理
        https://www.jianshu.com/p/9fb882cae239 (Android Jobscheduler使用)










