package com.android.performanceanalysis.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

public class NetStatusUtils {

    @SuppressLint("HardwareIds")
    public static long getNetStatus(Context context, long startTime, long endTime) {
        Context applicationContext = context.getApplicationContext();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return 0;
        }
        // Rx --- R(ecive) 表示下行流量, 即下载接收.
        // Tx --- T(ransmit) 表示上行流量, 即上传发送.
        long netDataRx = 0; //接收
        long netDataTx = 0; //发送

        TelephonyManager telephonyManager =
                (TelephonyManager) applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return 0;
        }
        String subId = null;
        if (telephonyManager != null) {
            subId = telephonyManager.getSubscriberId();
        }
        @SuppressLint("WrongConstant")
        NetworkStatsManager manager =
                (NetworkStatsManager) applicationContext.getSystemService(Context.NETWORK_STATS_SERVICE);
        NetworkStats networkStats = null;
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        try {
            if (manager != null) {
                networkStats = manager.querySummary(NetworkCapabilities.TRANSPORT_WIFI, subId,
                        startTime, endTime);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        while (networkStats != null && networkStats.hasNextBucket()) {
            networkStats.getNextBucket(bucket);
            int uid = bucket.getUid();
            if (getUidByPackageName(context) == uid) {
                netDataRx += bucket.getRxBytes();
                netDataTx += bucket.getTxBytes();
            }
        }
        LogUtils.i("appNetUse" + (netDataRx + netDataTx));
        return netDataRx + netDataTx;
    }


    private static int getUidByPackageName(Context context) {
        Context applicationContext = context.getApplicationContext();
        int uid = -1;
        PackageManager packageManager = applicationContext.getPackageManager();
        try {
            PackageInfo packageInfo =
                    packageManager.getPackageInfo(applicationContext.getPackageName(), 0);
            uid = packageInfo.applicationInfo.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return uid;
    }
}
