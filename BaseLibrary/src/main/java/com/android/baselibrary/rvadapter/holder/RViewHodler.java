package com.android.baselibrary.rvadapter.holder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

final public class RViewHodler extends RecyclerView.ViewHolder {

    private View mContentView;// 当前View
    private SparseArray<View> mViews;// 当前布局中的控件集合

    public RViewHodler(@NonNull View itemView) {
        super(itemView);
        mContentView = itemView;
        mViews = new SparseArray<>();
    }

    public static RViewHodler createViewHolder(Context context, ViewGroup parent, int layoutId) {
        View itemView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new RViewHodler(itemView);
    }

    // 对外提供条目点击
    public View getmContentView() {
        return mContentView;
    }

    // 获取某个具体控件
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mContentView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }
}
