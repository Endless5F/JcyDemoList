package com.android.baselibrary.rvadapter.base;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.android.baselibrary.rvadapter.holder.RViewHodler;
import com.android.baselibrary.rvadapter.listener.ItemListener;
import com.android.baselibrary.rvadapter.listener.RViewItem;
import com.android.baselibrary.rvadapter.manager.RViewItemManager;

import java.util.ArrayList;
import java.util.List;

// RecycleView 的Adapter
public class RViewAdapter<T> extends RecyclerView.Adapter<RViewHodler> {

    private List<T> mDatas;
    private ItemListener<T> itemListener;
    private RViewItemManager itemStyle;// 多样式，item类型管理

    // 单样式构造
    public RViewAdapter(List<T> mDatas, RViewItem<T> item) {
        if (mDatas == null) this.mDatas = new ArrayList<>();
        this.mDatas = mDatas;
        itemStyle = new RViewItemManager();

        addRViewItem(item);
    }

    // 多样式构造
    public RViewAdapter(List<T> mDatas, List<RViewItem<T>> itemList) {
        if (mDatas == null) this.mDatas = new ArrayList<>();
        this.mDatas = mDatas;
        itemStyle = new RViewItemManager();

        // 若为多样式，需要添加item到管理器中
        addRViewItemList(itemList);
    }

    private void addRViewItem(RViewItem<T> item) {
        itemStyle.addRViewItem(item);
    }

    private void addRViewItemList(List<RViewItem<T>> itemList) {
        if (itemList != null){
            for (RViewItem item : itemList) {
                itemStyle.addRViewItem(item);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        // 若为多样式则需要判断
        if (hasMultiStyle()) {
            try {
                return itemStyle.getItemViewType(mDatas.get(position), position);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RViewHodler onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        RViewItem item = itemStyle.getRViewItem(viewType);
        int itemLayout = item.getItemLayout();
        RViewHodler holder = RViewHodler.createViewHolder(viewGroup.getContext(), viewGroup, itemLayout);
        if (item.openClick()){
            setListener(holder);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RViewHodler rViewHodler, int position) {
        try {
            itemStyle.convert(rViewHodler, mDatas.get(position), position);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    // 添加数据
    public void addDatas(List<T> datas) {
        if (datas == null) return;
        this.mDatas.addAll(datas);
        notifyDataSetChanged();

    }

    // 更新数据
    public void updataDatas(List<T> datas) {
        if (datas == null) return;
        this.mDatas = datas;
        notifyDataSetChanged();

    }

    // 是否有多样式RViewItem
    private boolean hasMultiStyle(){
        return itemStyle.getItemStylesCount() > 0;
    }

    private void setListener(RViewHodler holder) {
        holder.getmContentView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (itemListener != null){
                    itemListener.onItemClick(v, mDatas.get(position), position);
                }
            }
        });
        holder.getmContentView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getAdapterPosition();
                if (itemListener != null){
                    itemListener.onItemLongClick(v, mDatas.get(position), position);
                }
                return true;// 若返回false，则上面的点击监听也会响应
            }
        });
    }

    public void setItemListener(ItemListener<T> itemListener) {
        this.itemListener = itemListener;
    }
}
