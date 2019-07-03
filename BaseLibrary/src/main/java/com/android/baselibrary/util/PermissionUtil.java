package com.android.baselibrary.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.AppOpsManagerCompat;

import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.annotations.NonNull;

/**
 * Android6.0权限申请工具类
 */
public class PermissionUtil {

    public interface IPermissionListener {

        //权限被授权
        void permissionGranted();

        //权限被拒绝
        void permissionDenied();
    }

    /**
     * 申请获取相关权限
     *
     * @param context
     * @param iPermissionListener
     * @param toastDetails
     * @param permissions
     */
    @SuppressLint("CheckResult")
    public static void requestPermission(@NonNull Context context, @NonNull IPermissionListener iPermissionListener, @NonNull String toastDetails, @NonNull String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            iPermissionListener.permissionGranted();
        } else {
            new RxPermissions((Activity) context)
                    .request(permissions)
                    .subscribe(granted -> {
                        if (granted) {
                            if (hasOpsPermission(context, permissions)) {
                                iPermissionListener.permissionGranted();
                            } else {
                                iPermissionListener.permissionDenied();
                                ToastUtil.showShortToast(toastDetails);
                            }
                        } else {
                            iPermissionListener.permissionDenied();
                            ToastUtil.showShortToast(toastDetails);
                        }
                    });
        }
    }

    /**
     * Android6.0权限申请后再判断原生的权限是否真的被授权--适配部分国产机型（小米、华为、vivo、oppo等）
     *
     * @param context
     * @param permissions
     * @return
     */
    private static boolean hasOpsPermission(@NonNull Context context, @NonNull String... permissions) {
        for (String permission : permissions) {
            String op = AppOpsManagerCompat.permissionToOp(permission);
            int result = AppOpsManagerCompat.noteProxyOp(context, op, context.getPackageName());
            if (result == AppOpsManagerCompat.MODE_ALLOWED) return true;
        }
        return false;
    }

}