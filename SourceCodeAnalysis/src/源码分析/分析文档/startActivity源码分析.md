## Activity跳转流程
1. Activity类：startActivity->startActivity->startActivityForResult
```
Instrumentation.ActivityResult ar =
    // 执行启动Activity
    mInstrumentation.execStartActivity(
    this, mMainThread.getApplicationThread(), mToken, this,
    intent, requestCode, options);
if (ar != null) {
    // 发送启动Activity结果
    mMainThread.sendActivityResult(
    mToken, mEmbeddedID, requestCode, ar.getResultCode(),
   ar.getResultData());
}
```
  startActivityForResult方法中有一mParent成员变量，Activity类中会有判断mParent是否为NULL的情况， 而mParentmParent代表的是ActivityGroup，ActivityGroup最开始被用来在一个界面中嵌入多个Activity， 但是其在API13中已经被废弃了，系统推荐采用Fragment来代替ActivityGroup。Instrumentation在下面会介绍，通过mInstrumentation.execStartActivity，可以看出启动Activity交给了Instrumentation类。
  
2. Instrumentation类：Instrumentation将在应用程序的任何组件创建之前初始化，可以用来监控系统与应用的所有交互。
```
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        IApplicationThread whoThread = (IApplicationThread) contextThread;
        Uri referrer = target != null ? target.onProvideReferrer() : null;
        if (referrer != null) {
            intent.putExtra(Intent.EXTRA_REFERRER, referrer);
        }
        
        /**
         *  ActivityMonitor可以用来监控某个特定Intent的信息，
         *   可以通过Instrumentation.addMonitor方法来添加ActivityMonitor的实例。
         *   当一个新的Activity被启动时，会匹配Instrumentation中的ActivityMonitory实例列表
         *   ，如果匹配，就会累加命中计数器。ActivityMonitor也可以被用于获取新创建的
         *   Activity，通过waitForActivity方法，可返回一个匹配IntentFilter的Activity对象。
         *  注：waitForActivity()方法会阻塞直到有匹配该ActivityMonitor的Activity创建完成，**   最后将该Activity返回。
        */
        if (mActivityMonitors != null) {
            synchronized (mSync) {
                final int N = mActivityMonitors.size();
                for (int i=0; i<N; i++) {
                    final ActivityMonitor am = mActivityMonitors.get(i);
                    ActivityResult result = null;
                    if (am.ignoreMatchingSpecificIntents()) {
                        result = am.onStartActivity(intent);
                    }
                    if (result != null) {
                        am.mHits++;
                        return result;
                    } else if (am.match(who, null, intent)) {
                        am.mHits++;
                       //检查此监视器是否阻止Activity开始或允许它们正常执行。
                        if (am.isBlocking()) {
                            return requestCode >= 0 ? am.getResult() : null;
                        }
                        break;
                    }
                }
            }
        }
        try {
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(who);
            // 
            /**
             *  此处可以看出，启动Activity的真正实现由
             *   ActivityManager.getService().startActivity方法来完成
             *  通过下方源码可以看出ActivityManager.getService()返回的是IActivityManager
             *   而IActivityManager则为Binder接口，因此它的具体实现是ActivityManagerService
            */
            int result = ActivityManager.getService()
                .startActivity(whoThread, who.getBasePackageName(), intent,
                        intent.resolveTypeIfNeeded(who.getContentResolver()),
                        token, target != null ? target.mEmbeddedID : null,
                        requestCode, 0, null, options);
            // 用来检查Activity的启动情况，若忘记在AndroidManifest.xml注册Activity，则会在
            // 此处报异常：have you declared this activity in your AndroidManifest.xml?
            checkStartActivityResult(result, intent);
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
        return null;
    }
    
    // ActivityManager.getService()
    public static IActivityManager getService() {
        return IActivityManagerSingleton.get();
    }
    private static final Singleton<IActivityManager> IActivityManagerSingleton =
            new Singleton<IActivityManager>() {
                @Override
                protected IActivityManager create() {
                    final IBinder b = ServiceManager.getService(Context.ACTIVITY_SERVICE);
                    final IActivityManager am = IActivityManager.Stub.asInterface(b);
                    return am;
                }
            };
```
  从代码中可以看出，此时启动Activity由ActivityManagerService的startActivity方法执行
  
