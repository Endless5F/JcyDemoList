package com.android.baselibrary.rvadapter.listener;

import android.view.View;

public interface ItemListener<T> {

    void onItemClick(View view, T entity, int postion);

    void onItemLongClick(View view, T entity, int postion);
}
