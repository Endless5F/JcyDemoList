## Gradle概念
### 什么是gradle wrapper？
gradle wrapper 就是由gradle 帮我们生成的gradlew脚本，里面包含了用到的gradle版本信息，我们编译代码的时候不直接运行gradle命令，而是运行gradlew 命令，他会自动帮我们下载对应的gradle dist，gradle wrapper被添加到代码管理系统， 这样每一个开发人员都不用去折腾gradle版本。
```
gradle命令(Linux执行需要使用 ./)
常用任务指令：
gradlew build。生成所有的输出，并执行所有的检查。
gradlew run。生成应用程序并执行某些脚本或二进制文件
gradlew check。执行所有检测类任务如tests、linting等
gradlew clean。删除build文件目录。
gradlew projects。查看项目结构。
gradlew tasks。查看任务列表。查看某个任务详细信息，可用gradle help --task someTask
gradlew dependencies。查看依赖列表。
gradlew assembleDebug（或者gradlew aD） 编译并打Debug包
gradlew assembleRelease（或者gradlew aR） 编译并打Release的包
调试类：
gradlew -?, -h, --help。查看帮助信息。
gradlew -v,--version。查看版本信息。
gradlew -s,--stacktrace。执行任务时，打印栈信息。如gradle build --s
日志类：
-q, --quiet。只打印errors类信息。
-i, --info。打印详细的信息。
性能类：
--configure-on-demand,--no-configure-on-demand。是否开启按需配置模式。
--build-cache, --no-build-cache。是否使用缓存。

其它的详见其官方文档：https://docs.gradle.org/current/userguide/command_line_interface.html
```
### Gradle执行流程
1. 初始化阶段：执行settings.gradle脚本，解析整个工程中所有Project，构建所有Project对应的project对象。
2. 配置阶段：解析所有project对象中的task对象，构建好所有task的拓扑图
3. 执行阶段：执行具体的task以及依赖的task
### Gradle生命周期
```
// setting.gradle文件
    println '初始化阶段执行完毕'

    // settings.gradle配置完后调用，只对settings.gradle设置生效
    gradle.settingsEvaluated {
        println "settings：执行settingsEvaluated..."
    }

    // 当settings.gradle中引入的所有project都被创建好后调用，只在该文件设置才会生效
    gradle.projectsLoaded {
        println "settings：执行projectsLoaded..."
    }

    // 在每个project进行配置前调用，child project必须在root project中设置才会生效，root project必须在settings.gradle中设置才会生效
    gradle.beforeProject { proj ->
        println "settings：执行${proj.name} beforeProject"
    }

    // 在每个project配置后调用
    gradle.afterProject { proj ->
        println "settings：执行${proj.name} afterProject"
    }

    // 所有project配置完成后调用
    gradle.projectsEvaluated {
        println "settings: 执行projectsEvaluated..."
    }

    //构建开始前调用
    gradle.buildStarted {
        println "构建开始..."
    }

    //构建结束后调用
    gradle.buildFinished {
        println "构建结束..."
    }

// build.gradle文件中
/**
 * 配置本Project阶段开始前的监听回调
 */
this.beforeEvaluate {
    println '配置阶段执行之前'
}

/**
 * 配置本Project阶段完成以后的回调
 */
this.afterEvaluate {
    println '配置阶段执行完毕'
}

/**
 * gradle执行本Project完毕后的回调监听
 */
this.gradle.buildFinished {
    println '执行阶段执行完毕'
}

/**
 * 所有project配置完成后调用，可直接在setting.gradle中监听
 */
gradle.projectsEvaluated {
    gradle ->
        println "所有的project都配置完毕了，准备生成Task依赖关系"
}

/**
 * 表示本Project "task 依赖关系已经生成"
 */
gradle.taskGraph.whenReady {
    TaskExecutionGraph graph ->
        println "task 依赖关系已经生成"
}

/**
 * 每一个 Task 任务执行之前回调
 */
gradle.taskGraph.beforeTask {
    Task task ->
        println "Project[${task.project.name}]--->Task[${task.name}] 在执行之前被回调"
}

/**
 * 每一个 task 执行之后被回调
 */
gradle.taskGraph.afterTask {
    task, TaskState taskState ->
        //第二个参数表示 task 的状态，是可选的参数
        println "Project[${task.project.name}]--->Task[${task.name}] 在执行完毕,taskState[upToDate:${taskState.upToDate},skipped:${taskState.skipped},executed:${taskState.executed},didWork:${taskState.didWork}]"
}
```
* 注1：上述例子中setting.gradle和build.gradle中存在重复的Gradle生命周期
* 注2：有一些生命周期只在setting.gradle中配置有效，比如settingsEvaluated
* 注3：根据Gradle执行流程，第一步初始化setting.gradle文件，第二步配置各个project。而配置各个project的顺序是按照projectName首字母a-z的顺序执行，因此若某一生命周期在所有project的中间的位置声明，则会在声明处以及后面的project产生效应。

