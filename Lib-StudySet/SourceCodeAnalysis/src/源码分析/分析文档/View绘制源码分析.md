源码分析第三篇，Activity启动后的View绘制流程的源码分析，由于View绘制牵扯内容太多，因此此篇文章略长，也许许多小伙伴看到部分就会失去耐心，但我相信若能全部看完，或多或少也会有点感悟，若能看完整有那些不足和有误的地方，请留言告知，感谢！！！
<font color=#ff0000>(注：若有什么地方阐述有误，敬请指正。)</font>
## View绘制基础概念
说到View绘制首先要先了解几个概念：

* Window：是一个抽象类，具有窗口管理的功能，实现类为PhoneWindow。Window有3类，应用层Window、子Window、系统Window。应用层Window对应的比如说Activity，而子Window必须附着在父Window上，如Dialog、PopupWindow。系统Window有如Toast、System Alert等。其层级对应区间如下：

        应用层Window： 1 - 99
        子Window： 1000 - 1999
        系统Window： 2000 - 2999

    毫无疑问，层级越高的显示的越靠上。

* PhoneWindow类：PhoneWindow这个类是Framework为我们提供的Android窗口的具体实现。我们平时调用setContentView()方法设置Activity的用户界面时，实际上就完成了对所关联的PhoneWindow的
ViewTree(窗口所承载的控件树)的设置。我们还可以通过Activity类的requestWindowFeature()方法来定制Activity关联PhoneWindow的外观，这个方法实际上做的是把我们所请求的窗口外观特性存储到了PhoneWindow的mFeatures成员中，在窗口绘制阶段生成外观模板时，会根据mFeatures的值绘制特定外观。该类继承于Window类，是Window类的具体实现，即我们可以通过该类具体去绘制窗口。并且，该类内部引用一个DecorView对象，该DectorView对象是所有应用窗口(Activity界面)的根View。简而言之，PhoneWindow类是把一个FrameLayout类即DecorView对象进行一定的包装，将它作为应用窗口的根View，并提供一组通用的窗口操作接口。它是Android中的最基本的窗口系统，每个Activity均会创建一个PhoneWindow对象，是Activity和整个View系统交互的接口。

* DecorView类：是一个应用窗口的根容器，它本质上是一个FrameLayout。DecorView有唯一一个子View，它是一个垂直LinearLayout，包含两个子元素，一个是TitleView（ActionBar的容器），另一个是ContentView（窗口内容的容器）。关于ContentView，它是一个FrameLayout（android.R.id.content)，我们平常用的setContentView就是设置它的子View。

* ViewRootImpl类：ViewRootImpl是实际管理Window中所以View的类,每个Activity中ViewRootImpl数量取决于调用mWindowManager.addView的调用次数。

<font color="#ff0000">注：Activity是由中心控制器ActivityManagerService来管理控制的。和Activity类似，UI层的内容是由另一个控制器WindowManagerService（WMS）来管理的。</font>

## setContentView初始化布局
View的绘制要从Activity创建后，执行setContentView方法开始分析：
```
import android.app.Activity;
import android.os.Bundle;
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
```
这里的MainActivity继承自Activity类，Activity类是AppCompatActivity、FragmentActivity等
Activity的父类，因此直接继承自Activity更便于分析。

1. Activity类：
```
    public void setContentView(@LayoutRes int layoutResID) {
        // getWindow()获取的是Window
        getWindow().setContentView(layoutResID);
        initWindowDecorActionBar();
    }
    
    public Window getWindow() {
        return mWindow;
    }
```
getWindow()中直接返回mWindow，那么mWindow是什么时候被赋值的呢，这就要说到Activity启动过程啦，Activity启动最后会调用ActivityThread类中的handleLaunchActivity方法(若不清楚Activity启动流程的，请参考我上一篇文章)，而handleLaunchActivity方法里又会调用performLaunchActivity方法创建并返回一个Activity，看下performLaunchActivity方法里部分代码：
 ```
    private void handleLaunchActivity(ActivityClientRecord r, Intent customIntent) {
        handleConfigurationChanged(null, null);
        //初始化 WindowManagerService，主要是获取到 WindowManagerService 代理对象
        WindowManagerGlobal.initialize();
        Activity a = performLaunchActivity(r, customIntent);

        if (a != null) {
            r.createdConfig = new Configuration(mConfiguration);
            // 回调onResume()
            handleResumeActivity(r.token, false, r.isForward,
                !r.activity.mFinished && !r.startsNotResumed);
            ...
        }
        ...
    }
 
    private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
        ...
        ContextImpl appContext = createBaseContextForActivity(r);
        Activity activity = null;
        try {
            java.lang.ClassLoader cl = appContext.getClassLoader();
            // 创建Activity
            activity = mInstrumentation.newActivity(
                    cl, component.getClassName(), r.intent);
            StrictMode.incrementExpectedActivityCount(activity.getClass());
            r.intent.setExtrasClassLoader(cl);
            r.intent.prepareToEnterProcess();
            if (r.state != null) {
                r.state.setClassLoader(cl);
            }
        } catch (Exception e) {
            if (!mInstrumentation.onException(activity, e)) {
                throw new RuntimeException(
                    "Unable to instantiate activity " + component+ ": " + e.toString(), e);
            }
        }

        try {
            // 创建 Application 对象
            Application app = r.packageInfo.makeApplication(false, mInstrumentation);
            ...
            if (activity != null) {
                ...
                // 回调Activity的attach方法
                activity.attach(appContext, this, getInstrumentation(), r.token,
                        r.ident, app, r.intent, r.activityInfo, title, r.parent,
                        r.embeddedID, r.lastNonConfigurationInstances, config,
                        r.referrer, r.voiceInteractor, window, r.configCallback);
                ...
                // 回调Activity的onCreate()方法
                if (r.isPersistable()) {
                    mInstrumentation.callActivityOnCreate(activity, 
                        r.state, r.persistentState);
                } else {
                    mInstrumentation.callActivityOnCreate(activity, r.state);
                }
            ...
            }
        } catch (SuperNotCalledException e) {
            throw e;
        ...
        return activity;
    }
 ```
 从代码中可以看出在performLaunchActivity方法里不仅创建了Activity还调用了attahc方法，而
 mWindow就是在attahc方法中赋值的。
 ```
 // Activity类的attach方法：
 final void attach(Context context, ActivityThread aThread,
            Instrumentation instr, IBinder token, int ident,
            Application application, Intent intent, ActivityInfo info,
            CharSequence title, Activity parent, String id,
            NonConfigurationInstances lastNonConfigurationInstances,
            Configuration config, String referrer, IVoiceInteractor voiceInteractor,
            Window window, ActivityConfigCallback activityConfigCallback) {
        attachBaseContext(context);

        mFragments.attachHost(null /*parent*/);
        // mWindow赋值 PhoneWindow继承自Window对象，是Window类的具体实现
        mWindow = new PhoneWindow(this, window, activityConfigCallback);
        mWindow.setWindowControllerCallback(this);
        mWindow.setCallback(this);
        mWindow.setOnWindowDismissedCallback(this);
        mWindow.getLayoutInflater().setPrivateFactory(this);
        ...
        // 设置WindowManagerImpl对象
        mWindow.setWindowManager(
                (WindowManager)context.getSystemService(Context.WINDOW_SERVICE),
                mToken, mComponent.flattenToString(),
                (info.flags & ActivityInfo.FLAG_HARDWARE_ACCELERATED) != 0);
        if (mParent != null) {
            mWindow.setContainer(mParent.getWindow());
        }
        // 获取WindowManagerImpl对象
        mWindowManager = mWindow.getWindowManager();
        mCurrentConfig = config;
        mWindow.setColorMode(info.colorMode);
    }
 ```
 回到setContentView方法，在该方法中getWindow()也调用了setContentView方法，而getWindow()返回的真实类则是PhoneWindow，所以此时调用的是PhoneWindow类中的setContentView方法。
 
 2. PhoneWindow类setContentView方法：
```
/**
 * 什么是Transition?
 *  安卓5.0中Activity和Fragment变换是建立在名叫Transitions的安卓新特性之上的。
 *  这个诞生于4.4的transition框架为在不同的UI状态之间产生动画效果提供了非常方便的API。
 *  该框架主要基于两个概念：场景（scenes）和变换（transitions）。
 *  场景（scenes）定义了当前的UI状态，
 *  变换（transitions）则定义了在不同场景之间动画变化的过程。
*/

    @Override
    public void setContentView(int layoutResID) {
        // contentParent是mDecor(DecorView)两部分中的ContentView部分
        if (mContentParent == null) {
            // 初始化DecoView
            installDecor();
        } else if (!hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            mContentParent.removeAllViews();
        }
        // 是否有特征，需要动态设置，默认没有(动画效果)
        if (hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            // 本文不是重点先不分析
            final Scene newScene = Scene.getSceneForLayout(mContentParent, layoutResID,
                    getContext());
            transitionTo(newScene);
        } else {
            // 一般会走这里
            mLayoutInflater.inflate(layoutResID, mContentParent);
        }
        mContentParent.requestApplyInsets();
        final Callback cb = getCallback();
        if (cb != null && !isDestroyed()) {
            cb.onContentChanged();
        }
        mContentParentExplicitlySet = true;
    }
    
    private void installDecor() {
        mForceDecorInstall = false;
        if (mDecor == null) {
            // 生成mDecor
            mDecor = generateDecor(-1);
            ...
        } else {
            mDecor.setWindow(this);
        }
        if (mContentParent == null) {
            mContentParent = generateLayout(mDecor);
            ...
        }
    }

    protected DecorView generateDecor(int featureId) {
        Context context;
        if (mUseDecorContext) {
            Context applicationContext = getContext().getApplicationContext();
            if (applicationContext == null) {
                context = getContext();
            } else {
                context = new DecorContext(applicationContext, getContext().getResources());
                if (mTheme != -1) {
                    context.setTheme(mTheme);
                }
            }
        } else {
            context = getContext();
        }
        // new DecorView
        return new DecorView(context, featureId, this, getAttributes());
    }

    protected ViewGroup generateLayout(DecorView decor) {
        ...
        // android.R.id.content
        // 系统内部定义的布局，contentParent指的是DecorView的ContentView部分
        ViewGroup contentParent = (ViewGroup)findViewById(ID_ANDROID_CONTENT);
    
        int layoutResource;
        ...

        mDecor.startChanging();
        // 根据不同的Feature设置不同的布局文件
        mDecor.onResourcesLoaded(mLayoutInflater, layoutResource);
        ...
        return contentParent;
    }
```
hasFeature(FEATURE_CONTENT_TRANSITIONS)是用来判断是否来启用Transition Api，Transition是什么代码上方有所说明。想具体了解Transition以及用法，点击下方链接。

http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2015/0113/2310.html

先初始化了DecoView(根布局)，然后会调用mLayoutInflater.inflate()方法来填充布局，inflate方法会使用Xml解析器，解析我们传入的xml文件，并保存到mContentParent里。Xml解析的具体源码就不分析啦，感兴趣的小伙伴自行查看吧。到这里，setContentView()的整体执行流程我们就分析完了，至此我们已经完成了Activity的ContentView的创建与设置工作。

