package com.android.framework.launch.activity;

import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.baselibrary.base.BaseToolbarCompatActivity;
import com.android.baselibrary.utils.ThreadPoolUtils;
import com.android.framework.R;
import com.android.framework.launch.adapter.MixedItemAdapter;
import com.android.framework.launch.data.MixedResourceBean;

import java.util.ArrayList;
import java.util.List;

public class MixedItemActivity extends BaseToolbarCompatActivity {
    private  List<MixedResourceBean> resourceBeans = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mixed_item);
        setMiddleTitle("混合Item单层RecycleView");
        RecyclerView rlMixed = findViewById(R.id.rl_mixed);
        int spanCount = 3;
        final GridLayoutManager manager = new GridLayoutManager(this, spanCount);
        rlMixed.setLayoutManager(manager);
        MixedItemAdapter itemAdapter = new MixedItemAdapter(this, resourceBeans, spanCount);
        itemAdapter.addHeaderView(R.layout.adapter_mixed_item_header);
        rlMixed.setAdapter(itemAdapter);

        ThreadPoolUtils.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                initData();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        itemAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void initData() {
        for (int i = 0; i < 10; i++) {
            MixedResourceBean titleBean = new MixedResourceBean();
            MixedResourceBean.Title title = new MixedResourceBean.Title();
            title.setIcon(R.mipmap.ic_launcher);
            title.setName("条目 " + i);
            title.setTime(System.currentTimeMillis() + "");
            titleBean.setTitle(title);
            resourceBeans.add(titleBean);

            if (i == 0) {
                for (int j = 0; j < 10; j++) {
                    MixedResourceBean resourceBean = new MixedResourceBean();
                    MixedResourceBean.ResourceInfo resourceInfo = new MixedResourceBean.ResourceInfo();
                    resourceInfo.setThumbnail(R.mipmap.ic_launcher);
                    resourceInfo.setThumbnailName("缩略图" + i);
                    resourceBean.setResourceInfo(resourceInfo);
                    resourceBeans.add(resourceBean);
                }
            } else if (i == 1) {
                for (int j = 0; j < 8; j++) {
                    MixedResourceBean resourceBean = new MixedResourceBean();
                    MixedResourceBean.ResourceInfo resourceInfo = new MixedResourceBean.ResourceInfo();
                    resourceInfo.setThumbnail(R.mipmap.ic_launcher);
                    resourceInfo.setThumbnailName("缩略图" + i);
                    resourceBean.setResourceInfo(resourceInfo);
                    resourceBeans.add(resourceBean);
                }
            } else {
                for (int j = 0; j < 5; j++) {
                    MixedResourceBean resourceBean = new MixedResourceBean();
                    MixedResourceBean.ResourceInfo resourceInfo = new MixedResourceBean.ResourceInfo();
                    resourceInfo.setThumbnail(R.mipmap.ic_launcher);
                    resourceInfo.setThumbnailName("缩略图" + i);
                    resourceBean.setResourceInfo(resourceInfo);
                    resourceBeans.add(resourceBean);
                }
            }
        }
    }
}
