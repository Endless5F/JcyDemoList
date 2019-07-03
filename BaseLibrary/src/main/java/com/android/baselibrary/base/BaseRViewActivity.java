package com.android.baselibrary.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.android.baselibrary.R;
import com.android.baselibrary.rvadapter.RViewHelper;
import com.android.baselibrary.rvadapter.SwipeRefreshHelper;
import com.android.baselibrary.rvadapter.listener.RViewCreate;

import java.util.List;

public abstract class BaseRViewActivity extends AppCompatActivity
        implements RViewCreate, SwipeRefreshHelper.SwipeRefreshLisenter {

    private RViewHelper build;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        build = new RViewHelper.Builder(this, this).build();
    }

    @Override
    public Context context() {
        return this;
    }

    @Override
    public SwipeRefreshLayout createSwipeRefresh() {
        return findViewById(R.id.swipeRefreshLayout);
    }

    @Override
    public RecyclerView createRecyclerView() {
        return findViewById(R.id.recyclerView);
    }


    @Override
    public boolean isSupportPaging() {
        return false;
    }

    protected void notifyAdapterDataSetChanged(List datas){
        build.notifyAdapterDataSetChanged(datas);
    }
}