## onResume界面可见绘制之关联Window和ViewRootImpl
3. View的绘制和Activity的启动息息相关，此时Activity已经走完回调onCreate，而在Activity流程中已经走完performLaunchActivity方法，继续往下走则会走入handleResumeActivity方法中然后调用
performResumeActivity
```
// ActivityThread类：
    final void handleResumeActivity(IBinder token,boolean clearHide
        , boolean isForward, boolean reallyResume, int seq, String reason) {
        ...
        // 回调onStart和onResume方法
        r = performResumeActivity(token, clearHide, reason);

        if (r != null) {  
            final Activity a = r.activity;  
            boolean willBeVisible = !a.mStartedActivity;  
            ...  
            if (r.window == null && !a.mFinished && willBeVisible) {  
                r.window = r.activity.getWindow();  
                View decor = r.window.getDecorView();  
                decor.setVisibility(View.INVISIBLE);  
                ViewManager wm = a.getWindowManager();  
                WindowManager.LayoutParams l = r.window.getAttributes();  
                a.mDecor = decor;  
                l.type = WindowManager.LayoutParams.TYPE_BASE_APPLICATION;  
                l.softInputMode |= forwardBit;  
                if (a.mVisibleFromClient) {  
                    // 1.主要此变量，下面要用
                    a.mWindowAdded = true;  
                    // 2.Window和DecorView关联
                    wm.addView(decor, l);  
                }  
            ...  
            if (!r.activity.mFinished && willBeVisible  
                && r.activity.mDecor != null && !r.hideForNow) {  
                ...  
                mNumVisibleActivities++;  
                if (r.activity.mVisibleFromClient) {  
                    // 3.显示DecorView
                    r.activity.makeVisible();   
                }  
            }  
            ... 
        }
    }
```
WindowManager是个接口，它的实现类是WindowManagerImpl类，而WindowManagerImpl又把相关逻辑交给了WindowManagerGlobal处理。WindowManagerGlobal是个单例类，它在进程中只存在一个实例，是它内部的addView方法最终创建了我们的核心类ViewRootImpl。先看上面代码1处，设置当前Activity成员变量mWindowAdded为true表明Window已经添加过啦，2处代码和3处代码：
```
// WindowManagerImpl类：
    @Override
    public void addView(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
        applyDefaultToken(params);
        mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
    }
// WindowManagerGlobal类：
    // 2处
    public void addView(View view, ViewGroup.LayoutParams params,
            Display display, Window parentWindow) {
        ...
        ViewRootImpl root;
        View panelParentView = null;

        synchronized (mLock) {
            ...
            // new出实际管理Window中所以View的类ViewRootImpl
            root = new ViewRootImpl(view.getContext(), display);
            view.setLayoutParams(wparams);
            mViews.add(view);
            // mRoots为ViewRootImpl的集合，即执行过多少次addView就有多少ViewRootImpl
            mRoots.add(root);
            mParams.add(wparams);
            try {
                // 执行ViewRootImpl的setView--View绘制的起点
                root.setView(view, wparams, panelParentView);
            } catch (RuntimeException e) {
                // BadTokenException or InvalidDisplayException, clean up.
                if (index >= 0) {
                    removeViewLocked(index, true);
                }
                throw e;
            }
        }
    }
// Activity类：
    // 3处
    void makeVisible() {
        // 第1处代码，mWindowAdded已为true
        if (!mWindowAdded) {
            ViewManager wm = getWindowManager();
            wm.addView(mDecor, getWindow().getAttributes());
            mWindowAdded = true;
        }
        // mDecor设置为显示
        mDecor.setVisibility(View.VISIBLE);
    }
```
这个过程创建一个 ViewRootImpl，并将之前创建的 DecoView 作为参数传入，以后 DecoView 的事件都由 ViewRootImpl 来管理了，比如，DecoView 上添加 View，删除 View。ViewRootImpl 实现了 ViewParent 这个接口，这个接口最常见的一个方法是 requestLayout()。
```
// ViewRootImpl类：
    public ViewRootImpl(Context context, Display display) {
        ...
        // 从WindowManagerGlobal中获取一个IWindowSession的实例。它是ViewRootImpl和WMS进行通信的代理
        mWindowSession = WindowManagerGlobal.getWindowSession();
        ...
        mWindow = new W(this);//创建了一个W本地Binder对象，作用为将WMS的事件通知到应用程序进程
        ...
        mChoreographer = Choreographer.getInstance();//Choreographer对象,用于统一调度窗口绘图
        ...
    }
    
// WindowManagerGlobal类：
    public static IWindowSession getWindowSession() {
        synchronized (WindowManagerGlobal.class) {
            if (sWindowSession == null) {
                try {
                    InputMethodManager imm = InputMethodManager.getInstance();
                    // 获取WindowManagerService的Binder代理 windowManager
                    IWindowManager windowManager = getWindowManagerService();
                    // 通过Binder代理 windowManager调用openSession函数
                    // 获取实例sWindowSession：表示活动的客户端会话。每个进程通常有一个 Session对象与windowManager交互。
                    // 通过openSession函数来与WMS建立一个通信会话，后面继续细说
                    sWindowSession = windowManager.openSession(
                            new IWindowSessionCallback.Stub() {
                                @Override
                                public void onAnimatorScaleChanged(float scale) {
                                    ValueAnimator.setDurationScale(scale);
                                }
                            },
                            imm.getClient(), imm.getInputContext());
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            return sWindowSession;
        }
    }
```
该段代码中引入两个概念：mWindowSession和mWindow，分别是IWindowSession和IWindow，具体作用下面细讲。

## onResume界面可见绘制之IWindowSession和IWindow
4. 关联完Window和ViewRootImpl后，ViewRootImpl立马执行了setView，开始了View绘制的征程。
```
// ViewRootImpl类：
    public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
        synchronized (this) {
            if (mView == null) {
                mView = view;
                ...onResume界面可见绘制之IWindowSession和IWindow
                requestLayout();
                ...
                try {
                    mOrigWindowType = mWindowAttributes.type;
                    mAttachInfo.mRecomputeGlobalAttributes = true;
                    collectViewAttributes();
                    // 调用IWindowSession的addToDisplay方法，第一个参数是IWindow
                    res = mWindowSession.addToDisplay(mWindow, mSeq, mWindowAttributes,
                            getHostVisibility(), mDisplay.getDisplayId(),
                            mAttachInfo.mContentInsets, mAttachInfo.mStableInsets,
                            mAttachInfo.mOutsets, mInputChannel);
                } catch (RemoteException e) {
                    mAdded = false;
                    mView = null;
                    mAttachInfo.mRootView = null;
                    mInputChannel = null;
                    mFallbackEventHandler.setView(null);
                    unscheduleTraversals();
                    setAccessibilityFocus(null, null);
                    throw new RuntimeException("Adding window failed", e);
                } finally {
                    if (restore) {
                        attrs.restore();
                    }
                }
            }
        }
    }
```
ViewRootImpl类的setView方法主要做了3个事：
* 保存传入的view参数为mView，这个mView只向PhoneWindow的DecorView
* 执行了开始绘制的方法requestLayout();
* 调用IWindowSession的addToDisplay函数，这是一个跨进程的Binder通信，第一个参数事mWindow，它事W类型，从IWindow.Stub派生的。

从上面代码可发现，ViewRoot和远端进程SystemServer的WMS是有交互的，总结一下交互流程：
* ViewRootImpl初始化时WindowManagerGlobal调用getWindowSession，经IWindowManager调用openSession，得到IWindowSession对象。
* setView方法中，调用IWindowSession的addToDisplay函数，把一个IWindow对象作为参数传入。

