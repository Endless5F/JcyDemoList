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

## 开源库
一个简单易用的android apm框架（工具）：https://github.com/SusionSuc/rabbit-client

性能优化：https://jsonchao.github.io/categories/%E6%80%A7%E8%83%BD%E4%BC%98%E5%8C%96/