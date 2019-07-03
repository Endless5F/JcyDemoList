package com.android.baselibrary.rvadapter.listener;

import com.android.baselibrary.rvadapter.holder.RViewHodler;

// 当作位JavaBean对象：某一类型的Item对象（对应一种ViewType）
// 此接口提供给开发者实现
public interface RViewItem<T> {
    // 获取item布局
    int getItemLayout();

    // 是否开启点击事件
    boolean openClick();

    // 是否为当前布局
    boolean isItemView(T entity, int position);

    // 将item的控件与需要显示的数据绑定
    void convert(RViewHodler hodler, T entity, int position);
}