3. ActivityManagerService类源码中startActivity方法将启动Activity交给了startActivityAsUser方法，而startActivityAsUser又交给了ActivityStarter类中的startActivityMayWait方法
4. ActivityStarter类：ActivityStarter是Android7.0新加入的类，它是加载Activity的控制类，会收集所有的逻辑来决定如何将Intent和Flags转换为Activity，并将Activity和Task以及Stack相关联。
ActivityStarter的startActivityMayWait方法最终会调用startActivityLocked方法。
```
    final int startActivityMayWait(IApplicationThread caller, int callingUid,
            String callingPackage, Intent intent, String resolvedType,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            IBinder resultTo, String resultWho, int requestCode, int startFlags,
            ProfilerInfo profilerInfo, WaitResult outResult,
            Configuration globalConfig, Bundle bOptions, boolean ignoreTargetSecurity, int userId,
            TaskRecord inTask, String reason) {
            
        ......
        // mSupervisor(ActivityStackSupervisor) Task是以堆栈形式组织Activity的集合，
        // 而Task又由ActivityStack管理，ActivityStackSupervisor则是管理ActivityStack的类
        ResolveInfo rInfo = mSupervisor.resolveIntent(intent, resolvedType, userId);
        ......
        
            int res = startActivityLocked(caller, intent, ephemeralIntent, resolvedType,
                    aInfo, rInfo, voiceSession, voiceInteractor,
                    resultTo, resultWho, requestCode, callingPid,
                    callingUid, callingPackage, realCallingPid, realCallingUid, startFlags,
                    options, ignoreTargetSecurity, componentSpecified, outRecord, inTask,
                    reason);
        ......
    }
```
  而startActivityLocked方法会调用本类中的startActivity方法，startActivity方法中经过一系列操作(比如：检查权限checkStartAnyActivityPermission)后，又会调用同名重载方法startActivity
  ```
  // 重载方法
  private int startActivity(final ActivityRecord r, ActivityRecord sourceRecord,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask,
            ActivityRecord[] outActivity) {
        int result = START_CANCELED;
        try {
            mService.mWindowManager.deferSurfaceLayout();
            // startActivityUnchecked方法里
            // 会根据启动标志位和Activity启动模式来决定如何启动一个Activity以及
            // 是否要调用deliverNewIntent方法通知Activity有一个Intent试图重新启动它。
            result = startActivityUnchecked(r, sourceRecord, voiceSession, voiceInteractor,
                    startFlags, doResume, options, inTask, outActivity);
        } finally {
            // 如果我们无法继续，将活动与任务取消关联。
            if (!ActivityManager.isStartResultSuccessful(result)
                    && mStartActivity.getTask() != null) {
                mStartActivity.getTask().removeActivity(mStartActivity);
            }
            mService.mWindowManager.continueSurfaceLayout();
        }

        postStartActivityProcessing(r, result
            , mSupervisor.getLastStack().mStackId,  mSourceRecord,mTargetStack);

        return result;
    }
  ```
  startActivityUnchecked方法里无论已什么模式启动Activity，都会走到ActivityStackSupervisor类中的resumeFocusedStackTopActivityLocked方法中
  
