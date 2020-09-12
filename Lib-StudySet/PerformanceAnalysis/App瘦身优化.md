## 一、瘦身优化及APK分析方案
1. 瘦身优势
    1. 最主要是转换率：下载转换率(提高用户搜索后的下载几率)
    2. 应用商店头部App大部分都有Lite版(精简版)
    3. 渠道合作商的要求(APP做大后可能有渠道商预装此应用)
2. Apk组成
    1. 代码相关：classes.dex
    2. 资源相关：res、asserts、resources.arsc
    3. So相关：lib（优化空间很大）
3. Apk分析
    1. ApkTool(官网：https://ibotpeaches.github.io/Apktool/)，反编译工具。命令：apktool d xxx.apk
    2. Analyze APK(Android Studio 2.2之后自带)：查看Apk组成、大小、占比；查看Dex文件组成；Apk对比
    3. https://nimbledroid.com/ (app性能分析)：显示文件大小及排行；显示dex方法数、SDK方法数；启动时间、内存等信息
    4. android-classyshark(二进制检查工具，https://github.com/google/android-classyshark)：支持多种格式:Apk、Jar、Class、So等；使用简单，图形化显示
## 二、代码瘦身实战
1. 代码混淆

    * 也被称为花指令，功能等价但改变形式：代码中各个元素改写成无意义的名字，以更难理解的形式重写部分逻辑，打乱代码的格式
    * Proguard(java类文件处理工具，优化字节码)：代码中的元素名称改短，移除冗余代码、增加代码反编译的难度，一定程度上保证代码安全
    * Proguard使用：配置minifyEnabled为true，debug下不要配置，并且在proguard-rules中配置相应规则。

            buildTypes {
                debug {
                    minifyEnabled false
                    shrinkResources false
                    debuggable true
                }
                release {
                    minifyEnabled true
                    signingConfig signingConfigs.release
                    proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
                }
            }
2. 三方库处理
    1. 基础库的统一(如：同一种功能的不同的库，只引用一个)
    2. 选择更小的库——Android Methods Count(插件，在build.gradle中显示引入的三方库的方法数)
    3. 仅引入需要的部分代码——比如：Fresco的webp支持部分
3. 移除无用代码
    1. 问题：平时开发业务代码只加不减，并且代码太多不敢删除
    2. 解决：AOP统计使用情况

            // 被使用Task输出log，根据类的构造方法统计使用情况
            @After("execution(com.android.performanceanalysis.launchstarter.task.Task.new(..))")
            public void newObject(JoinPoint point){
                LogUtils.i( " new  " + point.getTarget().getClass().getSimpleName());
            }
## 三、资源瘦身实战
1. 冗余资源：Android studio中右键-->Refactor-->Remove Unused Resource

2. 图片压缩
    1. https://tinypng.com/ 图片压缩网站
    2. TingPngPlugin 插件
    3. 图片格式选择：相同的图片转换成webp之后会有一定幅度的压缩、png是无损格式(保留所有色彩)，而jpg是有损格式，当图片尺寸大或者颜色鲜艳时png的体积会明显大于jpg
3. 资源混淆：https://github.com/shwenzhang/AndResGuard (Android资源混淆工具，使冗长的资源路径变短)
4. 其他
    1. 图片只保留一份(只放在xhdpi下，其他dpi下会等比例的进行缩放)
    2. 资源在线化(将图片资源放在远端，结合预加载的手段)
5. 使用矢量图：可缩放矢量图形（英语：Scalable Vector Graphics，SVG）是一种基于可扩展标记语言（XML），用于描述二维矢量图形的图形格式。SVG由W3C制定，是一个开放标准。可以使用矢量图形来创建独立于分辨率的图标和其他可伸缩图片。使用矢量图片能够有效的减少App中图片所占用的大小，矢量图形在Android中表示为VectorDrawable对象。
    1. 优点：图片扩展性(不损伤图片质量，一套图适配所有)、图片非常小(比使用位图小十几倍，有利于减小apk体积)
    2. 缺点: 性能优损失，系统渲染VectorDrawable需要花费更多时间，因为矢量图的初始化加载会比相应的光栅图片消耗更多的CPU周期，但是两者之间的内存消耗和性能接近；而且矢量图主要用在色调单一的icon。
6. 需要注意的是在Android构建流程中AAPT会使用内置的压缩算法来优化res/drawable/目录下的PNG图片，但也可能会导致本来已经优化过的图片体积变大，可以通过在build.gradle中设置cruncherEnabled来禁止AAPT采用默认方式优化我们已经优化过的图片。

        aaptOptions {
            cruncherEnabled = false
        }
## 四、So瘦身实战
0. 了解：So是Android上的动态链接库，有七种不同类型的CPU架构

        ndk {
            abiFilters 'armeabi' // 设置支持的So架构
        }
1. So移除：一般选择armeabi(万金油，但是性能有所损耗)
2. 更优方案：对于性能敏感模块使用的So可以都放在armeabi目录，然后通过代码判断设备的CPU类型，再加载其对应架构的SO文件，例如微信就是这么做的。既缩减了Apk的体积，也不影响性能敏感模块的执行。
    1. https://www.cnblogs.com/dongweiq/p/6824727.html (Android SO文件的兼容和适配)
    2. https://blog.csdn.net/DJY1992/article/details/78890252 (Android 静态和动态的调用so库（JNI）)

            //伪代码演示逻辑
            String abi = "";
            //获取abi类型
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                abi = Build.CPU_ABI;
            }else {
                abi = Build.SUPPORTED_ABIS[0];
            }
            //根据abi类型进行相应的加载
            if (TextUtils.equals(abi,"ARMv7")){
                //加载特定平台的so
            } else {
                //正常加载
            }
3. 其他方案：So动态加载、插件化
## 五、Apk瘦身之7Zip压缩
我们知道Apk文件实际上就是一个Zip文件。Android SDK的打包工具apkbuilder采用的是Deflate算法将Android App的代码、资源等文件进行压缩，压缩成Zip格式，然后签名发布。

既然是压缩，那能不能改进其压缩方式，获取更小的Apk文件？通过分析Apk打包的流程图我们可以发现SignedJarBuilder类对整个工程包括代码Dex和一些课压缩的资源、文件进行压缩，使用的是JDK中zip包下提供的算法。

简单的方式我们可以在不改变App编译器工作的情况下，对生成的Apk文件进行二次压缩，同样使用Deflate算法，但是将压缩等级从标准提升到极限压缩。提高压缩级别可在不对Apk包本身的内容做任何修改的情况下得到更小的Apk。

备注：

* 需要注意这样极限压缩之后的签名被破坏，需要重新签名。
* Android平台对Apk安装包的解压算法只支持Deflate算法，其它算法如LZMA，虽然压缩率更好，但是由于Android平台默认不支持，所以如果采用这种算法压缩Apk，会导致Apk无法安装。
* 目前在Mac上没发现好用的7Zip压缩软件，需要在Windows下使用。

7Zip压缩图示详见：PerformanceAnalysis/性能分析相关图/7Zip压缩.JPG