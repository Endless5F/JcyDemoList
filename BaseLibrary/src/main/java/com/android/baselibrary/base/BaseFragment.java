package com.android.baselibrary.base;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.android.baselibrary.base.bus.BusEvent;
import com.android.baselibrary.util.AppUtil;
import com.android.baselibrary.util.log.LoggerUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author jcy.
 * @date 2018/5/30
 * description BaseFragment作为Fragment的基类
 */

public abstract class BaseFragment extends Fragment {
    private static final String TAG = BaseFragment.class.getSimpleName();
    private Handler m_handler = null;
    private boolean isActive = true;

    protected static final int REQUEST_OPEN_BT_CODE = 0x1001;
    protected static final int REQUEST_OPEN_GPS_CODE = 0x1002;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isRegisterEventBus()) {
            EventBus.getDefault().register(this);
        }
        m_handler = new InFragmentHandler(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRegisterEventBus()) {
            EventBus.getDefault().unregister(this);
        }
        //添加内存泄漏监控
        AppUtil.getRefWatcher().watch(this);
        try {
            m_handler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            LoggerUtil.e(TAG, "onDestroy" + this.getClass().getSimpleName() + Log.getStackTraceString(e));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isActive = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isActive = false;
    }

    /*********************************************************************************************************************************
     * EventBus 相关函数
     ********************************************************************************************************************************/
    /**
     * 是否注册事件分发
     *
     * @return true绑定EventBus事件分发，默认不绑定，子类需要绑定的话复写此方法返回true.
     */
    protected boolean isRegisterEventBus() {
        return false;
    }

    /**
     * 监听EventBus消息
     *
     * @param busEvent 接收到的event
     * 注意:子类的方法中一定不要有@Subscribe，否则将导致
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMain(BusEvent busEvent) {
    }

    /**
     * 注意:子类的方法中一定不要有@Subscribe，否则将导致,切到了默认ThreadMode.POSTING
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEventAsync(BusEvent busEvent) {
    }

    /*********************************************************************************************************************************
     * Handler 相关函数
     ********************************************************************************************************************************/

    /**
     * 移除handler消息
     *
     * @param what Value to assign to the returned Message.what field.
     */
    public final void removeMessages(int what) {
        Handler handler = getHandler();
        if (handler == null) {
            return;
        }
        handler.removeMessages(what);
    }

    /**
     * 发送消息给handler
     *
     * @param what Value to assign to the returned Message.what field.
     * @param obj  An arbitrary object to send to the recipient.
     */
    public final void sendMessage(int what, Object obj) {
        Handler handler = getHandler();
        if (handler == null) {
            return;
        }
        Message msg = handler.obtainMessage(what);
        if (obj != null) {
            msg.obj = obj;
        }
        handler.sendMessage(msg);
    }

    /**
     * 发送消息给handler
     *
     * @param what Value to assign to the returned Message.what field.
     */
    public final void sendMessage(int what) {
        if (what == -1) {
            return;
        }
        sendMessage(what, null);
    }

    /**
     * 发送消息给handler
     *
     * @param what        Value to assign to the returned Message.what field.
     * @param obj         An arbitrary object to send to the recipient.
     * @param delayMillis 延时
     */
    public final void sendMessage(int what, Object obj, int delayMillis) {
        Handler handler = getHandler();
        if (handler == null) {
            return;
        }
        Message msg = handler.obtainMessage(what);
        if (obj != null) {
            msg.obj = obj;
        }
        handler.sendMessageDelayed(msg, delayMillis);
    }

    /**
     * 延迟发送消息给handler
     *
     * @param what        Value to assign to the returned Message.what field.
     * @param delayMillis 延时
     */
    public final void sendDelayMessage(int what, int delayMillis) {
        sendMessage(what, null, delayMillis);
    }

    /**
     * handler的Post
     *
     * @param r Runnable
     * @return Returns true if the Runnable was successfully placed in to the
     * message queue.  Returns false on failure, usually because the
     * looper processing the message queue is exiting.
     */
    public final boolean post(Runnable r) {
        Handler h = getHandler();
        if (h == null) {
            return false;
        }
        return h.post(r);
    }

    /**
     * handler的postDelay
     *
     * @param r     Runnable
     * @param delay 延时
     * @return true if the Runnable was successfully placed in to the
     * message queue.  Returns false on failure, usually because the
     * looper processing the message queue is exiting.  Note that a
     * result of true does not mean the Runnable will be processed --
     * if the looper is quit before the delivery time of the message
     * occurs then the message will be dropped.
     */
    public final boolean postDelayed(Runnable r, long delay) {
        Handler h = getHandler();
        if (h == null) {
            return false;
        }
        return h.postDelayed(r, delay);
    }

    /**
     * handler重新postDelay
     * 先去除Runnable，后重新postDelayed
     *
     * @param r     Runnable
     * @param delay 延时
     * @return true if the Runnable was successfully placed in to the
     * message queue.  Returns false on failure, usually because the
     * looper processing the message queue is exiting.  Note that a
     * result of true does not mean the Runnable will be processed --
     * if the looper is quit before the delivery time of the message
     * occurs then the message will be dropped.
     */
    public final boolean postDelayedOnce(Runnable r, long delay) {
        Handler h = getHandler();
        if (h == null) {
            return false;
        }
        h.removeCallbacks(r);
        return h.postDelayed(r, delay);
    }

    /**
     * 获取handler
     *
     * @return
     */
    public Handler getHandler() {
        return m_handler;
    }

    /**
     * 子类重写处理Handler方法
     *
     * @param msg 接收到的Message
     */
    protected void processHandlerMessage(Message msg) {

    }

    private static class InFragmentHandler extends Handler {
        private final WeakReference<BaseFragment> baseFragmentWeakRef;

        InFragmentHandler(BaseFragment fragment) {
            baseFragmentWeakRef = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            if (baseFragmentWeakRef.get() != null) {
                baseFragmentWeakRef.get().processHandlerMessage(msg);
            }
        }
    }


    /*********************************************************************************************************************************
     * 权限请求 相关函数
     * 权限组列表：
     * Android6.0只用申请权限组中一个权限及获得全部权限
     * Android8.0需要全部申请权限组权限，但是只会申请第一个权限时提示，后面不会提示
     *
     * // 读写日历。
     * Manifest.permission.READ_CALENDAR,
     * Manifest.permission.WRITE_CALENDAR
     * // 相机。
     * Manifest.permission.CAMERA
     * // 读写联系人。
     * Manifest.permission.READ_CONTACTS,
     * Manifest.permission.WRITE_CONTACTS,
     * Manifest.permission.GET_ACCOUNTS
     * // 读位置信息。
     * Manifest.permission.ACCESS_FINE_LOCATION,
     * Manifest.permission.ACCESS_COARSE_LOCATION
     * // 使用麦克风。
     * Manifest.permission.RECORD_AUDIO
     * // 读电话状态、打电话、读写电话记录。
     * Manifest.permission.READ_PHONE_STATE,
     * Manifest.permission.CALL_PHONE,
     * Manifest.permission.READ_CALL_LOG,
     * Manifest.permission.WRITE_CALL_LOG,
     * Manifest.permission.ADD_VOICEMAIL,
     * Manifest.permission.USE_SIP,
     * Manifest.permission.PROCESS_OUTGOING_CALLS
     * // 传感器。
     * Manifest.permission.BODY_SENSORS
     * // 读写短信、收发短信。
     * Manifest.permission.SEND_SMS,
     * Manifest.permission.RECEIVE_SMS,
     * Manifest.permission.READ_SMS,
     * Manifest.permission.RECEIVE_WAP_PUSH,
     * Manifest.permission.RECEIVE_MMS,
     * Manifest.permission.READ_CELL_BROADCASTS
     * // 读写存储卡。
     * Manifest.permission.READ_EXTERNAL_STORAGE,
     * Manifest.permission.WRITE_EXTERNAL_STORAGE
     ********************************************************************************************************************************/
    private static final int REQUEST_PERMISSIONS_CODE = 0x1000;

    /**
     * 权限请求相关函数
     *
     * @param permissions String[] 所有请求
     */
    protected void checkPermissions(String[] permissions) {
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            if (getActivity() != null) {
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), permission);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    onPermissionGranted(permission);
                } else {
                    permissionDeniedList.add(permission);
                }
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            requestPermissions(deniedPermissions, REQUEST_PERMISSIONS_CODE);
        }
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode
            , @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CODE:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i]);
                        } else {
                            onPermissionFailed(permissions[i]);
                        }
                    }
                }
                break;
            default:
        }
    }

    /**
     * 权限允许
     *
     * @param permission permission String
     */
    protected void onPermissionGranted(String permission) {
        if (isActive) {
            LoggerUtil.d(TAG, String.format(Locale.getDefault(), "%s %s", permission, "request_success"));
        }
    }

    /**
     * 权限拒绝
     *
     * @param permission permission String
     */
    protected void onPermissionFailed(String permission) {
        if (isActive) {
            LoggerUtil.d(TAG, String.format(Locale.getDefault(), "%s %s", permission, "request_failed"));
        }
    }

    LoadingDialogFragment mDialogFragment = null;

    public void showProgressDialog(boolean cancelable) {

        mDialogFragment = new LoadingDialogFragment();
        //1 设置任意非0主题可以去除圆角Card外的边框,使dialog全屏
        //2 直接使用apptheme会影响dialog的statusbar
        mDialogFragment.setStyle(DialogFragment.STYLE_NORMAL, -1);
        mDialogFragment.setCancelable(cancelable);
        if (getActivity() != null) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            mDialogFragment.show(ft, LoadingDialogFragment.POP_NAME_DIALOG);
        }
    }

    public void dismissProgressDialog() {
        if (mDialogFragment != null) {
            mDialogFragment.dismiss();
        }
        mDialogFragment = null;
    }


}