5. ActivityStackSupervisor类，resumeFocusedStackTopActivityLocked里又走入同名重载方法里
  ```
    boolean resumeFocusedStackTopActivityLocked(
        ActivityStack targetStack, ActivityRecord target, ActivityOptions  targetOptions) {
        ......
        if (targetStack != null && isFocusedStack(targetStack)) {
            return targetStack.resumeTopActivityUncheckedLocked(target, targetOptions);
        }

        final ActivityRecord r = mFocusedStack.topRunningActivityLocked();
        if (r == null || !r.isState(RESUMED)) {
            mFocusedStack.resumeTopActivityUncheckedLocked(null, null);
        } else if (r.isState(RESUMED)) {
            // Kick off any lingering app transitions form the MoveTaskToFront operation.
            mFocusedStack.executeAppTransition(targetOptions);
        }
        ...
        return false;
    }

    // ActivityStack.java
    boolean resumeTopActivityUncheckedLocked(ActivityRecord prev, ActivityOptions options){
            ...
            result = resumeTopActivityInnerLocked(prev, options);
            ...
        return result;
    }
    private boolean resumeTopActivityInnerLocked(ActivityRecord prev, ActivityOptions         options) {
        ...
        boolean pausing = mStackSupervisor.pauseBackStacks(userLeaving, next, false);
        if (mResumedActivity != null) {
            // startPausingLocked方法里，栈顶Activity执行onPause方法退出
            pausing |= startPausingLocked(userLeaving, false, next, false);
        }
        ......
        mStackSupervisor.startSpecificActivityLocked(next, true, true);
        ...
        return true;
    }
  ```
  从重载方法resumeFocusedStackTopActivityLocked中，又调用ActivityStack类中的
  resumeTopActivityUncheckedLocked方法，该方法里调用resumeTopActivityInnerLocked方法中会去判断是否有Activity处于Resume状态，如果有的话会先让这个Activity执行Pausing过程，然后再执行startSpecificActivityLocked方法启动要启动Activity。
  ```
    void startSpecificActivityLocked(ActivityRecord r,
            boolean andResume, boolean checkConfig) {
        ProcessRecord app = mService.getProcessRecordLocked(r.processName,
                r.info.applicationInfo.uid, true);

        r.getStack().setLaunchTime(r);

        if (app != null && app.thread != null) {
            try {
                if ((r.info.flags&ActivityInfo.FLAG_MULTIPROCESS) == 0
                        || !"android".equals(r.info.packageName)) {
                    app.addPackage(r.info.packageName, r.info.applicationInfo.versionCode,
                            mService.mProcessStats);
                }
                // 如果不是第一次启动直接startActivity
                realStartActivityLocked(r, app, andResume, checkConfig);
                return;
            } catch (RemoteException e) {
                Slog.w(TAG, "Exception when starting activity "
                        + r.intent.getComponent().flattenToShortString(), e);
            }
        }
        // 如果是第一次启动，需要创建新的进程
        mService.startProcessLocked(r.processName, r.info.applicationInfo, true, 0,
                "activity", r.intent.getComponent(), false, false, true);
    }
  ```
  * 假设此时是第一次启动，则走ActivityManagerService中的startProcessLocked方法创建进程
  ```
    final ProcessRecord startProcessLocked(String processName, ApplicationInfo info,
            boolean knownToBeDead, int intentFlags, String hostingType, ComponentName hostingName,boolean allowWhileBooting, boolean isolated, int isolatedUid, boolean keepIfLarge,String abiOverride, String entryPoint, String[] entryPointArgs, Runnable crashHandler) {
        long startTime = SystemClock.elapsedRealtime();
        ProcessRecord app;
        if (!isolated) {
            app = getProcessRecordLocked(processName, info.uid, keepIfLarge);
            checkTime(startTime, "startProcess: after getProcessRecord");
            ...
        } else {
            app = null;
        }
        ...
        startProcessLocked(
                app, hostingType, hostingNameStr, abiOverride, entryPoint, entryPointArgs);
        checkTime(startTime, "startProcess: done starting proc!");
        return (app.pid != 0) ? app : null;
    }

    private final void startProcessLocked(ProcessRecord app,
            String hostingType, String hostingNameStr) {
        startProcessLocked(app, hostingType, hostingNameStr, null /* abiOverride */,
                null /* entryPoint */, null /* entryPointArgs */);
    }

    private final void startProcessLocked(ProcessRecord app, String hostingType,
            String hostingNameStr, String abiOverride, String entryPoint, String[] entryPointArgs) {
        startProcessLocked(app, hostingType, hostingNameStr, false /* disableHiddenApiChecks */,
                null /* abiOverride */, null /* entryPoint */, null /* entryPointArgs */);
    }
  
  private final void startProcessLocked(ProcessRecord app, String hostingType,
            String hostingNameStr, boolean disableHiddenApiChecks, String abiOverride,
            String entryPoint, String[] entryPointArgs) {
        long startTime = SystemClock.elapsedRealtime();
        ......// 初始化设置app
        if (entryPoint == null) entryPoint = "android.app.ActivityThread";
        ...
        ProcessStartResult startResult;
        // 指定不是webview_service
        if (hostingType.equals("webview_service")) {
            startResult = startWebView(entryPoint,
                    app.processName, uid, uid, gids, runtimeFlags, mountExternal,
                    app.info.targetSdkVersion, seInfo, requiredAbi, instructionSet,
                    app.info.dataDir, null, entryPointArgs);
        } else {
            // 开启新进程，并制定这个进程的入口是 ActivityThread 的静态方法 main
            // entryPoint，由上面代码可知 entryPoint == "android.app.ActivityThread"
            startResult = Process.start(entryPoint,
                    app.processName, uid, uid, gids, runtimeFlags, mountExternal,
                    app.info.targetSdkVersion, seInfo, requiredAbi, instructionSet,
                    app.info.dataDir, invokeWith, entryPointArgs);
        }
            ...
        }
    }
    
    // Process类
    public static final ProcessStartResult start(final String processClass,
        final String niceName,int uid, int gid, int[] gids,int runtimeFlags, int mountExternal,int targetSdkVersion,String seInfo,String abi,String instructionSet,String appDataDir,String invokeWith,String[] zygoteArgs) {
        // 请求 Zygote 来创建一个应用程序进程
        return zygoteProcess.start(processClass, niceName, uid, gid, gids,
                    runtimeFlags, mountExternal, targetSdkVersion, seInfo,
                    abi, instructionSet, appDataDir, invokeWith, zygoteArgs);
    }
    // ZygoteProcess类
    public final Process.ProcessStartResult start(final String processClass,
        final String niceName,int uid, int gid, int[] gids,int runtimeFlags, 
        int mountExternal,int targetSdkVersion,String seInfo,String abi,
        String instructionSet,String appDataDir,String invokeWith,String[] zygoteArgs) {
        try {
            return startViaZygote(processClass, niceName, uid, gid, gids,
                    runtimeFlags, mountExternal, targetSdkVersion, seInfo,
                    abi, instructionSet, appDataDir, invokeWith,
                    false /* startChildZygote */,zygoteArgs);
        } catch (ZygoteStartFailedEx ex) {
            Log.e(LOG_TAG,"Starting VM process through Zygote failed");
            throw new RuntimeException(
                    "Starting VM process through Zygote failed", ex);
        }
    }
    private Process.ProcessStartResult startViaZygote(final String processClass,
        final String niceName,final int uid, final int gid,final int[] gids,
        int runtimeFlags, int mountExternal,int targetSdkVersion,String seInfo,
        String abi,String instructionSet,String appDataDir,String invokeWith,
         boolean startChildZygote,String[] extraArgs)throws ZygoteStartFailedEx {
        // 初始化进程的启动参数列表
        ArrayList<String> argsForZygote = new ArrayList<String>();
        ......
        synchronized(mLock) {
            // 初始化完毕
            // openZygoteSocketIfNeeded(),创建一个连接到 Zygote 的 LocalSocket 对象
            return zygoteSendArgsAndGetResult(openZygoteSocketIfNeeded(abi), argsForZygote);
        }
    }
    private static Process.ProcessStartResult zygoteSendArgsAndGetResult(
            ZygoteState zygoteState, ArrayList<String> args)
            throws ZygoteStartFailedEx {
        try {
            int sz = args.size();
            for (int i = 0; i < sz; i++) {
                if (args.get(i).indexOf('\n') >= 0) {
                    throw new ZygoteStartFailedEx("embedded newlines not allowed");
                }
            }
            // 写参数给Zygote
            final BufferedWriter writer = zygoteState.writer;
            final DataInputStream inputStream = zygoteState.inputStream;
            // 将要创建的应用程序的进程启动参数传到 LocalSocket 对象中
            writer.write(Integer.toString(args.size()));
            writer.newLine();
            for (int i = 0; i < sz; i++) {
                String arg = args.get(i);
                writer.write(arg);
                writer.newLine();
            }
            writer.flush();
            Process.ProcessStartResult result = new Process.ProcessStartResult();
            //等待socket服务端（即zygote）返回新创建的进程pid;
            result.pid = inputStream.readInt();
            result.usingWrapper = inputStream.readBoolean();
            if (result.pid < 0) {
                throw new ZygoteStartFailedEx("fork() failed");
            }
            return result;
        } catch (IOException ex) {
            zygoteState.close();
            throw new ZygoteStartFailedEx(ex);
        }
    }
    private static ZygoteState openZygoteSocketIfNeeded(String abi) 
        throws ZygoteStartFailedEx {
    if (primaryZygoteState == null || primaryZygoteState.isClosed()) {
        try {
            // 向主zygote发起connect()操作
            primaryZygoteState = ZygoteState.connect(ZYGOTE_SOCKET);
        } catch (IOException ioe) {
            ...
        }
    }

    if (primaryZygoteState.matches(abi)) {
        return primaryZygoteState;
    }

    if (secondaryZygoteState == null || secondaryZygoteState.isClosed()) {
        // 当主zygote没能匹配成功，则采用第二个zygote，发起connect()操作
        secondaryZygoteState = ZygoteState.connect(SECONDARY_ZYGOTE_SOCKET);
    }

    if (secondaryZygoteState.matches(abi)) {
        return secondaryZygoteState;
    }
    ...
}
  ```
