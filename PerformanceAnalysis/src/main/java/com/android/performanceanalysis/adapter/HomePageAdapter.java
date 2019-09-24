package com.android.performanceanalysis.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.performanceanalysis.R;
import com.android.performanceanalysis.data.HomeData;

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

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
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
                    switch (pos) {
                        case 1:

                            break;
                        case 2:

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
