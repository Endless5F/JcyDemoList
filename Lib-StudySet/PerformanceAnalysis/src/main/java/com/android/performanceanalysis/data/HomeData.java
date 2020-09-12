package com.android.performanceanalysis.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcy on 2018/8/8.
 */

public class HomeData {
    public static List<ItemView> addDevTotalRes = new ArrayList<>();

    static {
        addDeviceItem("", "性能分析与优化");
        addDeviceItem("&#xe620;", "WebView");
        addDeviceItem("&#xe620;", "AOP 学习");
        addDeviceItem("&#xe620;", "App启动优化");
        addDeviceItem("&#xe620;", "App内存优化");
        addDeviceItem("&#xe620;", "App布局优化");
        addDeviceItem("&#xe620;", "App卡顿优化");
        addDeviceItem("&#xe620;", "App线程优化");
        addDeviceItem("&#xe620;", "App网络优化");
        addDeviceItem("&#xe620;", "App电量优化");
        addDeviceItem("&#xe620;", "App瘦身优化");
        addDeviceItem("&#xe620;", "App稳定性优化");
        addDeviceItem("&#xe620;", "IdleHandler的使用");
    }

    private static void addDeviceItem(String icon, String desc) {
        addDevTotalRes.add(new ItemView(icon, desc));
    }

    public static class ItemView {
        public String icon;
        public String desc;

        public ItemView(String icon, String desc) {
            this.icon = icon;
            this.desc = desc;
        }
    }
}
