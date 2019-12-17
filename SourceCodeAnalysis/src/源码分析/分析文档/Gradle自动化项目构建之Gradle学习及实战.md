## Project
### Peoject定义：
    1. 从Gradle的角度看，Gradle的管理是树状结构的，最外层的是根project，里层module是子project。
    2. 每一个子project都会对应输出，比如：apk，war，aar等等这个依赖配置完成，
    3. 每个project的配置和管理都是依靠自己的build.gradle完成的，并且build.gradle文件也是是否为project的标识。
    4. 虽然Gradle的管理是树状结构，也可以在里层module中再创建module，但是实际开发中绝对不会在子project中再创建子project，因此此树状结构只有两层。
    注：通过命令：gradlew projects，可以验证Project的树状结构
### Project相关api
    api	   作用
    getAllprojects() 获取工程中所有的project（包括根project与子project）
    getSubProjects() 获取当前project下，所有的子project（在不同的project下调用，结果会不一样，可能返回null）
    getParent()      获取当前project的父project（若在rooProject的build.gradle调用，则返回null）
    getRootProject() 获取项目的根project（一定不会为null）
    project(String path, Closure configureClosure)  根据path找到project，通过闭包进行配置（闭包的参数是path对应的Project对象）
    allprojects(Closure configureClosure)	 配置当前project和其子project的所有project
    subprojects(Closure configureClosure)	 配置子project的所有project（不包含当前project）
### 属性相关api
1. 在gradle脚本文件中使用ext块扩展属性(父project中通过ext块定义的属性，子project可以直接访问使用)

        // rootProject : build.gradle
        ext { // 定义扩展属性
          compileSdkVersion = 28
          libAndroidDesign = 'com.android.support:design:28.0.0'
        }

        // app : build.gradle
        android {
          compileSdkVersion = this.compileSdkVersion // 父project中的属性，子project可以直接访问使用
          ...
        }
        dependencies {
          compile this.libAndroidDesign // 也可以使用：this.rootProject.libAndroidDesign
          ...
        }
2. 在gradle.properties文件中扩展属性

        // gradle.properties

        isLoadTest=true // 定义扩展属性
        mCompileSdkVersion=28 // 定义扩展属性

        // setting.gradle
        // 判断是否需要引入Test这个Module
        if(hasProperty('isLoadTest') ? isLoadTest.toBoolean() : false) {
          include ':Test'
        }

        // app : build.gradle
        android {
          compileSdkVersion = mCompileSdkVersion.toInteger()
          ...
        }
    1. hasProperty('xxx')：判断是否有在gradle.properties文件定义xxx属性。
    2. 在gradle.properties中定义的属性，可以直接访问，但得到的类型为Object，一般需要通过toXXX()方法转型。
### 文件相关API
    api	作用
    getRootDir()	获取rootProject目录
    getBuildDir()	获取当前project的build目录（每个project都有自己的build目录）
    getProjectDir()	获取当前project目录
    File file(Object path)	定位一个文件，相对于当前project开始查找
    ConfigurableFileCollection files(Object... paths)	定位多个文件，与file类似
    copy(Closure closure)	拷贝文件
    fileTree(Object baseDir, Closure configureClosure)	定位一个文件树（目录+文件），可对文件树进行遍历

    例子：
    // 打印common.gradle文件内容
    println getContent('common.gradle')
    def getContent(String path){
      try{
        def file = file(path)
        return file.text
      }catch(GradleException e){
        println 'file not found..'
      }
      return null
    }

    // 拷贝文件、文件夹
    copy {
      from file('build/outputs/apk/')
      into getRootProject().getBuildDir().path + '/apk/'
      exclude {} // 排除文件
      rename {} // 文件重命名
    }

    // 对文件树进行遍历并拷贝
    fileTree('build/outputs/apk/') { FileTree fileTree ->
        // 访问树结构的每个结点
        fileTree.visit { FileTreeElement element ->
            println 'the file name is: '+element.file.name
            copy {
                from element.file
                into getRootProject().getBuildDir().path + '/test/'
            }
        }
    }
