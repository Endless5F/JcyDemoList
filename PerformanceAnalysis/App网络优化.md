## 一、网络优化从哪些纬度开展
1. 网络优化介绍(正确认识)：
    1. 网络优化需要从多个纬度展开
    2. 仅仅重视流量不够
    3. 网络流量消耗量：精准
    4. 整体均值掩盖单点问题(如：用户反馈app费流量，只统计流量消耗，不统计使用时长不好断定，还有前后台消耗流量的区分)
    5. 网络相关监控要全面
    6. 粗粒度监控不能帮助我们发现、解决深层次问题
2. 网络优化纬度
    1. 流量消耗
        1. 一段时间流量消耗的精准度量，网络类型、前后台
        2. 监控相关：用户流量消耗均值、异常率(消耗多、次数多)
        3. 完整链路全部监控(Request、Response记录下来)，主动上报(超过阈值)
    2. 网络请求质量
        1. 用户体验：请求速度、成功率
        2. 监控相关：请求时长、业务成功率、失败率、top失败接口
    3. 其他：耗电(网络请求密集) ；公司成本：带宽、服务器数、CDN
3. 网络优化误区
    1. 只关注流量消耗，忽视其他纬度
    2. 只关注平均值、整体，忽略个体
## 二、网络优化工具选择
1. NetWork Profiler：显示实时网络活动：发送、接收数据及连接数，需要启用高级分析，只支持HttpURLConnection和Okhttp网络库
    1. 开启高级分析功能详见：PerformanceAnalysis/性能分析相关图/NetWork Profiler启动高级分析功能.png
    2. NetWork Profiler使用详见：PerformanceAnalysis/性能分析相关图/NetWork Profiler监控网络的使用.png

2. 抓包工具：Charles、Fiddler、Wireshark、TcpDump

3. Stetho：应用调试桥，连接Android和Chrome，网络监控、视图查看、数据库查看、命令行扩展等


        // Stetho的使用：
            implementation 'com.facebook.stetho:stetho:1.5.1'
            implementation 'com.facebook.stetho:stetho-okhttp3:1.5.1'
            // Application中初始化
            Stetho.initializeWithDefaults(mContext);
        Chrome浏览器：chrome://inspect
        Stetho的使用图示详见：PerformanceAnalysis/性能分析相关图/Stetho的使用.png
## 三、精准获取流量消耗
0. 问题思考：如何判断app流量消耗偏高
    1. 绝对值看不出高低
    2. 对比竞品，相同Case对比流量消耗
    3. 异常监控，超过正常指标
1. 测试方案：设置——流量管理，设置只允许本App联网。此方案可以解决大多数问题，但是线上场景线下可能遇不到

2. 线上流量获取方案
    1. 普遍方案：TrafficStats：API8以上重启以来的流量数据统计

            getUidRxVytes(int uid) 指定Uid的接收流量
            getTotalTxBytes() 总发送流量
        总结：无法获取某个时间段内的流量消耗
    2. 第二种方案：NetworkStatsManager：API23之后流量统计，可获取指定时间间隔内的流量信息，也可以获取不同网络类型下的消耗

            代码详见：com.android.performanceanalysis.utils.NetStatusUtils
2. 前台后台流量获取

        难题：线上反馈App后台跑流量，只获取一个时间段的值不够全面
        方案详见：PerformanceAnalysis/性能分析相关图/前台后台流量获取图示.png
        代码详见：com.android.performanceanalysis.activity.MainActivity
    总结：有一定的误差，在可接收范围内。结合精细化的流量异常报警针对性的解决后台跑流量
## 四、网络请求流量优化
1. 使用网络的场景概述

        数据：Api、资源包（升级包、H5、RN）、配置信息
        图片：上传、下载
        监控：APM监控相关、单点问题相关
2. 优化之数据缓存：服务端返回加上过期时间，避免每次重新获取，节约流量且大幅提高数据访问速度，更好的用户体验，Okhttp、Volley都有较好的实践
    1. 代码详见：com.android.performanceanalysis.http.interceptor.NoNetInterceptor
