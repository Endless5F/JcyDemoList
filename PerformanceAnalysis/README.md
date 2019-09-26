## Android Studio 点击Build后的故事：
编译一个application module或者library文件，大致可以分为gradle task代表的五个阶段： 
1. Preparation of dependecies 在这个阶段gradle检测module依赖的所有library是否ready。
   如果这个module依赖于另一个module，则另一个module也要被编译。
2. Merging resources and processing Manifest 在这个阶段之后，资源和Manifest文件被打包。 
3. Compiling 在这个阶段处理编译器的注解，源码被编译成字节码。 
4. Postprocessing 所有带 “transform”前缀的task都是这个阶段进行处理的。 
5. Packaging and publishing 这个阶段library生成.aar文件，application生成.apk文件。


