package com.android.performanceanalysis.handler;

import android.os.Message;

import java.util.concurrent.ConcurrentHashMap;

//数据帮助类
public class GetDetailHandlerHelper {

    private static ConcurrentHashMap<Message, String> sMsgDetail = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<Message, String> getMsgDetail() {
        return sMsgDetail;
    }

}
