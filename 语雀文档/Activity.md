# Activity


## Activity正常生命周期![20160915144447393.png](https://cdn.nlark.com/yuque/0/2020/png/754789/1579508227008-35b5d06f-aa9d-43c2-98eb-5de8e056e343.png#align=left&display=inline&height=814&name=20160915144447393.png&originHeight=814&originWidth=1031&size=131017&status=done&style=none&width=1031)
### onCreate 
表示Activity正在被创建，这是生命周期的第一个方法，在这个方法中，我们可以做一些初始化的工作，比如调用onContentView去加载界面布局资源，初始化Activity所需数据等。
若此时调用onContentView方法，则会初始化DecorView根布局，并通过LayoutInflater解析XML布局文件中的节点View添加addView至DecorView的子布局**mContentParent**中。
### onRestart
表示Activity正在重新启动，一般情况下，当当前Activity从不可见重新变为可见时，onRestart就会被调用，这种情况一般是用户行为所导致的，比如用户按home键切换到桌面或者用户打开了一个新的Activity，这时当前的Activity就会被暂停，也就是onPause和onStop方法被执行了，接着用户又回到了这个Activity，就会出现这种情况。
### onStart
表示Activity正在被启动，即将开始，这个时候Activity已经可见了，但是还没有出现在前台，还无法和用户交互，这个时候我们可以理解为Activity已经启动了，但是我们还没有看见。
### onResume
表示Activity已经可见了，并且出现在前台，并开始活动了，要注意这个和onStart的对比，这两个都表示Activity已经可见了，但是onStart的时候Activity还处于后台，onResume的时候Activity才显示到前台。Activity的每个生命周期都是ActivityThread中mH的消息处理所执行，也是在而此生命周期则是来源于ActivityThread中的handleResumeActivity方法。而WindowManager和DecorView的关联也是在handleResumeActivity方法中处理的--wm.addView(decor, 1)，显示DecorView r.activity.makeVisible()和View绘制// 执行ViewRootImpl的setView--View绘制的起点
                ;root.setView(view, wparams, panelParentView)均在此阶段执行，root.setView中通过调用requestLayout()方法触发View的mesure、layout和draw等方法。
### onPause
表示Activity正在停止，正常情况下，紧接着onStop就会被调用，在特殊情况下，如果这个时候再快速的回到当前Activity，那么onResume就会被调用。这个情况比较极端，用户操作很难重现这个场景，此时可以做一些数据存储，停止动画等工作，但是注意不要太耗时了，因为这样会影响到新的Activity的显示，onPause必须先执行完，新Activity的onResume才会执行。
### onStop
表示Activity即将停止，同样可以做一些轻量级的资源回收，但是不要太耗时了。
### onDestroy
表示Activity即将被销毁，这是Activity生命周期的最后一个回调，在这里我们可以做一些最后的回收工作和资源释放。

### View的onAttachedToWindow和onDetachedFromWindow的调用时机？

1. onAttachedToWindow的调用：`ActivityThread.handleResumeActivity`的过程中，会将Activity的DecorView添加到`WindowManager`中，而`WindowManager`实际上只是个继承了`ViewManager`的接口。当在`ActivityThread.handleResumeActivity()`方法中调用`WindowManager.addView()`方法时，最终是调去了WindowManagerGlobal.addView()，其中`root.setView(view, wparams, panelParentView);`，正是这行代码将调用流程转移到了`ViewRootImpl.setView()`里面，继而触发了`ViewRootImpl.performTraversals()`方法，开启了View从无到有要经历的3个阶段(measure, layout, draw)。而正是`performTraversals()`方法中host.dispatchAttachedToWindow(mAttachInfo, 0);开启了调用onAttachedToWindow的时机。host实际上是DecorView，dispatch方法将这个调用沿着View tree分发了下去，ViewGroup先是调用自己的`onAttachedToWindow()`方法，再调用其每个child的`onAttachedToWindow()`方法，这样此方法就在整个view树中遍布开了，visibility并不会对这个方法产生影响。
1. onDetachedFromWindow的调用：和attched对应的，detached的发生是从Activity的销毁开始的。
```
ActivityThread.handleDestroyActivity() --> WindowManager.removeViewImmediate() 
--> WindowManagerGlobal.removeViewLocked()方法 
--> ViewRootImpl.die() --> doDie() --> ViewRootImpl.dispatchDetachedFromWindow()
```
最终会调用到View层次结构的dispatchDetachedFromWindow方法，和attached类似会沿着View Tree遍历调用所有子View的onDetachedFromWindow方法。
### 两个问题：

