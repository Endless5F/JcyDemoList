## Hook实践
免注册跳转Activity：直接上代码
```
package com.android.hookjumpactivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HookUtil {
    private Context context;
    private Class<?> proxyActivity;

    public HookUtil(Context context, Class<?> proxyActivity) {
        this.context = context;
        this.proxyActivity = proxyActivity;
    }

    /**
     * 在AMS检测前设置钩子
     * 
     * 根据Activity跳转流程源码分析可知，Activity跳转需要使用到IActivityManager，
     * 而IActivityManager可通过反射获取，而可通过动态代理替换掉原有的IActivityManager，
     * 随后通过替换过的iActivityManager调用startActivity时即可触发代理类中真正执行方法前后的操作，
     * 因此只需要在代理类中将真实想跳转的Intent替换成在清单文件中注册的ProxyActivity
     * ，并且将真实的Intent当作参数传递过去，通过ProxyActivity的意图绕过AMS检测，并在后面将真实的Intent替换回来即可。
     */
    public void hookAms() {
        try {
            Class<?> ActivityManagerNativecls = Class.forName("android.app.ActivityManagerNative");
            Field gDefault = ActivityManagerNativecls.getDeclaredField("gDefault");
            gDefault.setAccessible(true);
            //因为是静态变量  所以获取的到的是系统值  hook   伪hook
            Object defaltValue = gDefault.get(null);
            //mInstance对象
            Class<?> SingletonClass = Class.forName("android.util.Singleton");
            Field mInstance = SingletonClass.getDeclaredField("mInstance");
            //还原 IActivityManager对象  系统对象
            mInstance.setAccessible(true);
            Object iActivityManagerObject = mInstance.get(defaltValue);
            Class<?> iActivityManagerIntercept = Class.forName("android.app.IActivityManager");

            //第二参数  是即将返回的对象 需要实现那些接口,其中这些接口包含OnClickListener，和IActivityManagerIntercept所实现的接口。
            //也就是说IActivityManager和OnClickListener所实现的接口都动态替换成startActivtyMethod了
            Object oldIactivityManager = Proxy
                    .newProxyInstance(Thread.currentThread().getContextClassLoader()
                            , new Class[]{iActivityManagerIntercept}, new AmsInvocationHandler
                                    (iActivityManagerObject));

            //将系统的iActivityManager  替换成   自己通过动态代理实现的对象
            //oldIactivityManager对象 实现了 IActivityManager这个接口的所有方法
            mInstance.set(defaltValue, oldIactivityManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class AmsInvocationHandler implements InvocationHandler {
        private Object iActivityManagerObject;

        public AmsInvocationHandler(Object iActivityManagerObject) {
            this.iActivityManagerObject = iActivityManagerObject;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Log.i("INFO", "invoke    " + method.getName());
            if ("startActivity".equals(method.getName())) {
                Log.i("INFO", "-----------------startActivity--------------------------");
                //瞒天过海
                //寻找传进来的原intent(要跳转的意图)
                Intent intent = null;
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    //intent
                    Object arg = args[i];
                    if (arg instanceof Intent) {
                        // 此Intent为原意图，未注册清单文件的意图
                        intent = (Intent) args[i];
                        index = i;
                    }
                }
                Intent proxyIntent = new Intent();
                //ProxyActivity是合法意图(注册清单文件的意图)，这里用它通过AMS检测
                ComponentName componentName = new ComponentName(context, proxyActivity);
                proxyIntent.setComponent(componentName);
                //真实的意图 被我隐藏到了  键值对中，等待待会绕过AMS后再通过ActivityMH取出来。
                proxyIntent.putExtra("realIntent", intent);
                args[index] = proxyIntent;
            }

            return method.invoke(iActivityManagerObject, args);
        }
    }

    /**
     * 在AMS检测后设置钩子
     * 
     * 根据Activity跳转流程源码分析可知，Activity的创建必不可少的是需要走ActivityThread类中的
     * scheduleLaunchActivity方法，此方法中调用sendMessage(H.LAUNCH_ACTIVITY, r)发送此消息执行
     * handleLaunchActivity(r, null, "LAUNCH_ACTIVITY")方法
     * ，此方法调用performLaunchActivity(r, customIntent)来真正创建Activity
     * 主要关注点在sendMessage(H.LAUNCH_ACTIVITY, r)方法中，此方法通过Handler发送消息，
     * 而通过Handler机制可知，消息的分发方法dispatchMessage中，首先判断msg.callback此回调是否为空，
     * 若不为空则直接执行handleCallback(msg)方法，然后执行handleCallback方法message.callback.run()，
     * 因此只需要在 AmsInvocationHandler 绕过AMS检测后，可通过反射msg.callback，在callback将真实要跳转的
     * Intent意图替换回来，即可在后面执行handleLaunchActivity时创建出我们真实需要跳转的Activity
     */
    public void hookSysHandler() {
        try {
            Class<?> forName = Class.forName("android.app.ActivityThread");
            Field currentActivityThreadField = forName.getDeclaredField("sCurrentActivityThread");
            currentActivityThreadField.setAccessible(true);
            //还原系统的ActivityTread   mH
            Object activityThreadObj = currentActivityThreadField.get(null);

            Field handlerField = forName.getDeclaredField("mH");
            handlerField.setAccessible(true);
            //hook点找到了
            Handler mH = (Handler) handlerField.get(activityThreadObj);
            Field callbackField = Handler.class.getDeclaredField("mCallback");

            callbackField.setAccessible(true);

            callbackField.set(mH, new ActivityThreadHandlerCallback(mH));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ActivityThreadHandlerCallback implements Handler.Callback {
        private Handler mH;

        public ActivityThreadHandlerCallback(Handler mH) {
            this.mH = mH;
        }

        @Override
        public boolean handleMessage(Message msg) {
            //LAUNCH_ACTIVITY ==100 即将要加载一个activity了,这里是系统的规范定义的
            if (msg.what == 100) {
                //替换回真实的Intent
                handleLuachActivity(msg);
            }
            //做了真正的跳转
            mH.handleMessage(msg);
            return true;
        }

        private void handleLuachActivity(Message msg) {
            //还原
            Object obj = msg.obj;
            try {
                Field intentField = obj.getClass().getDeclaredField("intent");
                intentField.setAccessible(true);
                //  ProxyActivity   2
                Intent proxyIntent = (Intent) intentField.get(obj);
                // 到这里后，其实已经通过AMS检测了，
                // 这里将我们当作参数的realIntent取出来，并将真实用来跳转的Component设置回去。
                Intent realIntent = proxyIntent.getParcelableExtra("realIntent");
                if (realIntent != null) {
                    proxyIntent.setComponent(realIntent.getComponent());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

```
使用方法：
```
package com.android.hookjumpactivity;

import android.app.Application;

public class HookApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HookUtil hookUtil = new HookUtil(this, ProxyActivity.class);
        hookUtil.hookAms();
        hookUtil.hookSysHandler();
    }
}


AndroidManifest.xml文件中：
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.android.hookjumpactivity">

    <application
        android:name=".HookApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity android:name=".ProxyActivity"/>
    </application>

</manifest>
```

具体讲解已在代码中。