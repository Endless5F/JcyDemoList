package com.android.customwidget.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcy on 2018/8/8.
 */

public class HomeData {
    public static List<ItemView> addDevTotalRes = new ArrayList<>();

    static {
        addDeviceItem("", "自定义View demo");
        addDeviceItem("&#xe620;", "Canves draw 基本用法");
        addDeviceItem("&#xe620;", "Paint 画笔的基本用法");
        addDeviceItem("&#xe620;", "DrawText 绘制文字的基本用法");
        addDeviceItem("&#xe620;", "Clip和Matrix对绘制的辅助");
        addDeviceItem("&#xe620;", "View的绘制顺序");
        addDeviceItem("&#xe620;", "Animation（上手篇）");
        addDeviceItem("&#xe620;", "Animation（进阶篇）");
        addDeviceItem("&#xe620;", "自定义布局onMeasure基础");
        addDeviceItem("&#xe620;", "综合练习 之 即刻app点赞效果");
        addDeviceItem("&#xe620;", "综合练习 之 RecycleView滑动触摸监听");
        addDeviceItem("&#xe620;", "综合练习 之 Scroller弹性滑动");
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