- onStart和onResume，onPause和onStop从描述上都差不多，对我们来说有什么实质性的不同呢？
- 假设当前Activity为A，如果用户打开了一个新的Activity为B，那么B的onResume和A的onPause谁先执行？
```
问题1：从实际使用过程来说， onStart和onResume，onPause和onStop看起来的确差不多，
甚至我们可以只保留其中的一对，比如只保留onStart和onStop，既然如此，那为什么Android系统还会提供看起来重复的接口呢？
根据上面的分析，我们知道，这两个配对的回调分别代表不同的意义，onStart和onStop是从Activity是否可见这个角度来回调的，除了这种区别，在实际的使用中，没有其他明显的区别

问题2：从源码的角度来分析以及得到解释了，关于Activity的工作原理会在本书后续章节进行讲解，
这里我们大致的了解即可，从Activity的启动过程来看，我们来看一下系统的源码，
Activity启动过程的源码相当复杂，设计到了Instrumentation,Activit和ActivityManagerService(AMS)，
这里不详细分析这一过程，简单理解，启动Activity的请求会由Instrumentation 来处理，然后它通过Binder向AMS发请求，
AMS内部维护着一个ActivityStack，并负责栈内的Activity的状态同步，AMS通过ActivityThread去同步Activity的状态
从而完成生命周期方法的调用，在ActivityStack中的resumeTopActivityLnnerLocked方法中可以了解到，
在新Activity启动之前，栈顶的Activity需要先onPause后，新的Activity才能启动，最终，
在ActvityStackSupervisor中的realStartActivityLocked方法中，最后会调用到ActivityThread的scheduleLaunchActivity方法，
而scheduleLaunchActivity方法最终会完成生命周期的调用过程，因此可以得出结论，是旧Activity县onPause，然后新的Activityy再启动。
```
### Activity启动时序图(android-26)
![Activity启动流程.jpg](https://cdn.nlark.com/yuque/0/2020/jpeg/754789/1579514980543-ab103ee5-1fac-4894-89fe-5d05fa20af1e.jpeg#align=left&display=inline&height=1622&name=Activity%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B.jpg&originHeight=1622&originWidth=2912&size=354975&status=done&style=none&width=2912)
### Activity启动大致流程(android-28)
```
1. startActivity--> Activity(startActivity-> startActivityForResult)
	--> Instrumentation(execStartActivity)--> ActivityManager(getService.startActivity)
  --> ActivityManagerService(startActivity)--> ActivityStartController(obtainStarter工厂方法模式)
  --> ActivityStarter(execute--> startActivityMayWait--> startActivity--> startActivityUnchecked)
2. --> ActivityStackSupervisor(resumeTopActivityUncheckedLocked)
	--> ActivityStack(resumeTopActivityUncheckedLocked--> resumeTopActivityInnerLocked)
  --> ActivityStartSupervisor(startSpecificActivityLocked--> realStartActivityLocked)
  --> ClientLifecycleManager(scheduleTransation)--> ClientTransation(schedule)
3. --> ActivityThread(ApplicationThread(scheduleTransation)--> scheduleTransation)
	--> ClientTransationHandler(scheduleTransation--> sendMessage(ActivityThread.H.EXECUTE_TRANSATION))
	--> ActivityThread(H(handleMessage))--> TransationExceutor(execute)
  --> LaunchActivityItem(excute)--> ClientTransationHandler(handleLaunchActivity)
4(最后使用反射创建Activity). --> ActivityThread(handleLaunchActivity--> performLaunchActivity)
	--> Instrumentation(newActivity--> getFactory(pkg))--> ActivityThread(peekPackageInfo)
  --> LoadedApk(getAppFactory)--> AppComponentFactory(instantiateActivity(cl, className, intent)
  --> (Activity) cl.loadClass(className).newInstance())--> Activity(performCreate--> onCreate)
```
## Activity异常情况生命周期
Activity除了受用户操作导致的正常生命周期的调度，同时还会存在一些异常的情况，比如当资源相关的系统配置发生改变以及系统内存不足的时候，Activity就有可能被杀死。
### 情况1：资源相关的系统配置发生改变导致Activity被杀死并重新创建
比如：旋转屏幕

![20160910132814613.png](https://cdn.nlark.com/yuque/0/2020/png/754789/1579517441886-7e83a51b-17e7-419b-842b-a776cf2484bb.png#align=left&display=inline&height=457&name=20160910132814613.png&originHeight=457&originWidth=794&size=49488&status=done&style=none&width=794)
当系统配置发生改变的时候，Activity会被销毁，其onPause，onStop,onDestroy均会被调用，同时由于Activity是异常情况下终止的，系统会调用onSaveInstanceState来保存当前Activity的状态，这个方法调用的时机是在onStop之前，他和onPause没有既定的时序关系，他即可能在onPause之前调用，也有可能在之后调用，需要强调的是，这个方法只出现在Activity被异常终止的情况下，正常情况下是不会走这个方法的吗，当我们onSaveInstanceState保存到Bundler对象作为参数传递给onRestoreInstanceState和onCreate方法，因此我们可以通过onRestoreInstanceState和onCreate方法来判断Activity是否被重建。如果被重建了，我们就取出之前的数据恢复，从时序上来说，onRestoreInstanceState的调用时机应该在onStart之后。

同时我们要知道，在onSaveInstanceState和onRestoreInstanceState方法中，系统自动为我们做了一些恢复工作，当Activity在异常情况下需要重新创建时，系统会默认我们保存当前的Activity视图架构，并且为我们恢复这些数据，比如文本框中用户输入的数据，ListView滚动的位置，这些View相关的状态系统都会默认恢复，具体针对某一个特定的View系统能为们恢复那些数据？我们可以查看View的源码，和Activity一样，每一个View都有onSaveInstanceState和onRestoreInstanceState这两个方法，看一下他们的实现，就能知道系统能够为每一个View恢复数据。

关于保存和恢复View的层次结构，系统的工作流程是这样的：首先Activity被意外终止时，Activity会调用onSaveInstanceState去保存数据，然后Activity会委托Window去保存数据，接着Window再委托上面的顶级容器去保存数据，顶级容器是一个ViewGroup，一般来说他可能是一个DecorView,最后顶层容器再去一一通知他的子元素来保存数据，这样整个数据保存过程就完成了，可以发现，这是一种典型的委托思想，上层委托下层，父容器委托子容器，去处理一件事件，这种思想在Android 中有很多的应用。

#### 旋转屏幕若不想销毁Activity
```
// AndroidManifest.xml中配置相应Activity
android:configChanges="keyboardHidden|orientation|screenSize"

// 配置发生改变：包括屏幕旋转
@Override
public void onConfigurationChanged(Configuration newConfig) {
	super.onConfigurationChanged(newConfig);
 	// 可以监听旋转，此回调代码配置改变，可在此回调中，重新刷新部分数据
}

// 监听屏幕旋转角度
mOrientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
    @Override
    public void onOrientationChanged(int orientation) {
        Log.e(DEBUG_TAG, "Orientation changed to " + orientation);
        Log.e(DEBUG_TAG, "Orientation changed  width to " + ScreenUtil.getScreenWidth());
        Log.e(DEBUG_TAG, "Orientation changed height to " + ScreenUtil.getScreenHeight());
    }
};
if (mOrientationListener.canDetectOrientation()) {
    Log.i(DEBUG_TAG, "Can detect orientation");
    mOrientationListener.enable();
} else {
    Log.i(DEBUG_TAG, "Cannot detect orientation");
    mOrientationListener.disable();
}
```
### 情况2：资源内存不足导致低优先级的Activity被杀死
Activity优先级：

- 1.前台Activity:正在和用户交互的Activity，优先级最高
- 2.可见但非前台Activity:比如对话框，导致Activity可见但是位于后台无法和用户直接交互
- 3.后台Activity:已经被暂停的Activity，比如执行了onStop，优先级最低

当系统内存不足的时候，系统就会按照上述优先级去杀死目标Activity所在的进程，并且在后续通过onSaveInstanceState和onRestoreInstanceState来存储和恢复数据，如果一个进程中没有四大组件在执行，那么这个进程将很快被系统杀死，因此，一些后台工作不适合脱离四大组建而独立运行在后台中，这样进程很容易就被杀死了，比较好的方法就是将后台工作放在Service中从而保证了进程有一定的优先级，这样就不会轻易的被杀死。
![捕获.PNG](https://cdn.nlark.com/yuque/0/2020/png/754789/1579518915060-095754c6-a6b4-4456-9760-ee30a1201d88.png#align=left&display=inline&height=586&name=%E6%8D%95%E8%8E%B7.PNG&originHeight=586&originWidth=791&size=256907&status=done&style=none&width=791)
从上面的属性中我们可以知道，如果我们没有在Activity的configChanges中设备属性的话，当系统发生改变后就会导致Activity重新被创建，上面表格中的项目很多，但是我们常用的只有locale,orientation，keyboardHidden这三个选项。
## Activity的启动模式
### Standard标准模式

这也是系统的默认模式，每次启动一个Activity都会重新创建一个实例。

ApplicationContext去启动standard模式的Activity的时候就会报错：这是因为我们的standard模式的Activity默认会进入启动它的Activity所属的任务栈中，但是由于非Activity类型的Context（如ApplicationContext）并没有所谓的任务栈，所以这就有问题了，解决这个问题，就是待启动Activity指定FLAG_ACTIVITY_TASK标记位，这样启动的时候就会为他创建一个新的任务栈，这个时候待启动Activity实际上是以singleTask模式启动的。
### SingleTop栈顶复用模式
这个模式下，如果新的Activity已经位于任务栈的栈顶，那么此Activity不会被重新创建，同时他的onNewIntent方法会被调用，通过此方法的参数我们可以取出当前请求的信息，需要注意的是，这个Activity的onCreate,onStart不会被系统调用，因为他并没有发生改变，如果新Activity已存在但不是在栈顶，那么新Activity则会重新创建。
### SingTask栈内复用模式
这是一种单实例模式，在这种模式下，只要Activity在一个栈内存在，那么多次启动此Activity都不会创建实例，和singTop一样，系统也会回调其onNewIntent方法，具体一点，当一个具有singleTask模式的Activity请求启动后，比如Activity A，系统首先会去寻找是否存在A想要的任务栈，如果不存在，就重新创建一个任务栈，然后创建A的实例把A放进栈中，如果存在A所需要的栈，这个时候就要看A是否在栈中有实例存在，如果实例存在，那么系统就会把A调到栈顶并调用它的onNewIntent方法，如果实例不存在，就创建A的实例并且把A压入栈中。若Activity A存在任务栈中，而Activity A之上还有其它Activity，则其它Activity先出栈，直到Activity A出现在栈顶。
### SingleInstance单实例模式
这是一种加强的singleTask的模式，他除了具有singleTask的所有属性之外，还加强了一点，那就是具有此模式下的Activity只能单独的处于一个任务栈中，换句话说，比如Activity A是singleInstance模式，当A启动的时候，系统会为创建创建一个新的任务栈，然后A独立在这个任务栈中，由于栈内复用的特性，后续的请求均不会创建新的Activity，除非这个独特的任务栈被系统销毁了。

问题1：我们假设目前有两个任务栈，前台任务栈的情况为AB，而后台任务栈的情况是CD，这里假设CD的启动模式都是singleTask，现在请求启动D，那么整个后台任务站都会被切换到前台，这个时候整个后退列表变成了ABCD，当用户按back键的时候，列表中的Activity会一一出栈。

问题2：
在singkleTask启动模式中，多次提到了某个Activity所需的任务栈，什么是Activity所需的任务栈尼？这要从一个参数说起：TaskAffinity,可以翻译成任务相关性，这个参数标示了一个Activity所需要的任务栈的名字默认情况下，所有的Activity所需要的任务栈的名字为应用的包名，当然，我们可以为每个Activity都单独指定TaskAffinity，这个属性值必须必须不能和包名相同，否则就相当于没有指定，TaskAffinity属性主要和singleTask启动模式或者allowTaskReparenting属性配合使用，在其他状况下没有意义，另外，任务栈分为前台任务栈和后台任务栈，后台任务栈中的Activity位于暂停状态，用户可以通过切换将后台任务栈再次调为前台。当TaskAffinity和singleTask启动模式配对使用的时候，他是具有该模式Activity目前任务栈的名字，待启动的Activity会运行在名字和TaskAffinity相同的任务栈中。
当TaskAffinity和allowTaskReparentiing结合的时候，这种情况比较复杂，会产生特殊的效果，当一个应用A启动了应用B的某一个Activity后，如果这个Activity会直接从应用A的任务栈转移到应用B的任务栈中，这还是很抽象的，再具体点，比如现在有2个应用A和B，A启动了B的一个Activity C ，然后按Home键回到桌面，然后再单击B的桌面图标，这个时候并不是启动； B的主Activity，而是重新显示了已经被应用A启动的Activity C,或者说，C从A的任务栈转移到了B的任务栈中，可以这么理解，由于A启动了C，这个时候C只能运行在A的任务栈中，但是C属于B应用，正常情况下，他的TaskAffinity值肯定不可能和A的任务栈相同（因为包名不同），所以，当B启动后，B会创建自己的任务栈，这个时候系统发现C原本所想要的任务栈已经被创建出来了，所以就把C从A的任务栈中转移过来，这种情况读者可以写一个例子测试一下，这里就不做演示了

如何给Activity指定启动模式？
第一种：通过清单文件为Activity指定：android:launchMode="singleTask"     
指定taskAffinity：android:taskAffinity="com.xxx.xxx"
第二种：通过intent的标志位为Activity指定启动模式：
Intent intent = new Intent();
intent.setClass(this,SecondActivity.class);
intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
startActivity(intent);
这两种方式都可以为Activity指定启动模式，但是二者还是有一些区别的，首先，优先级上，第二种比第一种高，当两种同时存在的时候，以第二种为准，其次，上述两种方式在限定范围内有所不同，比如，第一种方式无法直接为Activity设置FLAG_ACTIVITY_CLEAR_TOP标识，而第二种方式无法指定singleInstance模式。
### Activity的Flags

1. FLAG_ ACTIVITY_ NEW _ TASK：这个标志位的作用是为Activity指向‘singleTask’启动模式，其效果和XML中指定该模式相同。
1. FLAG_ ACTIVITY_ SINGLE _ TOP：这个标志位的作用是为Activity指向‘singleTop’启动模式，其效果和XML中指定该模式相同。
1. FLAG_ ACTIVITY_ CLEAR _ TOP：具有此标记位的Activity,当他启动时，在同一个任务栈中所有位于他上面的Activity都要出栈，这个模式一般需要和FLAG_ ACTIVITY_ NEW _ TASK配合使用，在这种情况下，被启动的Activity的实例如果已经存在，那么系统就会调用它的onNewIntent,如果被启动的Activity采用标准模式，那么他连同他之上的Activity都要出栈，系统会创建新的Activity实例并放入栈顶。
1. FLAG_ ACTIVITY_ EXCLUDE_ FROM _ RECENTS：具有此标记位的Activity，不会出现在历史Activity的列表当中，当某种情况下我们不希望用户通过历史列表回到我们的Activity的时候就使用这个标记位了，他等同于在XML中指定Activity的属性：android:excludeFromRecents="true"。
## IntentFilter的匹配规则
启动Activity分为两种，显示调用和隐式调用，二者的区别这里就不多讲了，显示调用需要明确的指定被启动对象的组件信息，包括包名和类名，而隐式意图则不需要明确指定调用信息，原则上一个intent不应该即是显式调用又是隐式调用，如果二者共存的话以显式调用为主，显式调用很简单，这里主要介绍隐式调用，隐式调用需要intent能够匹配目标组件的IntentFilter中所设置的过滤信息，如果不匹配将无法启动目标Activity，IntentFilter中的过滤信息有action、category、data。
为了匹配过滤列表，需要同时匹配过滤列表中的action,category,data信息，否则匹配失败，一个过滤列表中的action,category,data可以有多个，所有的action,category,data分别构成不同类别，同一类型的信息共同约束当前类别的匹配过程，只有一个intent同时匹配action类别,category类别,data类别才算是匹配完成，只有完全匹配才能成功启动目标Activity，另外一点，一个Activity钟可以有多个intent-filter,一个intent只要能匹配一组intent-filter即可成功启动Activity。

### action的匹配规则
action是一个字符串，系统预定了一些action,同时我们也可以在应用中定义自己的action,action的匹配规则是intent中的action必须能够和过滤规则中的action匹配，这里说的匹配是指action的字符串值完全一样，一个过滤规则中的可以有多个action,那么只要intent中的action能够和过滤规则匹配成功，针对上面的过滤规则，需要注意的是，intent如果没有指定action，那么匹配失败，总结一下，action的匹配需求就是intent中的action存在且必和过滤规则一样的action，这里需要注意的是他和category匹配规则的不同，另外，action区分大小写，大小写不同的字符串匹配也会失败。
### category的匹配规则
category是一个字符串，系统预定义了一些category，同时我们也可以在应用中定义自己的category。category的匹配规则和action不同，它要求Intent中如果含有category，那么所有的category都必须和过滤规则中的其中一个category相同。换句话说，Intent如果出现了category，不管有几个category，对于每个category来说，它必须是过滤规则中已经定义的category。当然，Intent中可以没有category，如果没有category的话，按照上面的描述，这个Intent仍然可以匹配成功。这里要注意下它和action匹配过程的不同，action
是要求Intent中必须有一个action且必须能够和过滤规则中的某个action相同，而category要求Intent可以没有category，但是如果你一旦有category，不管有几个，每个都要能和过滤规则中的任何一个category相同。为了匹配前面的过滤规则中的category，我们可以写出下面的Intent，intent.addcategory ("com.ryg.category.c")或者Intent.addcategory ("com.ryg.rcategory.d")亦或者不设category。为什么不设置category也可以匹配呢？原因是系统在调用startActivity或者startActivityForResult的时候会默认为Intent加上“android.intent.category.DEFAULT”这个category，所以这个category就可以匹配前面的过滤规则中的第三个category。同时，为了我们的activity能够接收隐式调用，就必须在intent-filter中指定“android intent categor.DEFAULT”这个category，原因刚才已经说明了。
### data匹配规则
data由两部分组成，mimeType和URI，前者是媒体类型，比如image/jpeg等，可以表示图片等，而URI包含的数据可就多了，下面的URI的结构：
<**scheme**>://<**host**>"<**port**>/[<**path**>|<**pathPrefix**>|<**pathPattern**>]
URI举例说明：
[http://www.baidu.com:80/search/info](http://www.baidu.com:80/search/info)
content://com.liuguilin.project:200/folder/subfolder/etc
