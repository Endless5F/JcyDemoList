package com.android.framework.launch.activity;

import android.os.Bundle;

import com.android.baselibrary.base.BaseToolbarCompatActivity;
import com.android.framework.R;

public class BgLibActivity extends BaseToolbarCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_library);
        setMiddleTitle("LayoutInflater Factory");
    }

    @Override
    public boolean setFactory2() {
        return true;
    }
}
