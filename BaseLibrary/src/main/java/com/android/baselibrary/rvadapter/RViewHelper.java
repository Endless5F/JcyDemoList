package com.android.baselibrary.rvadapter;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.baselibrary.rvadapter.base.RViewAdapter;
import com.android.baselibrary.rvadapter.listener.RViewCreate;

import java.util.List;

// 帮助管理类
public class RViewHelper<T> {

    private Context context;
    private SwipeRefreshLayout swipeRefresh;
    private SwipeRefreshHelper swipeRefreshHelper;
    private RecyclerView recyclerView;
    private RViewAdapter<T> adapter;
    private int startPageNumber = 1;
    private boolean isSupportPaging;
    private SwipeRefreshHelper.SwipeRefreshLisenter lisenter;
    private int currentPaging;

    public RViewHelper(Builder<T> builder) {
        this.context = builder.create.context();
        this.swipeRefresh = builder.create.createSwipeRefresh();
        this.adapter = builder.create.createRecyclerViewAdapter();
        this.recyclerView = builder.create.createRecyclerView();
        this.isSupportPaging = builder.create.isSupportPaging();
        this.lisenter = builder.lisenter;

        this.currentPaging = this.startPageNumber;
        if (swipeRefresh != null) {
            swipeRefreshHelper = SwipeRefreshHelper.createSwipeRefreshHelper(swipeRefresh);
        }
        init();
    }

    private void init() {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if (swipeRefreshHelper != null) {
            swipeRefreshHelper.setRefreshLisenter(new SwipeRefreshHelper.SwipeRefreshLisenter() {
                @Override
                public void onRefresh() {
                    // 下拉刷新之前，都重置
                    if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                        // 页码也要重置
                        currentPaging = startPageNumber;
                        if (lisenter != null) {
                            lisenter.onRefresh();
                        }
                    }
                }
            });
        }
    }

    public void notifyAdapterDataSetChanged(List datas) {
        // 如果第一次加载或者下拉刷新
        if (currentPaging == startPageNumber) {
            adapter.updataDatas(datas);
        } else {
            adapter.addDatas(datas);
        }

        recyclerView.setAdapter(adapter);

        if (isSupportPaging){
            // todo ...
        }
    }

    public static class Builder<T> {
        private RViewCreate<T> create;//控件初始化
        private SwipeRefreshHelper.SwipeRefreshLisenter lisenter;//下拉刷新

        public Builder(RViewCreate<T> create, SwipeRefreshHelper.SwipeRefreshLisenter lisenter) {
            this.create = create;
            this.lisenter = lisenter;
        }

        public RViewHelper build() {
            return new RViewHelper(this);
        }
    }
}
