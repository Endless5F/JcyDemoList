package com.android.framework.launch.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.framework.R;
import com.android.framework.launch.data.MixedResourceBean;

import java.util.List;

public class MixedItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ITEM_TYPE_HEADER = 100000;
    private static final int ITEM_TYPE_TITLE = 111110;
    private static final int ITEM_TYPE_SECOND = 111111;
    private final Context mContext;
    private final int mSpanCount;
    private final List<MixedResourceBean> mItemData;
    private SparseArrayCompat<Integer> mHeaderViews = new SparseArrayCompat<>();

    public MixedItemAdapter(Context context, List<MixedResourceBean> list, int spanCount) {
        this.mContext = context;
        this.mItemData = list;
        this.mSpanCount = spanCount;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderViews.get(viewType) != null) {
            View v = LayoutInflater.from(mContext).inflate(mHeaderViews.get(viewType), parent,
                    false);
            HeadViewHolder headViewHolder = new HeadViewHolder(v);
            return headViewHolder;
        } else if (ITEM_TYPE_TITLE == viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.adapter_mixed_item_title,
                    parent, false);
            TitleViewHolder titleViewHolder = new TitleViewHolder(v);
            return titleViewHolder;
        } else {
            View v = LayoutInflater.from(mContext).inflate(R.layout.adapter_mixed_item_context,
                    parent, false);
            ItemViewHolder itemViewHolder = new ItemViewHolder(v);
            return itemViewHolder;
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (mHeaderViews.get(viewType) != null) {
            HeadViewHolder headViewHolder = (HeadViewHolder) holder;

            return;
        } else {
            position -= getHeadersCount();
        }
        if (viewType == ITEM_TYPE_TITLE) {
            TitleViewHolder titleViewHolder = (TitleViewHolder) holder;
            titleViewHolder.icon.setImageResource(mItemData.get(position).getTitle().getIcon());
            titleViewHolder.name.setText(mItemData.get(position).getTitle().getName());
            titleViewHolder.time.setText(mItemData.get(position).getTitle().getTime());
        } else {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            itemViewHolder.name.setText(mItemData.get(position).getResourceInfo().getThumbnailName());
            itemViewHolder.icon.setImageResource(mItemData.get(position).getResourceInfo().getThumbnail());
            itemViewHolder.icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewCompat.animate(itemViewHolder.icon)
                            .scaleX(1.1f).scaleY(1.1f).start();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return (mItemData.size() + getHeadersCount());
    }

    private boolean isTitle(int position) {
        return mItemData.get(position - getHeadersCount()).getTitle() != null;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        //如果是title就占据4个单元格(重点)
        GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = getItemViewType(position);
                if (mHeaderViews.get(viewType) != null) {
                    return mSpanCount;
                }
                if (viewType == ITEM_TYPE_TITLE) {
                    return mSpanCount;
                }
                return 1;
            }
        });
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

    class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView icon;

        @SuppressLint("WrongViewCast")
        public ItemViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_thumbnail);
            name = itemView.findViewById(R.id.iv_thumbnail_name);
        }
    }

    class TitleViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        TextView time;

        public TitleViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_item_icon);
            name = itemView.findViewById(R.id.tv_item_name);
            time = itemView.findViewById(R.id.tv_item_time);
        }
    }

    class HeadViewHolder extends RecyclerView.ViewHolder {
        TextView header;

        public HeadViewHolder(View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.tv_item_header);
        }
    }
}
