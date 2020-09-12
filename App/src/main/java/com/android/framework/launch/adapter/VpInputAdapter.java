package com.android.framework.launch.adapter;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class VpInputAdapter extends FragmentPagerAdapter {

    private final Context mContext;
    private final List<String> titles;
    private final List<Fragment> fragmentList;

    public VpInputAdapter(Context context, FragmentManager fragmentManager,
                          List<Fragment> dataList, List<String> titles) {
        super(fragmentManager);
        this.mContext = context;
        this.titles = titles;
        this.fragmentList = dataList;
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }
}
