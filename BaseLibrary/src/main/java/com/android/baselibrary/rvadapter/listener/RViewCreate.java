package com.android.baselibrary.rvadapter.listener;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;

import com.android.baselibrary.rvadapter.base.RViewAdapter;

/**
 * 创建RViewHelper所需要的数据，它的实现类很方便创建RViewHelper对象
 * */
public interface RViewCreate<T> {

    Context context();

    /* 创建SwipeRefresh下拉 */
    SwipeRefreshLayout createSwipeRefresh();

    /* 创建RecycleView */
    RecyclerView createRecyclerView();

    /* 创建RecycleView.Adapter */
    RViewAdapter<T> createRecyclerViewAdapter();

    /* 是否支持分页 */
    boolean isSupportPaging();

}
