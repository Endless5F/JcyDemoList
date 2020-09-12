package com.android.performanceanalysis.http.bean;

//实体信息类
public class OkHttpEvent {
    public long dnsStartTime;
    public long dnsEndTime;
    public long responseBodySize;
    public boolean apiSuccess;
    public String errorReason;
}