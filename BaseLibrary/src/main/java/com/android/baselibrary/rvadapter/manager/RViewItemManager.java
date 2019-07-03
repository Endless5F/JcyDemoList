package com.android.baselibrary.rvadapter.manager;

import android.support.v4.util.SparseArrayCompat;

import com.android.baselibrary.rvadapter.holder.RViewHodler;
import com.android.baselibrary.rvadapter.listener.RViewItem;

public class RViewItemManager<T> {

    // 所有RViewItem集合 key：viewType
    private SparseArrayCompat<RViewItem<T>> styles = new SparseArrayCompat<>();

    public void addRViewItem(RViewItem<T> item) {
        if (item != null) {
            styles.put(styles.size(), item);
        }
    }

    public int getItemStylesCount() {
        return styles.size();
    }

    // 根据数据源和位置返回某item类型的ViewType（即styles集合中的key）
    public int getItemViewType(T entity, int position) throws IllegalAccessException {
        for (int i = 0; i < styles.size(); i++) {
            RViewItem<T> item = (RViewItem<T>) styles.valueAt(i);
            if (item.isItemView(entity, position)) {
                return styles.keyAt(position);
            }
        }
        throw new IllegalAccessException("该item没有匹配的RViewItem条目类型");
    }

    public RViewItem getRViewItem(int viewType) {
        return styles.get(viewType);
    }

    // 将视图和数据绑定
    public void convert(RViewHodler hodler, T entity, int position) throws IllegalAccessException {
        for (int i = 0; i < styles.size(); i++) {
            RViewItem<T> item = (RViewItem<T>) styles.valueAt(i);
            if (item.isItemView(entity, position)) {
                // 条目赋值过程
                item.convert(hodler, entity, position);
                return;
            }
        }
        throw new IllegalAccessException("该item没有匹配的RViewItem条目类型");
    }
}