附一张不知名大佬的执行流程和声明周期图示：

![](https://user-gold-cdn.xitu.io/2019/12/22/16f2c72e88b88689?w=586&h=1330&f=png&s=72454)
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

## Task
### Task定义及配置
Task定义的方法很简单，创建的方式主要为两种：
    * 一种迭代声明task任务以及doLast,doFirst方法添加可执行代码；
    * 一种是通过 “<<” 快捷创建task任务，闭合执行任务代码。但不仅限于这两种。
> TaskContainer：管理所有的Task，如：增加、查找。
1. 定义（创建）Task

    ```
    // 直接通过task函数去创建
    task helloTask {
        println 'i am helloTask.'
    }

    // 通过TaskContainer去创建
    this.tasks.create(name: 'helloTask2') {
        println 'i am helloTask 2.'
    }
    ```
    * 查看所有Task命令：gradlew task
    * 执行某一Task命令：gradlew taskName
2. 配置Task

    ```
    // 给Task指定分组与描述
    task helloTask(group: 'study', description: 'task study'){ // 语法糖
        ...
    }
    task helloTask {
        group 'study' // 或者setGroup('study')
        description 'task study' // 或者setDescription('task study')
        ...
    }
    ```
    Task除了可以配置group、description外，还可以配置name、type、dependsOn、overwrite、action。
    * 注1：给Task分组之后，该task会被放到指定组中，方便归类查找。（默认被分组到other中）
    * 注2：给Task添加描述，相当于给方法添加注释。
### Task的执行详情
Gradle的执行阶段执行的都是Task，即只有Task可在执行阶段执行。
1. Task中doFirst与doLast的使用：
    ```
    // 1. task代码块内部使用
    task helloTask {
        println 'i am helloTask.'
        doFirst {
            println 'the task group is: ' + group
        }
        // doFirst、doLast可以定义多个
        doFirst {}
    }
    // 2. 外部指定doFirst（会比在闭包内部指定的doFirst先执行）
    helloTask.doFirst {
        println 'the task description is: ' + description
    }

    // 统计build执行时长
    def startBuildTime, endBuildTime
    this.afterEvaluate { Project project ->
        // 通过taskName找到指定的Task
        def preBuildTask = project.tasks.getByName('preBuild') // 执行build任务时，第一个被执行的Task
        // 在preBuildTask这个task执行前执行
        preBuildTask.doFirst {
            startBuildTime = System.currentTimeMillis()
        }
        def buildTask = project.tasks.getByName('build') // 执行build任务时，最后一个被执行的Task
        // 在buildTask这个task执行后执行
        buildTask.doLast {
            endBuildTime = System.currentTimeMillis()
            println "the build time is: ${endBuildTime - startBuildTime}"
        }
    }
    ```
2. 总结
    1. Task闭包中直接编写的代码，会在配置阶段执行。可以通过doFirst、doLast块将代码逻辑放到执行阶段中执行。
    2. doFirst、doLast可以指定多个。
    3. 外部指定的doFirst、doLast会比内部指定的先执行。
    4. doFirst、doLast可以对gradle中提供的已有的task进行扩展。
### Task的执行顺序
1. Task执行顺序指定的三种方式：
    1. dependsOn强依赖方式
    2. 通过Task输入输出指定（与第1种等效）
    3. 通过API指定执行顺序
2. Task的依赖
    ```
    task taskX {
        doLast {
            println 'taskX'
        }
    }
    task taskY {
        doLast {
            println 'taskY'
        }
    }
    // 方式一：静态依赖
    // task taskZ(dependsOn: taskY) // 依赖一个task
    task taskZ(dependsOn: [taskX, taskY]) { // 依赖多个task，需要用数组[]表示
        doLast {
            println 'taskZ'
        }
    }
    // 方式二：静态依赖
    taskZ.dependsOn(taskX, taskY)
    // 方式三：动态依赖
    task taskZ() {
        dependsOn this.tasks.findAll {
            // 依赖所有以lib开头的task
            task -> return task.name.startsWith('lib')
        }
        doLast {
            println 'taskZ'
        }
    }
    // lib开头task
    task lib1 << { println 'lib1' }
    task lib2 << { println 'lib2' }
    task lib3 << { println 'lib3' }

    注：此处 << 为快捷创建task，闭包里代码等同于在doLast闭包中执行一样，但此写法目前已被标记为deprecated
    ```
    * taskZ依赖了taskX与taskY，所以在执行taskZ时，会先执行taskX、taskY。
    * taskZ依赖了taskX与taskY，但taskX与taskY没有关系，它们的执行顺序是随机的。
3. Task的输入输出
流程：Task Inputs --> Task One ——> Task Outputs --> 通过输入输出关联Task间的关闭 --> Task Inputs --> Task Two ——> Task Outputs --> .....
    1. 流程分析：
        1. inputs和outputs是Task的属性。
        2. inputs可以是任意数据类型对象，而outputs只能是文件（或文件夹）。
        3. TaskA的outputs可以作为TaskB的inputs。
    2. 代码实战
        ```
        // 例子：将每个版本信息，保存到指定的release.xml中

        ext {
            versionCode = '1.0.0'
            versionName = '100'
            versionInfo = 'App的第1个版本，完成聊天功能'
            destFile = file('release.xml')
            if (destFile != null && !destFile.exists()) {
                destFile.createNewFile()
            }
        }

        // writeTask输入扩展属性，输出文件
        task writeTask {
            // 为task指定输入
            inputs.property('versionCode', this.versionCode)
            inputs.property('versionName', this.versionName)
            inputs.property('versionInfo', this.versionInfo)
            // 为task指定输出
            outputs.file this.destFile
            doLast {
                def data = inputs.getProperties() // 返回一个map
                File file = outputs.getFiles().getSingleFile()
                // 将map转为实体对象
                def versionMsg = new VersionMsg(data)
                def sw = new StringWriter()
                def xmlBuilder = new groovy.xml.MarkupBuilder(sw)
                if (file.text != null && file.text.size() <= 0) { // 文件中没有内容
                    // 实际上，xmlBuilder将xml数据写入到sw中
                    xmlBuilder.releases { // <releases>
                        release { // <releases>的子节点<release>
                            versionCode(versionMsg.versionCode)
                            // <release>的子节点<versionCode>1.0.0<versionCode>
                            versionName(versionMsg.versionName)
                            versionInfo(versionMsg.versionInfo)
                        }
                    }
                    // 将sw里的内容写到文件中
                    file.withWriter { writer ->
                        writer.append(sw.toString())
                    }
                } else { // 已经有其它版本信息了
                    xmlBuilder.release {
                        versionCode(versionMsg.versionCode)
                        versionName(versionMsg.versionName)
                        versionInfo(versionMsg.versionInfo)
                    }
                    def lines = file.readLines()
                    def lengths = lines.size() - 1
                    file.withWriter { writer ->
                        lines.eachWithIndex { String line, int index ->
                            if (index != lengths) {
                                writer.append(line + '\r\n')
                            } else if (index == lengths) {
                                writer.append(sw.toString() + '\r\n')
                                writer.append(line + '\r\n')
                            }
                        }
                    }
                }
            }
        }

        // readTask输入writeTask的输出文件
        task readTask {
            inputs.file destFile
            doLast {
                def file = inputs.files.singleFile
                println file.text
            }
        }

        task taskTest(dependsOn: [writeTask, readTask]) {
            doLast {
                println '任务执行完毕'
            }
        }

        class VersionMsg {
            String versionCode
            String versionName
            String versionInfo
        }
        ```
        通过执行 gradle taskTask 之后，就可以在工程目录下看到release.xml文件了。
4. Task API指定顺序
    * mustRunAfter : 强行指定在某个或某些task执行之后才执行。
    * shouldRunAfter : 与mustRunAfter一样，但不强制。
    ```
    task taskX {
        doLast {
            println 'taskX'
        }
    }
    task taskY {
        // shouldRunAfter taskX
        mustRunAfter taskX
        doLast {
            println 'taskY'
        }
    }
    task taskZ {
        mustRunAfter taskY
        doLast {
            println 'taskZ'
        }
    }
    ```
    通过执行 gradle taskY taskZ taskX 之后，可以看到终端还是按taskX、taskY、taskZ顺序执行的。
5. 挂接到构建生命周期
    1. 例子：build任务执行完成后，执行一个自定义task
        ```
        this.afterEvaluate { Project project ->
            def buildTask = project.tasks.getByName('build')
            if (buildTask == null) throw GradleException('the build task is not found')
            buildTask.doLast {
                taskZ.execute()
            }
        }
        ```
    2. 例子：Tinker将自定义的manifestTask插入到了gradle脚本中processManifest与processResources这两个任务之间
        ```
        TinkerManifestTask manifestTask = project.tasks.create("tinkerProcess${variantName}Manifest", TinkerManifestTask)
        ...
        manifestTask.mustRunAfter variantOutput.processManifest
        variantOutput.processResources.dependsOn manifestTask
        ```
6. Task类型
    1. <a href="https://docs.gradle.org/current/dsl/">Gradle DSL Version 5.1</a>
    2. <a href="https://docs.gradle.org/current/dsl/org.gradle.api.tasks.Copy.html">Copy - Gradle DSL Version 5.1</a>--> Task types

## Gradle其它模块
### Settings类
settings.gradle（对应Settings.java）决定哪些工程需要被gradle处理，占用了整个gradle生命周期的三分之一，即Initialzation初始化阶段。
### SourceSet类
Gradle有一个约定的目录结构，格式和maven的结构一样。但不同的是，gradle的目录结构是可以改的。对默认的文件位置进行修改，从而让gradle知道哪种资源要从哪些文件夹中去查找。
```
// 1. sourceSets是可以调用多次的
android {
    sourceSets {
        main {
            // 配置jni so库存放位置
            jniLibs.srcDirs = ['libs']
        }
    }
    sourceSets {
        main {
            // 根据模块配置不同的资源位置
            res.srcDirs = ['src/main/res',  // 普通资源目录
                           'src/main/res-ad',   // 广告资源目录
                           'src/main/res-player']   // 播放器相关资源目录
        }
    }
}

// 2. sourceSets一般情况下是一次性配置
android {
    sourceSets {
        main {
            jniLibs.srcDirs = ['libs']
            res.srcDirs = ['src/main/res',
                           'src/main/res-ad',
                           'src/main/res-player']
        }
    }
}

// 3. 使用编程的思想，配置sourceSets
this.android.sourceSets{
    main {
        jniLibs.srcDirs = ['libs']
        res.srcDirs = ['src/main/res',
                       'src/main/res-ad',
                       'src/main/res-player']
    }
}
```
### Gradle Plugin
#### Gradle插件（Plugin）是什么?
Gradle中的Plugin是对完成指定功能的Task封装的体现，只要工程依赖了某个Plugin，就能执行该Plugin中所有的功能，如：使用java插件，就可以打出jar包，使用Android插件，就可以生成apk、aar。

#### 自定义Plugin
1. 创建插件工程
    * 在工程目录下创建buildSrc文件夹。
    * 在buildSrc目录下，创建src文件夹、build.gradle文件。
    * 在buildSrc/src目录下，再创建main文件夹。
    * 在buildSrc/src/main目录下，再分别创建groovy、resources文件夹。
    * 在buildSrc/src/main/resources再创建一个META-INF文件夹，再在META-INF下创建一个gradle-plugins文件夹。
    * 在build.gradel文件中输入如下脚本：
        ```
        apply plugin: 'groovy'

        sourceSets {
            main {
                groovy {
                    srcDir 'src/main/groovy'
                }
                resources {
                    srcDir 'src/main/resources'
                }
            }
        }
        ```
        最后，Async一下工程，buildSrc就会被识别出来了，整体目录如图：E:\CodeProject\android\Github\JcyDemoList\SourceCodeAnalysis\src\源码分析\图示讲解\Gradle自定义Plugin.png
2. 创建插件类：
与Java一样，在groovy目录下，创建一个包，再创建一个插件类（如：com.android.gradle.GradleStudyPlugin），该插件类必须实现Plugin<Project>接口。
    > 注意：gradle插件类是.groovy文件，不是.java文件
    ```
    import org.gradle.api.Plugin
    import org.gradle.api.Project

    /**
     * 自定义Gradle插件
     */
    class GradleStudyPlugin implements Plugin<Project> {

        /**
         * 插件引入时要执行的方法
         * @param project 引入当前插件的project
         */
        @Override
        void apply(Project project) {
            println 'hello gradle study plugin. current project name is ' + project.name
        }
    }
    ```
3. 指定插件入口：
在编写完插件类的逻辑之后，需要在META-INF.gradle-plugins目录下创建一个properties文件（建议以插件类包名来命名，如：com.android.gradle.properties），在该properties中声明插件类，以此来指定插件入口。
    > 该properties文件的名字将作为当前gradle插件被app工程引用的依据。
    ```
    implementation-class=com.android.gradle.GradleStudyPlugin
    // 如果报错 Could not find implementation class 'xxx' 的话，
    // 一般是类全路径有问题，默认包不需要写包路径，修改如下即可：implementation-class=GradleStudyPlugin
    ```
4. 使用自定义插件：
打开app工程的build.gradle，应用上面的自定义gradle插件，并Async。
    ```
    apply plugin: 'com.android.application'
    apply plugin: 'com.android.gradle'

    android {
      ...
    }
    ```
    在Terminal中可以看到，在gradle的配置阶段，就输出了前面自定义插件的apply方法中的日志。
5. 创建扩展属性：
插件往往会在gradle脚本中进行参数配置，如在android{}中，可以配置compileSdkVersion等参数，其实本质上，就是在gradle脚本中使用闭包方式创建了一个javaBean，并将其传递到插件中被插件识别读取而已。

    步骤：
    1. 创建一个实体类，声明成员变量，用于接收gradle中配置的参数。（可以理解为就是javaBean，不过要注意，该文件后缀是.groovy，不是.java）
        ```
        class ReleaseInfoExtension {
            String versionCode
            String versionName
            String versionInfo
            String fileName

            ReleaseInfoExtension() {}

            @Override
            String toString() {
                return "versionCode = ${versionCode} , versionName = ${versionName} ," +
                        " versionInfo = ${versionInfo} , fileName = ${fileName}"
            }
        }
        ```
    2. 在自定义插件中，对当前project进行扩展。
        ```
        class GradleStudyPlugin implements Plugin<Project> {

            /**
             * 插件引入时要执行的方法
             * @param project 引入当前插件的project
             */
            @Override
            void apply(Project project) {
                // 这样就可以在gradle脚本中，通过releaseInfo闭包来完成ReleaseInfoExtension的初始化。
                project.extensions.create("releaseInfo", ReleaseInfoExtension)
            }
        }
        ```
    3. 打开在app工程的build.gradle，通过扩展key值命名闭包的方式，就可以配置指定参数了。
        ```
        apply plugin: 'com.android.gradle'

        releaseInfo {
            versionCode = '1.0.0'
            versionName = '100'
            versionInfo = '第一个app信息'
            fileName = 'release.xml'
        }
        ```
    4. 接收参数
        ```
        def versionCodeMsg = project.extensions.releaseInfo.versionCode
        ```
6. 创建扩展Task：
自定义插件无非就是封装一些常用Task，所以，扩展Task才是自定义插件的最重要的一部分。扩展Task也很简单，继承DefaultTask，编写TaskAction注解方法。
    ```
    // 例子：把app版本信息写入到xml文件中
    import groovy.xml.MarkupBuilder
    import org.gradle.api.DefaultTask
    import org.gradle.api.tasks.TaskAction

    class ReleaseInfoTask extends DefaultTask {

        ReleaseInfoTask() {
            group 'android' // 指定分组
            description 'update the release info' // 添加说明信息
        }

        /**
         * 使用TaskAction注解，可以让方法在gradle的执行阶段去执行。
         * doFirst其实就是在外部为@TaskAction的最前面添加执行逻辑。
         * 而doLast则是在外部为@TaskAction的最后面添加执行逻辑。
         */
        @TaskAction
        void doAction() {
            updateInfo()
        }

        private void updateInfo() {
            // 获取gradle脚本中配置的参数
            def versionCodeMsg = project.extensions.releaseInfo.versionCode
            def versionNameMsg = project.extensions.releaseInfo.versionName
            def versionInfoMsg = project.extensions.releaseInfo.versionInfo
            def fileName = project.extensions.releaseInfo.fileName
            // 创建xml文件
            def file = project.file(fileName)
            if (file != null && !file.exists()) {
                file.createNewFile()
            }
            // 创建写入xml数据所需要的类。
            def sw = new StringWriter();
            def xmlBuilder = new groovy.xml.MarkupBuilder(sw)
            // 若xml文件中没有内容，就多创建一个realease节点，并写入xml数据
            if (file.text != null && file.text.size() <= 0) {
                xmlBuilder.releases {
                    release {
                        versionCode(versionCodeMsg)
                        versionName(versionNameMsg)
                        versionInfo(versionInfoMsg)
                    }
                }
                file.withWriter { writer ->
                    writer.append(sw.toString())
                }
            } else { // 若xml文件中已经有内容，则在原来的内容上追加。
                xmlBuilder.release {
                    versionCode(versionCodeMsg)
                    versionName(versionNameMsg)
                    versionInfo(versionInfoMsg)
                }
                def lines = file.readLines()
                def lengths = lines.size() - 1
                file.withWriter { writer ->
                    lines.eachWithIndex { String line, int index ->
                        if (index != lengths) {
                            writer.append(line + '\r\n')
                        } else if (index == lengths) {
                            writer.append(sw.toString() + '\r\n')
                            writer.append(line + '\r\n')
                        }
                    }
                }
            }
        }
    }
    ```
    与创建扩展属性一样，扩展Task也需要在project中创建注入。
    ```
    /**
     * 自定义Gradle插件
     */
    class GradleStudyPlugin implements Plugin<Project> {

        /**
         * 插件引入时要执行的方法
         * @param project 引入当前插件的project
         */
        @Override
        void apply(Project project) {
            // 创建扩展属性
            // 这样就可以在gradle脚本中，通过releaseInfo闭包来完成ReleaseInfoExtension的初始化。
            project.extensions.create("releaseInfo", ReleaseInfoExtension)
            // 创建Task
            project.tasks.create("updateReleaseInfo", ReleaseInfoTask)
        }
    }
    ```
    再次Async工程之后，就可以在Idea的gradle标签里android分组中看到自定义好的Task了。

    注：这种在工程下直接创建buildSrc目录编写的插件，只能对当前工程可见，所以，如果需要将我们自定义好的grdle插件被其他工程所使用，则需要单独创建一个库工程，并创建如buildSrc目录下所有的文件，最后上传maven仓库即可
#### android插件对gradle扩展
1. <a href="https://avatarqing.github.io/Gradle-Plugin-User-Guide-Chinese-Verision/">译者序 | Gradle Android插件用户指南翻译</a>
2. <a href="https://avatarqing.github.io/Gradle-Plugin-User-Guide-Chinese-Verision/advanced_build_customization/manipulation_taskstask.html">Manipulation tasks（操作task） | Gradle Android插件用户指南翻译</a>
3. 自定义Apk输出位置：
    ```
    this.afterEvaluate {
        this.android.applicationVariants.all { variant ->
            def output = variant.outpus.first() // 获取变体输出文件（outputs返回是一个集合，但只有一个元素，即输出apk的file）
            def apkName = "app-${variant.baseName}-${variant.versionName}.apk"
            output.outputFile = new File(output.outputFile.parent, apkName)
        }
    }
    ```
## Jenkins
 Jenkins是一个开源的、提供友好操作界面的持续集成(CI)工具，起源于Hudson（Hudson是商用的），主要用于持续、自动的构建/测试软件项目、监控外部任务的运行（这个比较抽象，暂且写上，不做解释）。Jenkins用Java语言编写，可在Tomcat等流行的servlet容器中运行，也可独立运行。通常与版本管理工具(SCM)、构建工具结合使用。常用的版本控制工具有SVN、GIT，构建工具有Maven、Ant、Gradle。

具体学习请参考：<a href="https://www.jianshu.com/p/5f671aca2b5a">Jenkins详细教程</a>
## 参考链接

https://www.jianshu.com/p/498ae3fabe6f

https://www.jianshu.com/u/f9de259236a3

...

<font color="#ff0000">注：若有什么地方阐述有误，敬请指正。**期待您的点赞哦！！！**</font>