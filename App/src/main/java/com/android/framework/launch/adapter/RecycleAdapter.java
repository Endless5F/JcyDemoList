package com.android.framework.launch.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.framework.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHodler> {

    private final Context mContext;
    private final List<String> dataList;
    private int selectPosition;
    private OnClickItemLisenter onClickItemLisenter;

    public RecycleAdapter(Context context, List<String> dataList) {
        this.mContext = context;
        this.dataList = dataList;
    }

    class ViewHodler extends RecyclerView.ViewHolder {

        private final ImageView recyclerIcon;

        ViewHodler(@NonNull View itemView) {
            super(itemView);
            recyclerIcon = itemView.findViewById(R.id.recycleIcon);
        }
    }

    @NonNull
    @Override
    public ViewHodler onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = View.inflate(mContext, R.layout.activity_recycle_item, null);
        return new ViewHodler(view);
    }

    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(@NonNull ViewHodler viewHodler,
                                 @SuppressLint("RecyclerView") int position) {
        if (position == selectPosition) {
            viewHodler.recyclerIcon.setBackground(mContext.getDrawable(R.drawable.rect_blue_stroke));
        } else {
            viewHodler.recyclerIcon.setBackground(null);
        }
        Glide.with(mContext)
                .load(dataList.get(position))
                .into(viewHodler.recyclerIcon);

        viewHodler.recyclerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickItemLisenter.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void setBackground(int position) {
        this.selectPosition = position;
        notifyDataSetChanged();
    }

    public abstract static class OnClickItemLisenter {
        public abstract void onClick(int position);
    }

    public void setOnClick(OnClickItemLisenter onClickItemLisenter) {
        this.onClickItemLisenter = onClickItemLisenter;
    }
}
