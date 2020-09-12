package com.android.framework.launch.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

public class VpAdapter extends PagerAdapter {

    private final Context mContext;
    private final List<String> dataList;

    public VpAdapter(Context context, List<String> dataList) {
        this.mContext = context;
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ImageView imageView = (ImageView) object;
        if (imageView == null) {
            return;
        }
//        Glide.get(mContext).clearDiskCache();
        container.removeView(imageView);
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(mContext);
        ViewGroup.LayoutParams layoutParams =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(layoutParams);
        container.addView(imageView);
        Glide.with(mContext)
                .load(dataList.get(position))
                .into(imageView);
        return imageView;
    }
}
