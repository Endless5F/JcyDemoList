package com.android.framework.launch.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcy on 2018/8/8.
 */

public class HomeData {
    public static List<ItemView> addDevTotalRes = new ArrayList<>();

    static {
        addDeviceItem("", "工具类demo");
        addDeviceItem("&#xe620;", "DialogUtil");
        addDeviceItem("&#xe620;", "EventBusDemo");
        addDeviceItem("&#xe620;", "联网架构Demo");
        addDeviceItem("&#xe620;", "LayoutInflater.setFactory2");
        addDeviceItem("&#xe620;", "RecycleView+ViewPager联动");
        addDeviceItem("&#xe620;", "自定义键盘");
        addDeviceItem("&#xe620;", "混合Item单层RecycleView");
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