system_server进程的zygoteSendArgsAndGetResult()方法通过socket向Zygote进程发送消息，这是便会唤醒Zygote进程，来响应socket客户端的请求（即system_server端），接下来的操作便是在Zygote来创建进程，Zygote进程是由init进程而创建的，进程启动之后调用ZygoteInit.main()->runSelectLoop->
acceptCommandPeer->runOnce->Zygote.forkAndSpecialize执行完此方法又经过一系列native以及c/c++代码完成所有App进程的工作->handleChildProc->...->ActivityThread.main
* 假设此时不是第一次启动，则走ActivityStackSupervisor中的realStartActivityLocked方法
```
final boolean realStartActivityLocked(ActivityRecord r, ProcessRecord app,
            boolean andResume, boolean checkConfig) throws RemoteException {
        ......
        /*
         *  这个app.thread的类型为IApplicationThread，
         *   IApplicationThread继承了IInterface接口，所以它是一个Binder类型的接口。
         *   从IApplicationThread声明的接口方法可以看出，它的内部包含了
         *   大量启动、停止Activity的接口，此外还包含了启动和停止服务的接口，从接口方法的命 *   名可以知道，IApplicationThread这个Binder接口的实现者完成了大量和Activity以及
         *   Service启动和停止相关的功能。而IApplicationThread的实现者就是
         *   ActivityThread中的内部类ApplicationThread。所以，绕来绕去，
         *   是用ApplicationThread中的scheduleLaun*chActivity来启动Activity的。
        */
        app.thread.scheduleLaunchActivity(new Intent(r.intent), r.appToken,
            System.identityHashCode(r), r.info,
            mergedConfiguration.getGlobalConfiguration(),
            mergedConfiguration.getOverrideConfiguration(), r.compat,
            r.launchedFromPackage, task.voiceInteractor, app.repProcState, r.icicle,
            r.persistentState, results, newIntents, !andResume,
            mService.isNextTransitionForward(), profilerInfo);
        ......
        return true;
    }
```
  此时走到了ApplicationThread类中(ApplicationThread类为ActivityThread静态内部类)的
  scheduleLaunchActivity方法中，该方法使用Handler发送LAUNCH_ACTIVITY消息后，调用
  handleLaunchActivity方法 -> 最终在performLaunchActivity方法中的创建并启动Activity
  ```
  private void handleLaunchActivity(ActivityClientRecord r
            , Intent customIntent, String reason) {
        ...
        // 启动Activity在此！！！！
        Activity a = performLaunchActivity(r, customIntent);

        if (a != null) {
            r.createdConfig = new Configuration(mConfiguration);
            reportSizeConfigurations(r);
            Bundle oldState = r.state;
            // 调用Activity的onResume方法
            handleResumeActivity(r.token, false, r.isForward,
                !r.activity.mFinished && !r.startsNotResumed, r.lastProcessedSeq, reason);
            ...
        } else {
            ...
        }
    }
  ```
  至此，Activity启动完成