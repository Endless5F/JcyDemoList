package com.android.performanceanalysis.http.dns;

import android.content.Context;
import androidx.annotation.NonNull;

import com.alibaba.sdk.android.httpdns.HttpDns;
import com.alibaba.sdk.android.httpdns.HttpDnsService;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Dns;

//添加OkHttpDNS解析类
public class OkHttpDNS implements Dns {

    private HttpDnsService dnsService;
    private static OkHttpDNS instance = null;

    private OkHttpDNS(Context context) {
        dnsService = HttpDns.getService(context, "");
    }

    public static OkHttpDNS getIns(Context context) {
        if (instance == null) {
            synchronized (OkHttpDNS.class) {
                if (instance == null) {
                    instance = new OkHttpDNS(context);
                }
            }
        }
        return instance;
    }

    @NonNull
    @Override
    public List<InetAddress> lookup(@NonNull String hostname) throws UnknownHostException {
        String ip = dnsService.getIpByHostAsync(hostname);
        //如果不为空走OkhttpDNS解析
        if (ip != null) {
            return Arrays.asList(InetAddress.getAllByName(ip));
        }
        //如果为空走系统的DNS解析
        return Dns.SYSTEM.lookup(hostname);
    }
}
