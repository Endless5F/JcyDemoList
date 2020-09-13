package com.android.customwidget.exerciseList.hencoderpracticedraw5;

import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.customwidget.BaseActivity;
import com.android.customwidget.R;
import com.android.customwidget.fragment.PageFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class DrawOrderActivity extends BaseActivity {
    TabLayout tabLayout;
    ViewPager pager;
    List<PageModel> pageModels = new ArrayList<>();

    {
        pageModels.add(new PageModel(R.layout.sample_after_on_draw, R.string.title_after_on_draw, R.layout.practice_after_on_draw));
        pageModels.add(new PageModel(R.layout.sample_before_on_draw, R.string.title_before_on_draw, R.layout.practice_before_on_draw));
        pageModels.add(new PageModel(R.layout.sample_on_draw_layout, R.string.title_on_draw_layout, R.layout.practice_on_draw_layout));
        pageModels.add(new PageModel(R.layout.sample_dispatch_draw, R.string.title_dispatch_draw, R.layout.practice_dispatch_draw));
        pageModels.add(new PageModel(R.layout.sample_after_on_draw_foreground, R.string.title_after_on_draw_foreground, R.layout.practice_after_on_draw_foreground));
        pageModels.add(new PageModel(R.layout.sample_before_on_draw_foreground, R.string.title_before_on_draw_foreground, R.layout.practice_before_on_draw_foreground));
        pageModels.add(new PageModel(R.layout.sample_after_draw, R.string.title_after_draw, R.layout.practice_after_draw));
        pageModels.add(new PageModel(R.layout.sample_before_draw, R.string.title_before_draw, R.layout.practice_before_draw));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                PageModel pageModel = pageModels.get(position);
                return PageFragment.newInstance(pageModel.sampleLayoutRes, pageModel.practiceLayoutRes);
            }

            @Override
            public int getCount() {
                return pageModels.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return getString(pageModels.get(position).titleRes);
            }
        });

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(pager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    private class PageModel {
        @LayoutRes int sampleLayoutRes;
        @StringRes int titleRes;
        @LayoutRes int practiceLayoutRes;

        PageModel(@LayoutRes int sampleLayoutRes, @StringRes int titleRes, @LayoutRes int practiceLayoutRes) {
            this.sampleLayoutRes = sampleLayoutRes;
            this.titleRes = titleRes;
            this.practiceLayoutRes = practiceLayoutRes;
        }
    }
}
