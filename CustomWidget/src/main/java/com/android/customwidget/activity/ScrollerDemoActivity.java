package com.android.customwidget.activity;

import android.os.Bundle;
import android.view.View;

import com.android.customwidget.BaseActivity;
import com.android.customwidget.R;
import com.android.customwidget.widget.CustomScrollerView;

public class ScrollerDemoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroller_demo);
        final CustomScrollerView csv_scroll = findViewById(R.id.csv_scroll);
        csv_scroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                csv_scroll.smoothScrollTo(100,200);
            }
        });
    }
}