### 依赖相关API
#### 1. 配置工程仓库及gradle插件依赖

    // rootProject : build.gradle
    buildscript { ScriptHandler scriptHandler ->
        // 配置工程仓库地址
        scriptHandler.repositories { RepositoryHandler repositoryHandler ->
            repositoryHandler.jcenter()
            repositoryHandler.mavenCentral()
            repositoryHandler.mavenLocal()
            repositoryHandler.ivy {}
            repositoryHandler.maven { MavenArtifactRepository mavenArtifactRepository ->
                mavenArtifactRepository.name 'personal'
                mavenArtifactRepository.url 'http://localhost:8081/nexus/repositories/'
                mavenArtifactRepository.credentials {
                    username = 'admin'
                    password = 'admin123'
                }
            }
        }
        // 配置工程的"插件"（编写gradle脚本使用的第三方库）依赖地址
        scriptHandler.dependencies {
            classpath 'com.android.tools.build:gradle:2.2.2'
            classpath 'com.tencent.tinker-patch-gradle-plugin:1.7.7'
        }
    }

    // =========================== 上述简化后 ============================

    buildscript {
        /**
         * 配置工程仓库地址
         *  由于repositories这个闭包中的delegate是repositoryHandler，
         *      因此可以省略repositoryHandler的引用，直接使用其属性和方法。
         */
        repositories {
            jcenter()
            mavenCentral()
            mavenLocal()
            ivy {}
            maven {
                name 'personal'
                url 'http://localhost:8081/nexus/repositories/'
                credentials {
                    username = 'admin'
                    password = 'admin123'
                }
            }
        }
        // 配置工程的"插件"（编写gradle脚本使用的第三方库）依赖地址
        dependencies {
            classpath 'com.android.tools.build:gradle:2.2.2'
            classpath 'com.tencent.tinker-patch-gradle-plugin:1.7.7'
        }
    }
#### 2. 配置应用程序第三方库依赖
    // app : build.gradle
    dependencies {
        implementation fileTree(dir: 'libs', include: ['*.jar']) // 依赖文件树
        // compile file() // 依赖单个文件
        // compile files() // 依赖多个文件
        implementation 'com.android.support:appcompat-v7:28.0.0' // 依赖仓库中的第三方库（即：远程库）
        implementation project('CommonSDK') { // 依赖工程下其他Module（即：源码库工程）
          exclude module: 'support-v4' // 排除依赖：排除指定module
          exclude group: 'com.android.support' // 排除依赖：排除指定group下所有的module
          transitive false // 禁止传递依赖，默认值为false
        }
        implementation('xxx') {
            changing true // 每次都从服务端拉取
        }

        // 栈内编译
        provided('com.tencent.tinker:tinker-android-anno:1.9.1')
    }
1. implementation和api: 编译依赖包并将依赖包中的类打包进apk。
2. provided: 只提供编译支持，但打包时依赖包中的类不会写入apk。
    1. 依赖包只在编译期起作用。（如：tinker的tinker-android-anno只用于在编译期生成Application，并不需要把该库中类打包进apk，这样可以减小apk包体积）
    2. 被依赖的工程中已经有了相同版本的第三方库，为了避免重复引用，可以使用provided。
### 外部命令API
    // copyApk任务：用于将app工程生成出来apk目录及文件拷贝到本地下载目录
    task('copyApk') {
        // doLast中会在gradle执行阶段执行
        doLast {
            // gradle的执行阶段去执行
            def sourcePath = this.buildDir.path + '/outputs/apk'
            def destinationPath = '/Users/xxx/Downloads'
            def command = "mv -f ${sourcePath} ${destinationPath}"
            // exec块代码基本是固定的
            exec {
                try {
                    executable 'bash'
                    args '-c', command
                    println 'the command is executed success.'
                }catch (GradleException e){
                    println 'the command is executed failed.'
                }
            }
        }
    }


