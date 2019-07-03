package com.android.baselibrary;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.baselibrary.base.BaseRViewActivity;
import com.android.baselibrary.rvadapter.base.RViewAdapter;
import com.android.baselibrary.rvadapter.holder.RViewHodler;
import com.android.baselibrary.rvadapter.listener.ItemListener;
import com.android.baselibrary.rvadapter.listener.RViewItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseRViewActivity {

    private List<String> datas = new ArrayList<>();
    private RViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycleview);

        initDatas();
        initListener();
    }

    private void initDatas() {
        if (datas.isEmpty()) {
            for (int i = 0; i < 100; i++) {
                datas.add("item" + i);
            }
        }
    }

    private void initListener() {
        adapter.setItemListener(new ItemListener() {
            @Override
            public void onItemClick(View view, Object entity, int postion) {

            }

            @Override
            public void onItemLongClick(View view, Object entity, int postion) {

            }
        });
        notifyAdapterDataSetChanged(datas);
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public RViewAdapter createRecyclerViewAdapter() {
        // 单样式示例
        adapter = new RViewAdapter<>(datas, new RViewItem<String>() {

            @Override
            public int getItemLayout() {
                return R.layout.item_list;
            }

            @Override
            public boolean openClick() {
                return true;
            }

            @Override
            public boolean isItemView(String entity, int position) {
                return true;
            }

            @Override
            public void convert(RViewHodler hodler, String entity, int position) {
                TextView view = (TextView) hodler.getView(R.id.itemTv);
                view.setText(entity);
            }
        });
        return adapter;
    }
}
