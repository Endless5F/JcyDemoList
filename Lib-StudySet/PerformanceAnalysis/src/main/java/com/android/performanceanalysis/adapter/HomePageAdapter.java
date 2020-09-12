package com.android.performanceanalysis.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.android.performanceanalysis.R;
import com.android.performanceanalysis.activity.AopDemoActivity;
import com.android.performanceanalysis.activity.WebViewDemoList;
import com.android.performanceanalysis.data.HomeData;
import com.android.performanceanalysis.utils.LaunchTimerUtil;

import java.util.List;


public class HomePageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<HomeData.ItemView> mItemData;
    private static final int ITEM_TYPE_HEADER = 100000;
    private static final int ITEM_TYPE_TITLE = 111110;
    private static final int ITEM_TYPE_SECOND = 111111;
    private SparseArrayCompat<Integer> mHeaderViews = new SparseArrayCompat<>();

    public HomePageAdapter(Context context, List<HomeData.ItemView> list) {
        this.mContext = context;
        this.mItemData = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mHeaderViews.get(viewType) != null) {
            View v = LayoutInflater.from(mContext).inflate(mHeaderViews.get(viewType), parent,
                    false);
            return new HeadViewHolder(v);
        } else if (ITEM_TYPE_TITLE == viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.activity_home_page_title,
                    parent, false);
            return new TitleViewHolder(v);
        } else {
            View v = LayoutInflater.from(mContext).inflate(R.layout.activity_home_page_item,
                    parent, false);
            return new ItemViewHolder(v);
        }
    }

    /**
     * App启动是否已经统计过
     * 此处埋点为App启动的结束埋点，只有第一条真实数据展示App才算真正启动完成
     */
    private boolean isRecorded = false;

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {
        if (position == 0 && !isRecorded) {
            // 监听 itemView 绘制之前
            viewHolder.itemView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    // 启动时间测量：结束记录
                    LaunchTimerUtil.endRecord();
                    viewHolder.itemView.getViewTreeObserver().removeOnPreDrawListener(this);
                    return true;
                }
            });
        }
        int viewType = getItemViewType(position);
        if (mHeaderViews.get(viewType) != null) {
            HeadViewHolder headViewHolder = (HeadViewHolder) viewHolder;
            headViewHolder.scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            return;
        } else {
            position -= getHeadersCount();
        }
        if (viewType == ITEM_TYPE_TITLE) {
            TitleViewHolder titleViewHolder = (TitleViewHolder) viewHolder;
            titleViewHolder.title.setText(mItemData.get(position).desc);
        } else {
            ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
            itemViewHolder.name.setText(mItemData.get(position).desc);
//            itemViewHolder.icon.setText(mItemData.get(position).icon);
            final int pos = position;
            itemViewHolder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    switch (pos) {
                        case 1:
                            intent.setClass(mContext, WebViewDemoList.class);
                            mContext.startActivity(intent);
                            break;
                        case 2:
                            intent.setClass(mContext, AopDemoActivity.class);
                            mContext.startActivity(intent);
                            break;
                        case 3:

                            break;
                        default:
                            break;
                    }

                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderViewPos(position)) {
            return mHeaderViews.keyAt(position);
        } else if (isTitle(position)) {
            return ITEM_TYPE_TITLE;
        } else {
            return ITEM_TYPE_SECOND;
        }
    }

    @Override
    public int getItemCount() {
        return mItemData.size() + getHeadersCount();
    }

    private boolean isHeaderViewPos(int position) {
        return position < getHeadersCount();
    }

    private int getHeadersCount() {
        return mHeaderViews.size();
    }

    public void addHeaderView(int view) {
        mHeaderViews.put(mHeaderViews.size() + ITEM_TYPE_HEADER, view);
    }

    public void removeHeaderView() {
        mHeaderViews.clear();
    }

    public boolean isHaveHeaderView() {
        return mHeaderViews.size() > 0;
    }

    private boolean isTitle(int position) {
        return "".equals(mItemData.get(position - getHeadersCount()).icon);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView icon;

        @SuppressLint("WrongViewCast")
        public ItemViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_item_name);
            icon = itemView.findViewById(R.id.tv_item_icon);
        }
    }

    class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public TitleViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_title);
        }
    }

    class HeadViewHolder extends RecyclerView.ViewHolder {
        TextView scan;

        public HeadViewHolder(View itemView) {
            super(itemView);
            scan = itemView.findViewById(R.id.tv_scan);
        }
    }
}
