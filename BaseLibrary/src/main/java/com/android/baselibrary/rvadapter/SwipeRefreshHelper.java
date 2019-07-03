package com.android.baselibrary.rvadapter;

import android.support.v4.widget.SwipeRefreshLayout;

public class SwipeRefreshHelper {
    private SwipeRefreshLayout swipeRefresh;
    private SwipeRefreshLisenter refreshLisenter;

    static SwipeRefreshHelper createSwipeRefreshHelper(SwipeRefreshLayout swipeRefresh){
        return new SwipeRefreshHelper(swipeRefresh);
    }

    public SwipeRefreshHelper(SwipeRefreshLayout swipeRefresh) {
        this.swipeRefresh = swipeRefresh;
        init();
    }

    private void init() {
        swipeRefresh.setColorSchemeResources(android.R.color.holo_orange_dark,
                android.R.color.holo_green_dark,android.R.color.holo_blue_dark);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (refreshLisenter != null){
                    refreshLisenter.onRefresh();
                }
            }
        });
    }

    public interface SwipeRefreshLisenter{
        void onRefresh();
    }

    public void setRefreshLisenter(SwipeRefreshLisenter refreshLisenter){
        this.refreshLisenter = refreshLisenter;
    }
}