看一下openSession方法：
```
// WindowManagerService类：
    @Override
    public IWindowSession openSession(IWindowSessionCallback callback, IInputMethodClient client,
            IInputContext inputContext) {
        ...
        // 返回一个Session对象，它支持Binder通信，并且属于Bn端。
        // Bn意味着Binder Native 端，Bp是Binder Proxy端
        // 这两端会实现相同的接口，但Proxy端只是通过Binder ipc发送一个Binder Transaction，
        // native端是真正做事情，再将结果返回。
        Session session = new Session(this, callback, client, inputContext);
        return session;
    }
    
// Session类：
    @Override
    public int addToDisplay(IWindow window, int seq, WindowManager.LayoutParams attrs,
            int viewVisibility, int displayId, Rect outContentInsets, Rect outStableInsets,
            Rect outOutsets, InputChannel outInputChannel) {
        // 调用WMS的addWindow方法
        return mService.addWindow(this, window, seq, attrs, viewVisibility, displayId,
                outContentInsets, outStableInsets, outOutsets, outInputChannel);
    }
    
// WindowManagerService类：
    public int addWindow(Session session, IWindow client, int seq,
            WindowManager.LayoutParams attrs, int viewVisibility, int displayId,
            Rect outContentInsets, Rect outStableInsets, Rect outOutsets,
            InputChannel outInputChannel) {
        ......
        synchronized(mWindowMap) {
            ...
            // 创建WindowToken
            WindowToken token = displayContent.getWindowToken(
                    hasParent ? parentWindow.mAttrs.token : attrs.token);
            ...
            // 调用attach方法
            win.attach();
        }    
        ...
        return res;
    }
    
// WindowToken类：
    void attach() {
        if (localLOGV) Slog.v(TAG, "Attaching " + this + " token=" + mToken);
        mSession.windowAddedLocked(mAttrs.packageName);
    }
    
// Session类：
    void windowAddedLocked(String packageName) {
        ...
        if (mSurfaceSession == null) {
            ...
            // 创建SurfaceSession对象
            mSurfaceSession = new SurfaceSession();
            ...
        }
        mNumWindow++;
    }
```
上面代码是按照IWindowSession有关的逻辑顺序排列的，这里又出现了一个重要对象mSurfaceSession，不过还是先讲解IWindowSession和IWindow，先来看一张ViewRootImpl和WMS关系图：
![](https://user-gold-cdn.xitu.io/2019/4/17/16a2bdeb87e1151d?w=991&h=430&f=png&s=341909)
根据这张图先来总结一下：
* ViewRootImpl通过IWindowSession和WMS进行跨进程通信，IWindowSession定义在IWindowSession.aidl文件中
* ViewRootImpl内部有一个W内部类，它也是一个基于Binder的通信类，W是IWindow的Bn端，用于请求响应。
我们来看一下W类内，都有哪些方法：
```
    static class W extends IWindow.Stub {
        private final WeakReference<ViewRootImpl> mViewAncestor;
        private final IWindowSession mWindowSession;

        W(ViewRootImpl viewAncestor) {
            mViewAncestor = new WeakReference<ViewRootImpl>(viewAncestor);
            mWindowSession = viewAncestor.mWindowSession;
        }

        @Override
        public void resized(Rect frame, Rect overscanInsets, Rect contentInsets,
                Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw,
                MergedConfiguration mergedConfiguration, Rect backDropFrame, boolean forceLayout,
                boolean alwaysConsumeNavBar, int displayId) {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchResized(frame, overscanInsets, contentInsets,
                        visibleInsets, stableInsets, outsets, reportDraw, mergedConfiguration,
                        backDropFrame, forceLayout, alwaysConsumeNavBar, displayId);
            }
        }

        @Override
        public void moved(int newX, int newY) {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchMoved(newX, newY);
            }
        }

        @Override
        public void dispatchAppVisibility(boolean visible) {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchAppVisibility(visible);
            }
        }

        @Override
        public void dispatchGetNewSurface() {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchGetNewSurface();
            }
        }

        @Override
        public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.windowFocusChanged(hasFocus, inTouchMode);
            }
        }

        private static int checkCallingPermission(String permission) {
            try {
                return ActivityManager.getService().checkPermission(
                        permission, Binder.getCallingPid(), Binder.getCallingUid());
            } catch (RemoteException e) {
                return PackageManager.PERMISSION_DENIED;
            }
        }
        ...
        /* Drag/drop */
        @Override
        public void dispatchDragEvent(DragEvent event) {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchDragEvent(event);
            }
        }

        @Override
        public void updatePointerIcon(float x, float y) {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.updatePointerIcon(x, y);
            }
        }
        ...
    }
```
可以看到W内部有一个ViewRootImpl的弱引用，从W内继承的方法可以看出，W即IWindow会通知ViewRootImpl一些事件。这里的事件指的就是按键、触屏等事件。一个按键是如何被分发的呢？其大致流程如下：
* WMS所在的SystemServer进程接收到按键事件
* WMS找到UI位于屏幕位于顶端的进程所对应的IWindow对象，这是一个Bp端对象。
* 调用这个IWindow对象的dispatchKey，IWindow对象的Bn端位于ViewRootImpl中，ViewRootImpl再根据内部View的位置信息找到真正处理这个事件的View，最后调用dispatchKey方法完成按键处理。

到此位置，应该大概能明白IWindowSession和IWindow的用处了吧，再总结一下：
* IWindowSession：用于和WMS通信，每个App进程都会和WMS建立一个IWindowSession会话用于通信。
* IWindow：用于回调WMS事件，IWindow是WMS用来进行事件通知的，每当发生一些事件时，WMS就会把这些事件告诉某个IWindow，然后IWindow再回调回ViewRootImpl中的某个View，来响应这些事件。

![](https://user-gold-cdn.xitu.io/2019/4/18/16a30ffce9513c16?w=281&h=475&f=png&s=96154)
## onResume界面可见绘制之同步屏障--VSYNC同步
还有Surface和SurfaceSession没有正式介绍呢，不过在此之前先来继续介绍requestLayout()：

```
// ViewRootImpl类：
    @Override
    public void requestLayout() {
        if (!mHandlingLayoutInLayoutRequest) {
            // 检查是否在非UI线程更新UI
            checkThread();
            mLayoutRequested = true;
            // 遍历
            scheduleTraversals();
        }
    }
    
    void checkThread() {
        if (mThread != Thread.currentThread()) {
            // 如果不是UI线程则抛出异常
            throw new CalledFromWrongThreadException(
                    "Only the original thread that created a view hierarchy can touch its views.");
        }
    }   
    
    void scheduleTraversals() {
        if (!mTraversalScheduled) {
            mTraversalScheduled = true;
            // postSyncBarrier方法，被称为同步屏障
            mTraversalBarrier = mHandler.getLooper().getQueue().postSyncBarrier();
            mChoreographer.postCallback(
                    Choreographer.CALLBACK_TRAVERSAL, mTraversalRunnable, null);
            if (!mUnbufferedInputDispatch) {
                scheduleConsumeBatchedInput();
            }
            notifyRendererOfFramePending();
            pokeDrawLockIfNeeded();
        }
    } 
    
```
MessageQueue#postSyncBarrier方法，被称为同步屏障，在这里主要为了不影响主线程UI的绘制。同步屏障可以理解为：在MessageQueue中添加一个特殊的msg，将这个msg作为一个标记，在这个标记被移除之前，当前MessageQueue队列中排在它后面的其它（非async：即同步） 的message不会被handler处理。因此此处的代码继续向下执行postCallback，我们来看看这个方法里干了些什么：
```
// Choreographer类：Choreographer就是负责获取Vsync同步信号并控制App线程(主线程)完成图像绘制的类。
    public void postCallback(int callbackType, Runnable action, Object token) {
        // 传递的参数 delayMillis 为 0
        postCallbackDelayed(callbackType, action, token, 0);
    }
    public void postCallbackDelayed(int callbackType,
            Runnable action, Object token, long delayMillis) {
        ...
        postCallbackDelayedInternal(callbackType, action, token, delayMillis);
    }
    private void postCallbackDelayedInternal(int callbackType,
            Object action, Object token, long delayMillis) {
        ...
        synchronized (mLock) {
            // 从开机到现在的毫秒数（手机睡眠的时间不包括在内）
            final long now = SystemClock.uptimeMillis();
            // 从上面方法可知，delayMillis == 0
            final long dueTime = now + delayMillis;
            mCallbackQueues[callbackType].addCallbackLocked(dueTime, action, token);

            if (dueTime <= now) { //  true
                // 执行该方法,从方法名可以看出此方法中处理跟帧相关的逻辑
                scheduleFrameLocked(now);
            } else {
                // 异步回调延迟执行
                Message msg = mHandler.obtainMessage(MSG_DO_SCHEDULE_CALLBACK, action);
                msg.arg1 = callbackType;
                msg.setAsynchronous(true);
                mHandler.sendMessageAtTime(msg, dueTime);
            }
        }
    }
    
    private void scheduleFrameLocked(long now) {
        if (!mFrameScheduled) {
            mFrameScheduled = true;
            // 是否允许动画和绘制的垂直同步，默认是为true
            if (USE_VSYNC) {
                ...
                // If running on the Looper thread, then schedule the vsync immediately,
                // otherwise post a message to schedule the vsync from the UI thread
                // as soon as possible.
                // 从上面注释中可知，如果运行在Looper线程，即主线程，View绘制走到这基本就是主线程
                if (isRunningOnLooperThreadLocked()) { // true
                    // 执行该方法
                    scheduleVsyncLocked();
                } else {
                    // 切换到主线程，调度vsync
                    Message msg = mHandler.obtainMessage(MSG_DO_SCHEDULE_VSYNC);
                    msg.setAsynchronous(true);
                    mHandler.sendMessageAtFrontOfQueue(msg);
                }
            } else {
                // 如果没有VSYNC的同步，则发送消息刷新画面
                final long nextFrameTime = Math.max(
                        mLastFrameTimeNanos / TimeUtils.NANOS_PER_MS + sFrameDelay, now);
                if (DEBUG) {
                    Log.d(TAG, "Scheduling next frame in " + (nextFrameTime - now) + " ms.");
                }
                Message msg = mHandler.obtainMessage(MSG_DO_FRAME);
                msg.setAsynchronous(true);
                mHandler.sendMessageAtTime(msg, nextFrameTime);
            }
        }
    }
    private void scheduleVsyncLocked() {
        mDisplayEventReceiver.scheduleVsync();
    }
// DisplayEventReceiver类：
    public void scheduleVsync() {
        if (mReceiverPtr == 0) {
            ...
        } else {
            nativeScheduleVsync(mReceiverPtr);
        }
    }
    
```
现在跟随源码到了mDisplayEventReceiver.scheduleVsync();方法，而mDisplayEventReceiver是什么时候初始化的呢，通过源码可以看到是在Choreographer类初始化时被new出来的，那Choreographer类是什么时候初始化的，再通过源码可以看到是ViewRootImpl类初始化时被new出来的。
```
    public ViewRootImpl(Context context, Display display) {
        ...
        mChoreographer = Choreographer.getInstance();
        ...
    }
// Choreographer类：Choreographer就是负责获取Vsync同步信号并控制App线程(主线程)完成图像绘制的类。
    //每个线程一个Choreographer实例
    private static final ThreadLocal<Choreographer> sThreadInstance =
            new ThreadLocal<Choreographer>() {
        @Override
        protected Choreographer initialValue() {
            Looper looper = Looper.myLooper();
            if (looper == null) {
                throw new IllegalStateException("The current thread must have a looper!");
            }
            return new Choreographer(looper);
        }
    };
    private Choreographer(Looper looper, int vsyncSource) {
        mLooper = looper;
        //创建handle对象，用于处理消息，其looper为当前的线程的消息队列
        mHandler = new FrameHandler(looper);
        //创建VSYNC的信号接受对象 USE_VSYNC默认为true
        mDisplayEventReceiver = USE_VSYNC ? new FrameDisplayEventReceiver(looper) : null;
        //初始化上一次frame渲染的时间点
        mLastFrameTimeNanos = Long.MIN_VALUE;
        //计算帧率，也就是一帧所需的渲染时间，getRefreshRate是刷新率，一般是60
        mFrameIntervalNanos = (long)(1000000000 / getRefreshRate());
        //创建消息处理队列
        mCallbackQueues = new CallbackQueue[CALLBACK_LAST + 1];
        for (int i = 0; i <= CALLBACK_LAST; i++) {
            mCallbackQueues[i] = new CallbackQueue();
        }
    }
// DisplayEventReceiver类：子类FrameDisplayEventReceiver
    public DisplayEventReceiver(Looper looper, int vsyncSource) {
        ...
        mMessageQueue = looper.getQueue();
        // //初始化native的消息队列
        // 接受数量多少等于looper中消息的多少
        mReceiverPtr = nativeInit(new WeakReference<DisplayEventReceiver>(this), mMessageQueue,
                vsyncSource);

        mCloseGuard.open("dispose");
    }
    
    // JNI--nativeInit方法(该方法定义在android_view_DisplayEventReceiver.cpp中)
    static jlong nativeInit(JNIEnv* env, jclass clazz, jobject receiverWeak,
            jobject messageQueueObj) {
        sp<MessageQueue> messageQueue = android_os_MessageQueue_getMessageQueue(env, messageQueueObj);
        ...
        sp<NativeDisplayEventReceiver> receiver = new NativeDisplayEventReceiver(env,
                receiverWeak, messageQueue);
        status_t status = receiver->initialize();
        ...
        receiver->incStrong(gDisplayEventReceiverClassInfo.clazz); 
        // 把c++中NativeDisplayEventReceiver对象地址返回给Java层
        // 通过这种方式将Java层的对象与Native层的对象关联在了一起。
        return reinterpret_cast<jlong>(receiver.get());
    }
```
现在让我们回到mDisplayEventReceiver.scheduleVsync();方法中：
```
// DisplayEventReceiver类：
    public void scheduleVsync() {
        // 从上面分析可知，此时mReceiverPtr保存着
        // c++中NativeDisplayEventReceiver对象的地址
        // 从名字就知道此对象为：原生显示接收器（作用：请求VSYNC的同步）
        if (mReceiverPtr == 0) {
            ...
        } else {
            // 该方法也为native方法
            // 传入NativeDisplayEventReceiver对象的地址,请求VSYNC的同步
            nativeScheduleVsync(mReceiverPtr);
        }
    }
```
VSYNC的同步：其作用主要是让显卡的运算和显示器刷新率一致以稳定输出的画面质量。VSYNC的同步具体如何用，具体如何，不是本文的重点，感兴趣的小伙伴自行搜索吧。
我们是执行mChoreographer.postCallback方法进入的JNI，因此其最终会回调到参数mTraversalRunnable(TraversalRunnable)的类方法内。想具体了解VSYNC的小伙伴不妨看看：
http://dandanlove.com/2018/04/13/android-16ms/ 
```
    final class TraversalRunnable implements Runnable {
        @Override
        public void run() {
            doTraversal();
        }
    }
    final TraversalRunnable mTraversalRunnable = new TraversalRunnable();
    
    void doTraversal() {
        if (mTraversalScheduled) {
            mTraversalScheduled = false;
            // 移除同步障碍
            mHandler.getLooper().getQueue().removeSyncBarrier(mTraversalBarrier);

            if (mProfile) {
                Debug.startMethodTracing("ViewAncestor");
            }

            performTraversals();
            ...
        }
    }

```
从源码中可以看到TraversalRunnable类内，执行了方法doTraversal();而doTraversal里先是移除了同步障碍，紧接着执行了performTraversals方法。

## 绘制流程三部曲
5. 重量级方法来啦，performTraversals方法中，执行了我们所熟知的Measure、Layout、Draw的方法：
```
    private void performTraversals() {
        final View host = mView;// 这就是DecorView
        ...
        boolean newSurface = false;
        ...
        // 27源码1901行
        relayoutResult = relayoutWindow(params, viewVisibility, insetsPending);
        ...
        ......
        if (mFirst || windowShouldResize || insetsChanged ||
                viewVisibilityChanged || params != null || mForceNextWindowRelayout) {
            if (!mStopped || mReportNextDraw) {
                boolean focusChangedDueToTouchMode = ensureTouchModeLocally(
                        (relayoutResult&WindowManagerGlobal.RELAYOUT_RES_IN_TOUCH_MODE) != 0);
                if (focusChangedDueToTouchMode || mWidth != host.getMeasuredWidth()
                        || mHeight != host.getMeasuredHeight() || contentInsetsChanged ||
                        updatedConfiguration) {
                    // 获取根View的MeasureSpec的方法
                    int childWidthMeasureSpec = getRootMeasureSpec(mWidth, lp.width);
                    int childHeightMeasureSpec = getRootMeasureSpec(mHeight, lp.height);

                    ...
                     // 27源码2167行
                    performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);

                    ...
                    int width = host.getMeasuredWidth();
                    int height = host.getMeasuredHeight();
                    boolean measureAgain = false;

                    if (lp.horizontalWeight > 0.0f) {
                        width += (int) ((mWidth - width) * lp.horizontalWeight);
                        childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
                                MeasureSpec.EXACTLY);
                        measureAgain = true;
                    }
                    if (lp.verticalWeight > 0.0f) {
                        height += (int) ((mHeight - height) * lp.verticalWeight);
                        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                                MeasureSpec.EXACTLY);
                        measureAgain = true;
                    }

                    if (measureAgain) {
                        if (DEBUG_LAYOUT) Log.v(mTag,
                                "And hey let's measure once more: width=" + width
                                + " height=" + height);
                        // 27源码2193行
                        performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
                    }

                    layoutRequested = true;
                }
            }
        }
        if (didLayout) {
            // 27源码2212行
            performLayout(lp, mWidth, mHeight);
            ...
        }
        ......
       
        // 决定是否让newSurface为true,导致后边是否让performDraw无法被调用，而是重新scheduleTraversals
        if (!hadSurface) {
            if (mSurface.isValid()) {
                // If we are creating a new surface, then we need to
                // completely redraw it.  Also, when we get to the
                // point of drawing it we will hold off and schedule
                // a new traversal instead.  This is so we can tell the
                // window manager about all of the windows being displayed
                // before actually drawing them, so it can display then
                // all at once.
                newSurface = true;
                        .....
            }
        }
        ......
        if (!cancelDraw && !newSurface) {
            // 27源码2359行
            performDraw();
        } else {
            if (isViewVisible) {
                // 再执行一次 scheduleTraversals，也就是会再执行一次performTraversals
                scheduleTraversals();
            } else if (mPendingTransitions != null && mPendingTransitions.size() > 0) {
                for (int i = 0; i < mPendingTransitions.size(); ++i) {
                    mPendingTransitions.get(i).endChangingAnimations();
                }
                mPendingTransitions.clear();
            }
        }

        mIsInTraversal = false;
    }

    private static int getRootMeasureSpec(int windowSize, int rootDimension) {
        int measureSpec;
        switch (rootDimension) {

        case ViewGroup.LayoutParams.MATCH_PARENT:
            measureSpec = MeasureSpec.makeMeasureSpec(windowSize, MeasureSpec.EXACTLY);
            break;
        case ViewGroup.LayoutParams.WRAP_CONTENT:
            // Window can resize. Set max size for root view.
            measureSpec = MeasureSpec.makeMeasureSpec(windowSize, MeasureSpec.AT_MOST);
            break;
        default:
            // Window wants to be an exact size. Force root view to be that size.
            measureSpec = MeasureSpec.makeMeasureSpec(rootDimension, MeasureSpec.EXACTLY);
            break;
        }
        return measureSpec;
    }
    
    private int relayoutWindow(WindowManager.LayoutParams params, int viewVisibility,
            boolean insetsPending) throws RemoteException {
        ...
        int relayoutResult = mWindowSession.relayout(
                mWindow, mSeq, params,
                (int) (mView.getMeasuredWidth() * appScale + 0.5f),
                (int) (mView.getMeasuredHeight() * appScale + 0.5f),
                viewVisibility, insetsPending ? WindowManagerGlobal.RELAYOUT_INSETS_PENDING : 0,
                mWinFrame, mPendingOverscanInsets, mPendingContentInsets, mPendingVisibleInsets,
                mPendingStableInsets, mPendingOutsets, mPendingBackDropFrame,
                mPendingMergedConfiguration, mSurface);// 将mSurface参数传入

        ...
        return relayoutResult;
    }

```
performTraversals()方法先调用了relayoutWindow()方法,而relayoutWindow()方法中调用了IWindowSession的relayout()方法并传入了mSurface，暂切记住这个方法，我们放到最后再说。

![](https://user-gold-cdn.xitu.io/2019/4/18/16a3105f70c38278?w=533&h=300&f=png&s=36780)
接下来需要分3个模块讲解：
## 绘制流程三部曲之Measure

这里有个重要的类MeasureSpec：在Measure流程中，系统会将View的LayoutParams根据父容器所施加的规则转换成对应的MeasureSpec,然后在onMeasure方法中根据这个MeasureSpec来确定View的测量宽高。它的高两位用来表示模式SpecMode，低30位用来表示大小SpecSize。
SpecMode共有以下三种类型：
1. UNSPECIFIED：父容器不作限制，子View想多大就多大，一般用于系统内部，如：ScrollView。
2. EXACTLY：精确模式，父容器完全决定子View的大小，当宽或高设为确定值时：即width=20dp，height=30dp，或者为match_parent。
3. AT_MOST：最大模式，大小不能大于SpecSize，也就是子View的大小有上限，对应于LayoutParams中的warp_content。看一看其部分源码：
```
    public static class MeasureSpec {
        private static final int MODE_SHIFT = 30;
        private static final int MODE_MASK  = 0x3 << MODE_SHIFT;

        /**
          * UNSPECIFIED 模式：
          * 父View不对子View有任何限制，子View需要多大就多大
          */ 
        public static final int UNSPECIFIED = 0 << MODE_SHIFT;

        /**
          * EXACTYLY 模式：
          * 父View已经测量出子Viwe所需要的精确大小，这时候View的最终大小
          * 就是SpecSize所指定的值。对应于match_parent和精确数值这两种模式
          */ 
        public static final int EXACTLY     = 1 << MODE_SHIFT;

        /**
          * AT_MOST 模式：
          * 子View的最终大小是父View指定的SpecSize值，并且子View的大小不能大于这个值，
          * 即对应wrap_content这种模式
          */ 
        public static final int AT_MOST     = 2 << MODE_SHIFT;

        //将size和mode打包成一个32位的int型数值
        //高2位表示SpecMode，测量模式，低30位表示SpecSize，某种测量模式下的规格大小
        public static int makeMeasureSpec(int size, int mode) {
            if (sUseBrokenMakeMeasureSpec) {
                return size + mode;
            } else {
                return (size & ~MODE_MASK) | (mode & MODE_MASK);
            }
        }

        //将32位的MeasureSpec解包，返回SpecMode,测量模式
        public static int getMode(int measureSpec) {
            return (measureSpec & MODE_MASK);
        }

        //将32位的MeasureSpec解包，返回SpecSize，某种测量模式下的规格大小
        public static int getSize(int measureSpec) {
            return (measureSpec & ~MODE_MASK);
        }
        //...
    }
```
可以看出，该类的思路是相当清晰的，对于每一个View，包括DecorView，都持有一个MeasureSpec，而该MeasureSpec则保存了该View的尺寸规格。在View的测量流程中，通过makeMeasureSpec来将size和mode打包成一个32位的int型数值，在其他流程通过getMode或getSize得到模式和宽高。

我们重新看回上面ViewRootImpl#getRootMeasureSpec方法的实现：根据不同的模式来设置MeasureSpec，如果是LayoutParams.MATCH_PARENT模式，则是窗口的大小，WRAP_CONTENT模式则是大小不确定，但是不能超过窗口的大小等等。对于DecorView来说，它已经是顶层view了，没有父容器，因此DecorView的
MeasureSpec使用的是屏幕窗口的大小windowSize和DecorView的LayoutParams来确认MeasureSpec的。那么到目前为止，就已经获得了一份DecorView的MeasureSpec，它代表着根View的规格、尺寸，在接下来的measure流程中，就是根据已获得的根View的MeasureSpec来逐层测量各个子View。

接下来分析测量的逻辑：
```
// ViewRootImpl类
    private void performMeasure(int childWidthMeasureSpec, int childHeightMeasureSpec) {
        if (mView == null) {
            return;
        }
        Trace.traceBegin(Trace.TRACE_TAG_VIEW, "measure");
        try {
            // 自己测量自己
            mView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_VIEW);
        }
    }
// View 类
    public final void measure(int widthMeasureSpec, int heightMeasureSpec) {
        ...
        // 回调onMeasure，在自定义View时也经常会重写此方法
        onMeasure(widthMeasureSpec, heightMeasureSpec);
        ...
    }
    // 若不重写此方法，系统会设置一个默认的大小给子View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }
    // 无论是EXACTLY还是AT_MOST，都按照测量结果进行设置。
    public static int getDefaultSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
        case MeasureSpec.UNSPECIFIED:
            result = size;
            break;
        case MeasureSpec.AT_MOST:
        case MeasureSpec.EXACTLY:
            result = specSize;
            break;
        }
        return result;
    }
    
    protected final void setMeasuredDimension(int measuredWidth, int measuredHeight) {
        boolean optical = isLayoutModeOptical(this);
        if (optical != isLayoutModeOptical(mParent)) {
            Insets insets = getOpticalInsets();
            int opticalWidth  = insets.left + insets.right;
            int opticalHeight = insets.top  + insets.bottom;

            measuredWidth  += optical ? opticalWidth  : -opticalWidth;
            measuredHeight += optical ? opticalHeight : -opticalHeight;
        }
        setMeasuredDimensionRaw(measuredWidth, measuredHeight);
    }
    private void setMeasuredDimensionRaw(int measuredWidth, int measuredHeight) {
        // 保存到成员变量mMeasuredWidth和mMeasuredHeight中
        mMeasuredWidth = measuredWidth;
        mMeasuredHeight = measuredHeight;

        mPrivateFlags |= PFLAG_MEASURED_DIMENSION_SET;
    }
```
根据源码的追踪，最终将测量的结果保存在自己的mMeasuredWidth和mMeasuredHeight成员变量中。ViewGroup的测量流程和此一致，只是其在onMeasure时需要测量子View。我们看一看FrameLayout的onMeasure：
```
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 获取子View的个数
        int count = getChildCount();
        // 判断当前布局的宽高是否是match_parent模式或者指定一个精确的大
        // 如果是则置measureMatchParent为false.
        // 因为如果是，则当前父控件则宽高大小已经确定，不受子View的限制
        final boolean measureMatchParentChildren =
                MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        // 清空需要测量match_parent宽或高的子视图的集合
        mMatchParentChildren.clear();

        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        // 遍历子View，获取修正自己的最大宽高
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (mMeasureAllChildren || child.getVisibility() != GONE) {
                // 对child进行测量，方法里child会调用自己的child.measure()方法测量自己
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                // 寻找子View中宽高的最大者，因为如果FrameLayout是wrap_content属性
                // 那么它的大小取决于子View中的最大者
                maxWidth = Math.max(maxWidth,
                        child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                maxHeight = Math.max(maxHeight,
                        child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                // 如果FrameLayout是wrap_content模式，那么往mMatchParentChildren中
                // 添加宽或者高为match_parent的子View，
                // 因为该子View的最终测量大小会影响到FrameLayout的最终测量大小影响
                if (measureMatchParentChildren) {
                    if (lp.width == LayoutParams.MATCH_PARENT ||
                            lp.height == LayoutParams.MATCH_PARENT) {
                        mMatchParentChildren.add(child);
                    }
                }
            }
        }

        // Account for padding too
        // 子View中的最大的宽高和自身的padding值都会影响到最终的大小
        maxWidth += getPaddingLeftWithForeground() + getPaddingRightWithForeground();
        maxHeight += getPaddingTopWithForeground() + getPaddingBottomWithForeground();

        // Check against our minimum height and width
        // 根据有无背景来算出最大的宽高，getSuggestedMinimumHeight/Width下面有说明
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        
        final Drawable drawable = getForeground();
        if (drawable != null) {
            maxHeight = Math.max(maxHeight, drawable.getMinimumHeight());
            maxWidth = Math.max(maxWidth, drawable.getMinimumWidth());
        }
        // 重新给自己(FrameLayout)设置测量结果（保存测量结果）
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));
        
        // 读取需要再测量的子视图数量(设置为match_parent的子View)
        count = mMatchParentChildren.size();
        // 此处判断必须大于1，若不大于1，则不会发生子View改变
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                final View child = mMatchParentChildren.get(i);
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                
                final int childWidthMeasureSpec;
                /**
                 * 如果子View的宽度是match_parent属性，那么对当前FrameLayout的MeasureSpec修改：
                 * 把widthMeasureSpec的宽度规格修改为:总宽度 - padding - margin，这样做的意思是：
                 * 对于子Viw来说，如果要match_parent，那么它可以覆盖的范围是FrameLayout的测量宽度
                 * 减去padding和margin后剩下的空间。
                 *
                 * 以下两点的结论，可以查看getChildMeasureSpec()方法：
                 *
                 * 如果子View的宽度是一个确定的值，比如50dp，那么FrameLayout的widthMeasureSpec的宽度规格修改为：
                 * SpecSize为子View的宽度，即50dp，SpecMode为EXACTLY模式
                 * 
                 * 如果子View的宽度是wrap_content属性，那么FrameLayout的widthMeasureSpec的宽度规格修改为：
                 * SpecSize为子View的宽度减去padding减去margin，SpecMode为AT_MOST模式
                */
                if (lp.width == LayoutParams.MATCH_PARENT) {
                    final int width = Math.max(0, getMeasuredWidth()
                            - getPaddingLeftWithForeground() - getPaddingRightWithForeground()
                            - lp.leftMargin - lp.rightMargin);
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                            width, MeasureSpec.EXACTLY);
                } else {
                    childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                            getPaddingLeftWithForeground() + getPaddingRightWithForeground() +
                            lp.leftMargin + lp.rightMargin,
                            lp.width);
                }
                
                final int childHeightMeasureSpec;
                // 同理对高度进行相同的处理
                if (lp.height == LayoutParams.MATCH_PARENT) {
                    final int height = Math.max(0, getMeasuredHeight()
                            - getPaddingTopWithForeground() - getPaddingBottomWithForeground()
                            - lp.topMargin - lp.bottomMargin);
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                            height, MeasureSpec.EXACTLY);
                } else {
                    childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                            getPaddingTopWithForeground() + getPaddingBottomWithForeground() +
                            lp.topMargin + lp.bottomMargin,
                            lp.height);
                }
                // 这部分的子View需要重新进行measure过程
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }
    
    // getSuggestedMinimumHeight同理
    protected int getSuggestedMinimumWidth() {
        //如果没有给View设置背景，那么就返回View本身的最小宽度mMinWidth
        //如果给View设置了背景，那么就取View本身最小宽度mMinWidth和背景的最小宽度的最大值
        return (mBackground == null) ? mMinWidth : max(mMinWidth, mBackground.getMinimumWidth());
    }
```
总结一下这段代码：首先，FrameLayout根据它的MeasureSpec来对每一个子View进行测量，即调用measureChildWithMargin方法；对于每一个测量完成的子View，会寻找其中最大的宽高，那么FrameLayout的测量宽高会受到这个子View的最大宽高的影响(wrap_content模式)，接着调用setMeasureDimension方法，把FrameLayout的测量宽高保存。最后则是特殊情况的处理，即当FrameLayout为wrap_content属性时，如果其子View是match_parent属性的话，则要重新设置FrameLayout的测量规格，然后重新对该部分View测量。我们看看它是如何测量child的，代码如下：
```
// ViewGroup类
    protected void measureChildWithMargins(View child,
            int parentWidthMeasureSpec, int widthUsed,
            int parentHeightMeasureSpec, int heightUsed) {
        // 子View的LayoutParams，你在xml的layout_width和layout_height,
        // layout_xxx的值最后都会封装到这个个LayoutParams。
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                mPaddingLeft + mPaddingRight + lp.leftMargin + lp.rightMargin
                        + widthUsed, lp.width);
        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                mPaddingTop + mPaddingBottom + lp.topMargin + lp.bottomMargin
                        + heightUsed, lp.height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }
```
由源码可知，里面调用了getChildMeasureSpec方法，把父容器的MeasureSpec以及自身的layoutParams属性传递进去来获取子View的MeasureSpec，这也印证了“子View的MeasureSpec由父容器的MeasureSpec和自身的LayoutParams共同决定”这个结论。现在我们看下这个getChildMeasureSpec的实现：
```
// ViewGroup类
    // spec为父View的MeasureSpec
    // padding为父View在相应方向的已用尺寸加上父View的padding和子View的margin
    // childDimension为子View的LayoutParams的值
    public static int getChildMeasureSpec(int spec, int padding, int childDimension) {
        int specMode = MeasureSpec.getMode(spec);
        int specSize = MeasureSpec.getSize(spec);
        // size表示子View可用空间：父容器尺寸减去padding
        int size = Math.max(0, specSize - padding);

        int resultSize = 0;
        int resultMode = 0;

        switch (specMode) {
        // Parent has imposed an exact size on us
        case MeasureSpec.EXACTLY:
            if (childDimension >= 0) {
                // 表示子View的LayoutParams指定了具体大小值（xx dp）
                resultSize = childDimension;
                resultMode = MeasureSpec.EXACTLY;
            } else if (childDimension == LayoutParams.MATCH_PARENT) {
                // 子View想和父View一样大
                resultSize = size;
                resultMode = MeasureSpec.EXACTLY;
            } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                // 子View想自己决定其尺寸，但不能比父View大
                resultSize = size;
                resultMode = MeasureSpec.AT_MOST;
            }
            break;

        case MeasureSpec.AT_MOST:
            if (childDimension >= 0) {
                // Child wants a specific size... so be it
                resultSize = childDimension;
                resultMode = MeasureSpec.EXACTLY;
            } else if (childDimension == LayoutParams.MATCH_PARENT) {
                // Child wants to be our size, but our size is not fixed.
                // Constrain child to not be bigger than us.
                resultSize = size;
                resultMode = MeasureSpec.AT_MOST;
            } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                resultSize = size;
                resultMode = MeasureSpec.AT_MOST;
            }
            break;

        // Parent asked to see how big we want to be
        case MeasureSpec.UNSPECIFIED:
            if (childDimension >= 0) {
                // Child wants a specific size... let him have it
                resultSize = childDimension;
                resultMode = MeasureSpec.EXACTLY;
            } else if (childDimension == LayoutParams.MATCH_PARENT) {
                // Child wants to be our size... find out how big it should
                // be
                resultSize = View.sUseZeroUnspecifiedMeasureSpec ? 0 : size;
                resultMode = MeasureSpec.UNSPECIFIED;
            } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                // Child wants to determine its own size.... find out how
                // big it should be
                resultSize = View.sUseZeroUnspecifiedMeasureSpec ? 0 : size;
                resultMode = MeasureSpec.UNSPECIFIED;
            }
            break;
        }
        //noinspection ResourceType
        return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
    }
```
这里根据ViewGroup的SpecMode不同，给child设置了不同的模式和大小，以保证child能正确完成测量的过程。现在我们返回measureChildWithMargins方法，接着就会执行child.measure方法，此时如果child还是ViewGroup，则依旧会走一遍上面举的Fragment的onMeasure流程，直到child为单纯的View为止。这就回到了更上面讲的View的measure方法啦。

**我们来个关于MeasureSpec的例子总结：**(纯属个人理解，若有误之处，敬请指正)

0. 先明确一个概念，现在有三种布局，分别为：DecorView、FrameLayout、若干childView
1. MeasureSpec的最初来源是DecorView，使用屏幕窗口的大小windowSize和DecorView的LayoutParams来确认firstWidthMeasureSpec和firstHeightMeasureSpec的，因此DecorView的大小一开始就是确认的
2. DecorView根布局，FrameLayout为DecorView的子View，若干childView为FrameLayout子View
3. 每个子View在measure自己前都会先根据父布局给的widthMeasureSpec和heightMeasureSpec以及padding和margin、自己的宽高来计算出适合自己的widthMeasureSpec和heightMeasureSpec
4. 若FrameLayout的宽高都为match_parent，则代表FrameLayout的大小和DecorView一样大，则DecorView和FrameLayout的widthMeasureSpec和heightMeasureSpec一样。
5. 若FrameLayout的宽高都为wrap_content或者有一样为wrap_content，则FrameLayout的大小就不一定和DecorView一样，根据源码ViewGroup的getChildMeasureSpec方法可知，wrap_content情况的布局宽高大小等于父布局宽高(DecorView)-自身padding，所以此时两者的widthMeasureSpec和heightMeasureSpec是不一样的，至少mode不一致。假设此时没有设置padding以及FrameLayout宽为match_paren高为wrap_content，因此此时FrameLayout的宽高会和屏幕大小一致，但heightMeasureSpec模式会为AT_MOST。
6. 根据第5条的分析(即同上面情况一致)，此时的FrameLayout的大小不确定，而此时需要先测量FrameLayout的子View(若干childView)，并且此时将使用size大小和屏幕大小一致但mode模式为AT_MOST的widthMeasureSpec和heightMeasureSpec传入childView中。
7. childView中根据第6步传入的widthMeasureSpec和heightMeasureSpec生成的自己的widthMeasureSpec和heightMeasureSpec，此时测量出来的各个childView的大小。
8. FrameLayout在childView测量完成后需要根据若干childView中的最大宽高和自身的padding值以及有无背景图引起的大小来设置自身最终的宽高。
9. 若在众多childView中存在宽高为match_parent的View时，则需要对这部分View重新测量，因为上一次测量是使用size大小和屏幕大小一致但mode模式不一致的widthMeasureSpec和heightMeasureSpec，而现在最终的FrameLayout的大小已经确定，因此FrameLayout的widthMeasureSpec和heightMeasureSpec已经发生改变(mode发生改变)，因此需要遍历这部分childView(这部分子View大小可能会因FrameLayout的高固定而改变自己的大小)，由于第8步已经知道了父布局的精确大小，所以此时只需要根据每个childView的允许的最大宽或高和MeasureSpec.EXACTLY形成适合每个childView的MeasureSpec值，然后重新measure这部分childView。
10. 若干childView在测量自己前也需要先结合参数中给出的widthMeasureSpec和heightMeasureSpec，以及padding、margin、自己的宽高计算出适合自己的MeasureSpec值，然后传出measure中，最后在onMeasure中进一步测量。
11. 就是由于会出现第9步的情形，因此View有时会多次onMeasure。
```
// 该布局中FrameLayout有两个TextView，都带有match_parent的属性
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="100dp"
    android:layout_height="wrap_content"
    tools:context=".MainActivity">
    
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:text="Hello World!Hello World" />
    <TextView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:text="Hello World!" />
</FrameLayout>

// TextView类：
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        ......
        if (heightMode == MeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
            height = heightSize;
            mDesiredHeightAtMeasure = -1;
        } else {
            // 获取所需高度
            int desired = getDesiredHeight();
            height = desired;
            ...
        }
        ...

        setMeasuredDimension(width, height);
    }
```
![](https://user-gold-cdn.xitu.io/2019/4/19/16a361ae9a1a2d0e?w=293&h=130&f=png&s=5733)
上述布局绘制的效果图，注：两个TextView高是一样的(不信可以自己试试)
说明：该情况符合上述说的第5步，而经过第6和第7步测量后，heightMeasureSpec的mode为AT_MOST，两个TextView的高度根据源码(感兴趣)来看是不一致的，虽然两者的measure之前的heightMeasureSpec一致，但是最终测量出来的高度不同，可将第一个TextView去掉可显示出，第二个TextView第一次测量后的高度，而FrameLayout确定高度后heightMeasureSpec的mode为EXACTLY，从代码中可看出height被赋值为FrameLayout高度-padding值，由于无padding，则和FrameLayout高度一致。

疑问：不过上述代码中为什么要判断match_parent的子View必须大于1，才重新测量子View？
将第一个TextView宽和高都设置为wrap_content，果真第二个TextView高度会变为一行高度。

![](https://user-gold-cdn.xitu.io/2019/4/18/16a3101845b4b25a?w=219&h=220&f=png&s=40127)
## 绘制流程三部曲之Layout

接下来看一看View的onLayout布局，layout的主要作用：根据子视图的大小以及布局参数将View树放到合适的位置上。此时回到ViewRootImpl类的performLayout方法：
```
// ViewRootImpl类
    private void performLayout(WindowManager.LayoutParams lp, int desiredWindowWidth,
            int desiredWindowHeight) {
        mLayoutRequested = false;
        mScrollMayChange = true;
        mInLayout = true;

        final View host = mView;
        ...
        try {
            // 先调用mView的layout方法
            host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());

            mInLayout = false;
            // 请求重新布局的集合（绘制流程一般为空）
            int numViewsRequestingLayout = mLayoutRequesters.size();
            if (numViewsRequestingLayout > 0) {
                // 获取到需要进行layout的View的个数
                // requestLayout() was called during layout.
                // If no layout-request flags are set on the requesting views, there is no problem.
                // If some requests are still pending, then we need to clear those flags and do
                // a full request/measure/layout pass to handle this situation.
                ArrayList<View> validLayoutRequesters = getValidLayoutRequesters(mLayoutRequesters,false);
                if (validLayoutRequesters != null) {
                    
                    mHandlingLayoutInLayoutRequest = true;

                    // Process fresh layout requests, then measure and layout
                    int numValidRequests = validLayoutRequesters.size();
                    for (int i = 0; i < numValidRequests; ++i) {
                        final View view = validLayoutRequesters.get(i);
                        // 调用它们的requestLayout方法，
                        view.requestLayout();
                    }
                    // 再次进行测量
                    measureHierarchy(host, lp, mView.getContext().getResources(),
                            desiredWindowWidth, desiredWindowHeight);
                    mInLayout = true;
                    // 重新layout
                    host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());

                    mHandlingLayoutInLayoutRequest = false;

                    validLayoutRequesters = getValidLayoutRequesters(mLayoutRequesters, true);
                    if (validLayoutRequesters != null) {
                        final ArrayList<View> finalRequesters = validLayoutRequesters;
                        // 再次检查是否仍有需要layout的View，如果有，就到下一帧再继续
                        getRunQueue().post(new Runnable() {
                            @Override
                            public void run() {
                                int numValidRequests = finalRequesters.size();
                                for (int i = 0; i < numValidRequests; ++i) {
                                    final View view = finalRequesters.get(i);
                                    Log.w("View", "requestLayout() improperly called by " + view +
                                            " during second layout pass: posting in next frame");
                                    view.requestLayout();
                                }
                            }
                        });
                    }
                }
            }
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_VIEW);
        }
        mInLayout = false;
    }
    
    boolean requestLayoutDuringLayout(final View view) {
        if (view.mParent == null || view.mAttachInfo == null) {
            // Would not normally trigger another layout, so just let it pass through as usual
            return true;
        }
        if (!mLayoutRequesters.contains(view)) {
            mLayoutRequesters.add(view);
        }
        ...
    }
    
// View类：
    @CallSuper
    public void requestLayout() {
        if (mMeasureCache != null) mMeasureCache.clear();

        if (mAttachInfo != null && mAttachInfo.mViewRequestingLayout == null) {
        
            ViewRootImpl viewRoot = getViewRootImpl();
            if (viewRoot != null && viewRoot.isInLayout()) {
                if (!viewRoot.requestLayoutDuringLayout(this)) {
                    return;
                }
            }
            mAttachInfo.mViewRequestingLayout = this;
        }
        ...
    }
```
performLayout方法中首先调用了View的layout方法布局控件，而后出现了一个集合mLayoutRequesters，从源码中可知mLayoutRequesters集合数据是在requestLayoutDuringLayout方法中添加的，而requestLayoutDuringLayout方法确是被View中的requestLayout方法调用。View类中的requestLayout方法和invalidate方法主要用于自定义View。
1. requestLayout方法会导致View的onMeasure、onLayout、onDraw方法被调用；
2. invalidate方法则只会导致View的onDraw方法被调用

因此，现在这种情况代码不会走到if (numViewsRequestingLayout > 0) 判断内，所以只需要看View的layout方法即可。
```
// View类：
    public void layout(int l, int t, int r, int b) {
        if ((mPrivateFlags3 & PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT) != 0) {
            onMeasure(mOldWidthMeasureSpec, mOldHeightMeasureSpec);
            mPrivateFlags3 &= ~PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT;
        }

        int oldL = mLeft;
        int oldT = mTop;
        int oldB = mBottom;
        int oldR = mRight;
        // 判断view和和其父view的布局模式情况，若两者不同步，则进行子view的size大小的修改
        // 即有两种情况会进入到该if条件：
        // 一是子view有特殊的光学边界，而父view没有，此时optical为true，
        // 另一种是父view有一个特殊的光学边界，而子view没有，此时optical为false
        boolean changed = isLayoutModeOptical(mParent) ?
                setOpticalFrame(l, t, r, b) : setFrame(l, t, r, b);

        if (changed || (mPrivateFlags & PFLAG_LAYOUT_REQUIRED) == PFLAG_LAYOUT_REQUIRED) {
            // 回调onLayout()方法
            onLayout(changed, l, t, r, b);
            ...
        }
        ...
    }
    
    public static boolean isLayoutModeOptical(Object o) {
        return o instanceof ViewGroup && ((ViewGroup) o).isLayoutModeOptical();
    }
// ViewGroup类：
    private int mLayoutMode = LAYOUT_MODE_UNDEFINED;
    
    boolean isLayoutModeOptical() {
        return mLayoutMode == LAYOUT_MODE_OPTICAL_BOUNDS;
    }
```
mLayoutMode的值默认是LAYOUT_MODE_UNDEFINED，也就是说：isLayoutModeOptical(mParent)返回false，所以会调用setFrame方法，并把四个位置信息传递进去，这个方法用于确定View的四个顶点的位置，即初始化mLeft,mRight,mTop,mBottom这四个值，当初始化完毕后，ViewGroup的布局流程也就完成了。
接下来layout方法里会回调onLayout()方法，该方法在ViewGroup中调用，用于确定子View的位置，即在该方法内部，子View会调用自身的layout方法来进一步完成自身的布局流程。由于上面Measure是对FrameLayout进行分析，则现在Layout也使用FrameLayout分析：
```
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        //把父容器的位置参数传递进去
        layoutChildren(left, top, right, bottom, false /* no force left gravity */);
    }

    void layoutChildren(int left, int top, int right, int bottom, boolean forceLeftGravity) {
        final int count = getChildCount();
        // 以下四个值会影响到子View的布局参数
        // parentLeft由父容器的padding和Foreground决定
        final int parentLeft = getPaddingLeftWithForeground();
        // parentRight由父容器的width和padding和Foreground决定
        final int parentRight = right - left - getPaddingRightWithForeground();

        final int parentTop = getPaddingTopWithForeground();
        final int parentBottom = bottom - top - getPaddingBottomWithForeground();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                // 获取子View的测量宽高
                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft;
                int childTop;

                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = DEFAULT_CHILD_GRAVITY;
                }

                final int layoutDirection = getLayoutDirection();
                final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
                final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
                //当子View设置了水平方向的layout_gravity属性时，根据不同的属性设置不同的childLeft
                //childLeft表示子View的 左上角坐标X值
                switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                /* 水平居中，由于子View要在水平中间的位置显示，因此，要先计算出以下：
                 * (parentRight - parentLeft -width)/2 此时得出的是父容器减去子View宽度后的
                 * 剩余空间的一半，那么再加上parentLeft后，就是子View初始左上角横坐标(此时正好位于中间位置)，
                 * 假如子View还受到margin约束，由于leftMargin使子View右偏而rightMargin使子View左偏，所以最后
                 * 是 +leftMargin -rightMargin .
                 */
                    case Gravity.CENTER_HORIZONTAL:
                        childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                        lp.leftMargin - lp.rightMargin;
                        break;
                    //水平居右，子View左上角横坐标等于 parentRight 减去子View的测量宽度 减去 margin
                    case Gravity.RIGHT:
                        if (!forceLeftGravity) {
                            childLeft = parentRight - width - lp.rightMargin;
                            break;
                        }
                    //如果没设置水平方向的layout_gravity，那么它默认是水平居左
                    //水平居左，子View的左上角横坐标等于 parentLeft 加上子View的magin值
                    case Gravity.LEFT:
                    default:
                        childLeft = parentLeft + lp.leftMargin;
                }
                //当子View设置了竖直方向的layout_gravity时，根据不同的属性设置同的childTop
                //childTop表示子View的 左上角坐标的Y值
                //分析方法同上
                switch (verticalGravity) {
                    case Gravity.TOP:
                        childTop = parentTop + lp.topMargin;
                        break;
                    case Gravity.CENTER_VERTICAL:
                        childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                        lp.topMargin - lp.bottomMargin;
                        break;
                    case Gravity.BOTTOM:
                        childTop = parentBottom - height - lp.bottomMargin;
                        break;
                    default:
                        childTop = parentTop + lp.topMargin;
                }
                //对子元素进行布局，左上角坐标为(childLeft，childTop)，右下角坐标为(childLeft+width,childTop+height)
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
        }
    }
```
onLayout方法内部直接调用了layoutChildren方法，而layoutChildren则是具体的实现。首先获取父容器的padding值，然后遍历其每一个子View，根据子View的layout_gravity属性、子View的测量宽高、父容器的padding值、来确定子View的布局参数，然后调用child.layout方法，把布局流程从父容器传递到子元素。如果子View是一个ViewGroup，那么就会重复以上步骤，如果是一个View，那么会直接调用View#layout方法。
## 绘制流程三部曲之Draw

现在就剩下最后的Draw啦，我们继续回到ViewRootImpl中看performDraw方法：
```
// ViewRootImpl类：
    private void performDraw() {
        ...
        try {
            draw(fullRedrawNeeded);
        } finally {
            mIsDrawing = false;
            Trace.traceEnd(Trace.TRACE_TAG_VIEW);
        }
        ...
        if (mReportNextDraw) {
            ...
            try {
                mWindowSession.finishDrawing(mWindow);
            } catch (RemoteException e) {
            }
        }
    }
```
里面又调用了ViewRootImpl#draw方法，并传递了fullRedrawNeeded参数，而该参数由mFullRedrawNeeded成员变量获取，它的作用是判断是否需要重新绘制全部视图，如果是第一次绘制视图，那么显然应该绘制所以的视图，如果由于某些原因，导致了视图重绘，那么就没有必要绘制所有视图。
```
// ViewRootImpl类：
    private void draw(boolean fullRedrawNeeded) {
        Surface surface = mSurface;
        ...
        if (!sFirstDrawComplete) {
            synchronized (sFirstDrawHandlers) {
                sFirstDrawComplete = true;
                final int count = sFirstDrawHandlers.size();
                for (int i = 0; i< count; i++) {
                    mHandler.post(sFirstDrawHandlers.get(i));
                }
            }
        }
        // 滑动到指定区域
        scrollToRectOrFocus(null, false);
        // 分发OnScrollChanged事件
        if (mAttachInfo.mViewScrollChanged) {
            mAttachInfo.mViewScrollChanged = false;
            mAttachInfo.mTreeObserver.dispatchOnScrollChanged();
        }

        boolean animating = mScroller != null && mScroller.computeScrollOffset();
        final int curScrollY;
        if (animating) {
            curScrollY = mScroller.getCurrY();
        } else {
            curScrollY = mScrollY;
        }
        // RootView滑动回调
        if (mCurScrollY != curScrollY) {
            mCurScrollY = curScrollY;
            fullRedrawNeeded = true;
            if (mView instanceof RootViewSurfaceTaker) {
                ((RootViewSurfaceTaker) mView).onRootViewScrollYChanged(mCurScrollY);
            }
        }

        final float appScale = mAttachInfo.mApplicationScale;
        final boolean scalingRequired = mAttachInfo.mScalingRequired;

        int resizeAlpha = 0;
        // 获取需要绘制的区域
        final Rect dirty = mDirty;
        if (mSurfaceHolder != null) {
            dirty.setEmpty();
            if (animating && mScroller != null) {
                mScroller.abortAnimation();
            }
            return;
        }

        //如果fullRedrawNeeded为真，则把dirty区域置为整个屏幕，表示整个视图都需要绘制
        //第一次绘制流程，需要绘制所有视图
        if (fullRedrawNeeded) {
            mAttachInfo.mIgnoreDirtyState = true;
            dirty.set(0, 0, (int) (mWidth * appScale + 0.5f), (int) (mHeight * appScale + 0.5f));
        }
        ...
        // 分发onDraw
        mAttachInfo.mTreeObserver.dispatchOnDraw();

        if (!dirty.isEmpty() || mIsAnimating || accessibilityFocusDirty) {
            if (mAttachInfo.mThreadedRenderer != null && mAttachInfo.mThreadedRenderer.isEnabled()) {
                //
            } else {
                //
                if (!drawSoftware(surface, mAttachInfo, xOffset, yOffset, scalingRequired, dirty)) {
                    return;
                }
            }
        }
        ...
    }
```
首先滑动到指定区域,然后获取了mDirty值，该值保存了需要重绘的区域的信息，接着根据fullRedrawNeeded来判断是否需要重置dirty区域，最后调用了ViewRootImpl.drawSoftware方法，并把相关参数传递进去，包括dirty区域。
```
// ViewRootImpl类：
    final Surface mSurface = new Surface(); // 成员变量
    private boolean drawSoftware(Surface surface, AttachInfo attachInfo
        , int xoff, int yoff,boolean scalingRequired, Rect dirty) {

        // Draw with software renderer.
        final Canvas canvas;
        try {
            final int left = dirty.left;
            final int top = dirty.top;
            final int right = dirty.right;
            final int bottom = dirty.bottom;
            // 从mSurface中锁定一块Canvas区域，由dirty区域决定
            canvas = mSurface.lockCanvas(dirty);

            // The dirty rectangle can be modified by Surface.lockCanvas()
            //noinspection ConstantConditions
            if (left != dirty.left || top != dirty.top || right != dirty.right
                    || bottom != dirty.bottom) {
                attachInfo.mIgnoreDirtyState = true;
            }

            // TODO: Do this in native
            canvas.setDensity(mDensity);
        } catch (Surface.OutOfResourcesException e) {
            handleOutOfResourcesException(e);
            return false;
        } catch (IllegalArgumentException e) {
            ...
            mLayoutRequested = true;    // ask wm for a new surface next time.
            return false;
        }

        try {
            ...
            try {
                canvas.translate(-xoff, -yoff);
                if (mTranslator != null) {
                    mTranslator.translateCanvas(canvas);
                }
                canvas.setScreenDensity(scalingRequired ? mNoncompatDensity : 0);
                attachInfo.mSetIgnoreDirtyState = false;
                // 正式开始绘制
                mView.draw(canvas);

                drawAccessibilityFocusedDrawableIfNeeded(canvas);
            } finally {
                ...
            }
        } finally {
            try {
                // 解锁Canvas，屏幕上马上就回出现绘制的画面长相
                surface.unlockCanvasAndPost(canvas);
            } catch (IllegalArgumentException e) {
                ...
            }
            ...
        }
        return true;
    }
```
首先是实例化了Canvas对象，然后从mSurface锁定一块Canvas的区域，由dirty区域决定，接着对canvas进行一系列的属性赋值，然后调用了mView.draw(canvas)方法绘制，最后解锁显示画面。我们在上面“onResume界面可见绘制之同步屏障--VSYNC同步”部分提到了Surface和SurfaceSession两个概念。而从ViewRootImpl.drawSoftware方法中也看到了Surface的身影。
现在来总结一下目前出现的和Surface有关的身影：
* 在ViewRootImpl初始化(构造时)，会创建一个Surface，上面代码可见，直接在成员变量new出来的。
* ViewRootImpl通过IWindowSession和WMS交互，而WMS调用的一个attahc方法会构造一个SurfaceSession，忘记的小伙伴请回看“onResume界面可见绘制之IWindowSession和IWindow”部分。
* ViewRootImpl在performTransval的处理过程中会调用IWindowSession的relayout方法，回看“绘制流程三部曲”部分。
* ViewRootImpl.drawSoftware方法中调用Surface的lockCanvas方法，得到一块画布
* ViewRootImpl.drawSoftware方法中调用Surface的unlockCanvasAndPost方法，释放这块画布

现在通过跟Surface有关的身影来说一下Surface，先来一张精简流程图：
![](https://user-gold-cdn.xitu.io/2019/4/18/16a30c1df55049b9?w=1054&h=381&f=png&s=82110)
图中出现了几个新类，主要看一下SurfaceComposerClient如何得到的，上面说到SurfaceSession类的初始化，回看“onResume界面可见绘制之IWindowSession和IWindow”
```
// SurfaceSession类：
    public SurfaceSession() {
        // 会调用native本地方法
        mNativeClient = nativeCreate();
    }
    
// android_view_SurfaceSession.cpp文件
    static jlong nativeCreate(JNIEnv* env, jclass clazz) {
    // 初始化SurfaceComposerClient，该对象会和SurfaceFlinger交互
    SurfaceComposerClient* client = new SurfaceComposerClient();
    client->incStrong((void*)nativeCreate);
    return reinterpret_cast<jlong>(client);
}
```
通过精简流程图和上述代码，我们可以发现一下几点：
1. SurfaceComposerClient是在初始化SurfaceSession时才显示初始化的
2. 在“onResume界面可见绘制之IWindowSession和IWindow”里我们讲解ViewRootImpl类中setView方法时，可以看到requestLayout()在mWindowSession.addToDisplay()之前调用，并且上面也说明了SurfaceSession初始化时机是在mWindowSession.addToDisplay中
3. 所有三部曲的逻辑都是由requestLayout()而来
4. 精简流程图上显示需要SurfaceComposerClient，才会有后面的mSurface.lockCanvas和mSurface.unlockCanvasAndPost

那么是什么让在走三部曲逻辑前，使得SurfaceSession初始化的呢？

其实，这个问题的答案就在requestLayout()里，我们上面“onResume界面可见绘制之同步屏障--VSYNC同步”讲到同步障碍：mHandler.getLooper().getQueue().postSyncBarrier()，就是因为这个，其发送了一个同步障碍消息后，会阻止消息队列中其它消息的执行，但是并不会停止代码的执行，此时代码会先越过requestLayout()继续向下走，此时就会初始化SurfaceSession...，等Looper轮询到同步障碍消息后继续走，直到回调mTraversalRunnable里的doTraversal()方法。这就类似于handler.post里代码和普通主线程代码：
```
// 主线程中：
    Handler handler = new Handler();
    handler.post(new Runnable() {
        @Override
        public void run() {
            Log.e("handler.post","handler.post：111111111111111111111111111111");
        }
    });
    for(int i = 0; i <10000; i++){
        Log.e("handler.post","handler.post："+i);
    }
    
//打印结果为：
    ......
    handler.post：9998
    handler.post：9999
    04-18 21:29:26.521 6249-6249/com.android.sourcecodeanalysis E/handler.post: handler.post：111111111111111111111111111111
```
因此来简单总结一下Surface：整个Activity的绘图流程就是从mSurface中lock一块Canvas，然后交给mView去自由发挥画画才能，最后unlockCanvasAndPost释放这块Canvas。
由于Surface这个系统非常复杂，包含Layer(显示层)、FrameBuffer(帧缓冲)、PageFlipping(画面交换:FrontBuffer前一帧缓冲画面和BackBuffer后一帧缓冲画面)、SurfaceFlinger(图像混合：多个显示层混合一起显示画面)以及Linux共享内存等知识点，这里就不再深究，有兴趣的小伙伴，可以参考“深入理解Android卷1（邓凡平）”一书第8章Surface系统，去进一步研究(切记阅读源码需谨慎，c/c++自我感觉很不错的小伙伴可以尝试深入一下)。

![](https://user-gold-cdn.xitu.io/2019/4/18/16a31030eff32dd3?w=220&h=220&f=png&s=17281)
接着看我们最后的draw的逻辑：
```
// View类：
 public void draw(Canvas canvas) {
    final int privateFlags = mPrivateFlags;
    final boolean dirtyOpaque = (privateFlags & PFLAG_DIRTY_MASK) == PFLAG_DIRTY_OPAQUE &&
            (mAttachInfo == null || !mAttachInfo.mIgnoreDirtyState);
    mPrivateFlags = (privateFlags & ~PFLAG_DIRTY_MASK) | PFLAG_DRAWN;

    /*
     * Draw traversal performs several drawing steps which must be executed
     * in the appropriate order:
     *
     *      1. Draw the background
     *      2. If necessary, save the canvas' layers to prepare for fading
     *      3. Draw view's content
     *      4. Draw children
     *      5. If necessary, draw the fading edges and restore layers
     *      6. Draw decorations (scrollbars for instance)
     */

    // Step 1, draw the background, if needed
    int saveCount;

    if (!dirtyOpaque) {
        drawBackground(canvas);
    }

    // skip step 2 & 5 if possible (common case)
    final int viewFlags = mViewFlags;
    boolean horizontalEdges = (viewFlags & FADING_EDGE_HORIZONTAL) != 0;
    boolean verticalEdges = (viewFlags & FADING_EDGE_VERTICAL) != 0;
    if (!verticalEdges && !horizontalEdges) {
        // Step 3, draw the content
        if (!dirtyOpaque) onDraw(canvas);

        // Step 4, draw the children
        dispatchDraw(canvas);

        // Overlay is part of the content and draws beneath Foreground
        if (mOverlay != null && !mOverlay.isEmpty()) {
            mOverlay.getOverlayView().dispatchDraw(canvas);
        }

        // Step 6, draw decorations (foreground, scrollbars)
            onDrawForeground(canvas);

        // Step 7, draw the default focus highlight
        drawDefaultFocusHighlight(canvas);
        
        if (debugDraw()) {
            debugDrawFocus(canvas);
        }
        
        // we're done...
        return;
    }
    ...
}
```
源码中已经给出了draw的步骤：
1. 对View的背景进行绘制
2. 保存当前的图层信息(可跳过)
3. 绘制View的内容
4. 对View的子View进行绘制(如果有子View)
5. 绘制View的褪色的边缘，类似于阴影效果(可跳过)
6. 绘制View的装饰（例如：滚动条）
源码中提示，其中第2步和第5步是可以跳过的，是常见的情况。
```
// View类：
    // 绘制背景
    private void drawBackground(Canvas canvas) {
        // mBackground是该View的背景参数，比如背景颜色
        final Drawable background = mBackground;
        if (background == null) {
            return;
        }
        // 根据View四个布局参数来确定背景的边界
        setBackgroundBounds();
        ...
        // 获取当前View的mScrollX和mScrollY值
        final int scrollX = mScrollX;
        final int scrollY = mScrollY;
        // 考虑到了view的偏移参数:scrollX和scrollY
        if ((scrollX | scrollY) == 0) {
            background.draw(canvas);
        } else {
            // 如果scrollX和scrollY有值，则对canvas的坐标进行偏移，再绘制背景
            canvas.translate(scrollX, scrollY);
            background.draw(canvas);
            canvas.translate(-scrollX, -scrollY);
        }
    }
    
    protected void onDraw(Canvas canvas) {
        // 该方法是一个空实现，因为不同的View有着不同的内容，需要自己去实现
        // 即在自定义View中重写该方法来实现。
    }
    // View中无子布局，所有dispatchDraw为空实现，该方法主要针对ViewGroup有子View情况
    protected void dispatchDraw(Canvas canvas) {

    }
```
如果该View是一个ViewGroup，则其绘制调用dispatchDraw(canvas);
```
// ViewGroup类：
protected void dispatchDraw(Canvas canvas) {
    boolean usingRenderNodeProperties = canvas.isRecordingFor(mRenderNode);
    final int childrenCount = mChildrenCount;
    final View[] children = mChildren;
    int flags = mGroupFlags;
    
    // 遍历了所以子View
    for (int i = 0; i < childrenCount; i++) {
        while (transientIndex >= 0 && mTransientIndices.get(transientIndex) == i) {
            final View transientChild = mTransientViews.get(transientIndex);
            if ((transientChild.mViewFlags & VISIBILITY_MASK) == VISIBLE ||
                    transientChild.getAnimation() != null) {
                more |= drawChild(canvas, transientChild, drawingTime);
            }
            transientIndex++;
            if (transientIndex >= transientCount) {
                transientIndex = -1;
            }
        }
        int childIndex = customOrder ? getChildDrawingOrder(childrenCount, i) : i;
        final View child = (preorderedList == null)
                ? children[childIndex] : preorderedList.get(childIndex);
        if ((child.mViewFlags & VISIBILITY_MASK) == VISIBLE || child.getAnimation() != null) {
            more |= drawChild(canvas, child, drawingTime);
        }
    }
    ......
}
```
可以看到dispatchDraw方法中会遍历所有子View，并且调用drawChild方法：
```
// ViewGroup类：
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        // 这里调用了View的draw方法，但这个方法并不是上面所说的，因为参数不同
        return child.draw(canvas, this, drawingTime);
    }
    
// View类：
    boolean draw(Canvas canvas, ViewGroup parent, long drawingTime) {
        ......
        if (!drawingWithDrawingCache) {
            if (drawingWithRenderNode) {
                mPrivateFlags &= ~PFLAG_DIRTY_MASK;
                ((DisplayListCanvas) canvas).drawRenderNode(renderNode);
            } else {
                // Fast path for layouts with no backgrounds
                if ((mPrivateFlags & PFLAG_SKIP_DRAW) == PFLAG_SKIP_DRAW) {
                    mPrivateFlags &= ~PFLAG_DIRTY_MASK;
                    dispatchDraw(canvas);
                } else {
                    draw(canvas);
                }
            }
        } else if (cache != null) {
            mPrivateFlags &= ~PFLAG_DIRTY_MASK;
            if (layerType == LAYER_TYPE_NONE) {
                // no layer paint, use temporary paint to draw bitmap
                Paint cachePaint = parent.mCachePaint;
                if (cachePaint == null) {
                    cachePaint = new Paint();
                    cachePaint.setDither(false);
                    parent.mCachePaint = cachePaint;
                }
                cachePaint.setAlpha((int) (alpha * 255));
                canvas.drawBitmap(cache, 0.0f, 0.0f, cachePaint);
            } else {
                // use layer paint to draw the bitmap, merging the two alphas, but also restore
                int layerPaintAlpha = mLayerPaint.getAlpha();
                mLayerPaint.setAlpha((int) (alpha * layerPaintAlpha));
                canvas.drawBitmap(cache, 0.0f, 0.0f, mLayerPaint);
                mLayerPaint.setAlpha(layerPaintAlpha);
            }
        }
    }
```
首先判断是否已经有缓存，即之前是否已经绘制过一次了，如果没有，则会调用draw(canvas)方法，开始正常的绘制，即上面所说的六个步骤，否则利用缓存来显示。ViewGroup绘制过程：dispatchDraw遍历绘制子View，若子View依旧为ViewGroup则接着dispatchDraw遍历绘制，直到不是ViewGroup为止。
最后就只剩下了前景绘制(onDrawForeground)：所谓的前景绘制，就是指View除了背景、内容、子View的其余部分，例如滚动条等.
```
// View类：
    public void onDrawForeground(Canvas canvas) {
        onDrawScrollIndicators(canvas);
        onDrawScrollBars(canvas);

        final Drawable foreground = mForegroundInfo != null ? mForegroundInfo.mDrawable : null;
        if (foreground != null) {
            if (mForegroundInfo.mBoundsChanged) {
                mForegroundInfo.mBoundsChanged = false;
                final Rect selfBounds = mForegroundInfo.mSelfBounds;
                final Rect overlayBounds = mForegroundInfo.mOverlayBounds;

                if (mForegroundInfo.mInsidePadding) {
                    selfBounds.set(0, 0, getWidth(), getHeight());
                } else {
                    selfBounds.set(getPaddingLeft(), getPaddingTop(),
                            getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
                }

                final int ld = getLayoutDirection();
                Gravity.apply(mForegroundInfo.mGravity, foreground.getIntrinsicWidth(),
                        foreground.getIntrinsicHeight(), selfBounds, overlayBounds, ld);
                foreground.setBounds(overlayBounds);
            }

            foreground.draw(canvas);
        }
    }
```
和一般的绘制流程非常相似，都是先设定绘制区域，然后利用canvas进行绘制。

至此，View整体绘制流程完成。

说下感受，此篇文章前前后后准备加写，用了5天空闲时间，最大的收获并不是只是知道了View的绘制流程，而是理解了自定义View什么时候需要调用什么方法和为什么这样调用的原理，并且以前有些看不懂的地方，突然感觉豁然开朗。虽然过程很辛苦，并且熬了几个晚上，但是感觉都是值得的。也许没有几个人能看完整篇，但还是希望看的小伙伴能在此文章中收获一点，一点点也是好的。最后告诫自己一句：深入源码需谨慎，能力不是很足时，不要太过深入，否则会迷失在里面。

![](https://user-gold-cdn.xitu.io/2019/4/18/16a3107abea11c6b?w=500&h=500&f=png&s=90388)

参考链接：

https://www.jianshu.com/p/a13e3a3259f3

https://www.jianshu.com/p/3299c3de0b7d

https://www.jianshu.com/p/dc6eeeb735e2

https://blog.csdn.net/freekiteyu/article/details/79408969

深入理解Android卷1（邓凡平）

...

<font color=#ff0000>若觉得文章不错或者不足之处，欢迎点赞留言，谢谢</font>