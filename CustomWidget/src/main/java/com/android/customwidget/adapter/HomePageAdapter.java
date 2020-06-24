package com.android.customwidget.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.customwidget.R;
import com.android.customwidget.activity.ScrollerDemoActivity;
import com.android.customwidget.activity.exerciseActivity.ThumbUpActivity;
import com.android.customwidget.data.HomeData;
import com.android.customwidget.exerciseList.hencoderpracticedraw1.DrawBasicActivity;
import com.android.customwidget.exerciseList.hencoderpracticedraw2.PaintActivity;
import com.android.customwidget.exerciseList.hencoderpracticedraw3.DrawTextActivity;
import com.android.customwidget.exerciseList.hencoderpracticedraw4.ClipAndMatrixActivity;
import com.android.customwidget.exerciseList.hencoderpracticedraw5.DrawOrderActivity;
import com.android.customwidget.exerciseList.hencoderpracticedraw6.Animation1Activity;
import com.android.customwidget.exerciseList.hencoderpracticedraw7.Animation2Activity;
import com.android.customwidget.exerciseList.hencoderpracticelayout1.LayoutBasicActivity;
import com.android.customwidget.kotlin.activity.LinkageNavigationActivity;
import com.android.customwidget.kotlin.activity.ScrollRecycleViewActivity;

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
                            Intent intent1 = new Intent(mContext, DrawBasicActivity.class);
                            mContext.startActivity(intent1);
                            break;
                        case 2:
                            Intent intent2 = new Intent(mContext, PaintActivity.class);
                            mContext.startActivity(intent2);
                            break;
                        case 3:
                            Intent intent3 = new Intent(mContext, DrawTextActivity.class);
                            mContext.startActivity(intent3);
                            break;
                        case 4:
                            Intent intent4 = new Intent(mContext, ClipAndMatrixActivity.class);
                            mContext.startActivity(intent4);
                            break;
                        case 5:
                            Intent intent5 = new Intent(mContext, DrawOrderActivity.class);
                            mContext.startActivity(intent5);
                            break;
                        case 6:
                            Intent intent6 = new Intent(mContext, Animation1Activity.class);
                            mContext.startActivity(intent6);
                            break;
                        case 7:
                            Intent intent7 = new Intent(mContext, Animation2Activity.class);
                            mContext.startActivity(intent7);
                            break;
                        case 8:
                            Intent intent8 = new Intent(mContext, LayoutBasicActivity.class);
                            mContext.startActivity(intent8);
                            break;
                        case 9:
                            Intent intent9 = new Intent(mContext, ThumbUpActivity.class);
                            mContext.startActivity(intent9);
                            break;
                        case 10:
                            Intent intent10 = new Intent(mContext, ScrollRecycleViewActivity.class);
                            mContext.startActivity(intent10);
                            break;
                        case 11:
                            Intent intent11 = new Intent(mContext, ScrollerDemoActivity.class);
                            mContext.startActivity(intent11);
                            break;
                        case 12:
                            Intent intent12 = new Intent(mContext, LinkageNavigationActivity.class);
                            mContext.startActivity(intent12);
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