3. 数据增量更新：1. 加上版本的概念，只传输有变化的数据；2. 配置信息、省市区等更新
4. 数据压缩
    1. Post请求Body使用GZip压缩
    2. 请求头压缩（只传递一次，以后只传递请求头的MD5值，服务端从之前的请求头中取）
    3. 图片上传之前必须压缩

            //图片压缩库
            implementation 'top.zibin:Luban:1.1.8'
            // 在demo中可以将1.94MB的图片压缩至446KB，且预览质量无差别
            // 以下代码是为了演示Luban这个库对图片压缩对流量方面的影响
            Luban.with(holder.imageView.getContext())
                .load(Environment.getExternalStorageDirectory()+"/Android/1.jpg")
                .setTargetDir(Environment.getExternalStorageDirectory()+"/Android")
                .launch();
5. 优化发送频率和时机
    1. 合并网络请求，减少请求次数(如埋点数据的统一上传)
    2. 性能日志上报：批量+特定场景上传(只在WiFi情况下上传)
6. 图片优化
    1. 图片使用策略细化：优化缩略图
    2. 使用WebP格式图片(https://www.upyun.com/products/process#pic 里面有介绍)
## 五、网络请求质量优化
1. 质量指标：网路请求成功率，网络请求速度

    http请求过程：

        1. 请求到达运营商的DNS服务器并解析成对应的IP地址
        2. 创建链接(TCP三次握手)，根据IP地址找到相应的服务器，发送一个请求
        3. 服务器找到对应资源原路返回给访问的用户
    DNS相关(网络请求一开始就受DNS影响)：

        1. 问题：DNS被劫持，DNS解析慢
        2. 方案：使用HttpDNS，绕过运营商域名解析过程(传统DNS向DNS53端口发送，HttpDNS向80端口发送)
        3. 优势：降低平均访问时长、提高连接成功率
    参考文档：

        https://blog.csdn.net/yb223731/article/details/82858057 (HttpDNS 服务详解)
        https://blog.csdn.net/z_xiaozhuT/article/details/80596469 (深入理解Http请求、DNS劫持与解析)
2. 使用OKhttp和HttpDNS——代码详见：com.android.performanceanalysis.http.dns.OkHttpDNS
3. 协议版本升级
    1. 1.0:版本的TCP连接不复用
    2. 1.1:引入持久连接，但数据通讯按次序进行
    3. 2：多工，客户端、服务器双向实时通信
4. 网络请求质量监控：监控接口请求耗时、成功率、错误码等，以及图片加载的每一步耗时
    1. 代码实现详见：com.android.performanceanalysis.http.listener.OkHttpEventListener
5. 网络容灾机制
    1. 备用服务器分流
    2. 多次失败后一定时间内不进行请求，避免雪崩效应
6. 其他
    1. CDN加速、提高带宽、动静资源分离(更新后清理缓存)
    2. 减少传输量，注意请求时机及频率
    3. OKhttp的请求池(OKHttp对单个Host的请求最大限制为5，若App中只使用一个域名，则可考虑增加一下对单个域名的最大限制。)
## 六、网络体系化的方案建设
1. 线下测试相关

        方案：至抓单独App
        侧重点：请求有误、多余、网络切换、弱网、无网测试
2. 线上监控相关
    1. 服务端：
        1. 请求耗时（区分地域、时间段、版本、机型）
        2. 失败率(业务失败和请求失败)
        3. 一段时间内(一天或者一周等)排行Top的失败接口、异常接口
    2. 客户端：
        1. 接口每一步详细信息(DNS、连接、请求等)
        2. 请求次数、网络包大小、失败原因
        3. 图片监控
    3. 异常监控体系
        1. 服务器防刷：超限拒绝访问
        2. 客户端大文件预警、异常兜底策略(如重连5次都失败了，延长重连时间，或者取消)
        3. 单点问题追查