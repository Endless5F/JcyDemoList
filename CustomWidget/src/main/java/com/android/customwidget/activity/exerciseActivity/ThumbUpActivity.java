package com.android.customwidget.activity.exerciseActivity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.customwidget.R;
import com.android.customwidget.widget.BarChart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ThumbUpActivity extends AppCompatActivity {

    private Random mRandom;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thumb_up);
        mRandom = new Random();
        BarChart barChart = findViewById(R.id.barChart);
        barChart.setBarInfoList(createBarInfo());
    }

    private List<BarChart.BarInfo> createBarInfo() {
        List<BarChart.BarInfo> data = new ArrayList<>();

        for (int i = 1; i <= 50; ++i) {
            data.add(new BarChart.BarInfo(i + "日", mRandom.nextFloat()));
        }

        return data;
    }
}
