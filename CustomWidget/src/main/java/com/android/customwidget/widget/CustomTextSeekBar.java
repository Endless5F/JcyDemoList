package com.android.customwidget.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.customwidget.R;

public class CustomTextSeekBar extends LinearLayout {

    private TextView seekTitleView;
    private SeekBar seekBarView;
    private TextView seekStartView;
    private TextView seekEndView;
    private TextView seekProgressText;
    private String seekTitle;
    private int seekStart;
    private int seekEnd;
    private int seekMax;
    private int seekInitProgress;
    private boolean isMarkProgress;
    private float currentProgress;

    public CustomTextSeekBar(Context context) {
        super(context);
        initView();
    }

    public CustomTextSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
        readAttrs(context, attrs);
    }

    public CustomTextSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        readAttrs(context, attrs);
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.custom_text_seekbar, this, true);
    }

    private void readAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomTextSeekBar);
        seekTitle = typedArray.getString(R.styleable.CustomTextSeekBar_seekTitle);
        seekStart = typedArray.getInt(R.styleable.CustomTextSeekBar_seekStart, -50);
        seekEnd = typedArray.getInt(R.styleable.CustomTextSeekBar_seekEnd, 50);
        seekMax = typedArray.getInt(R.styleable.CustomTextSeekBar_seekMax, 100);
        seekInitProgress = typedArray.getInt(R.styleable.CustomTextSeekBar_seekInitProgress, 0);
        isMarkProgress = typedArray.getBoolean(R.styleable.CustomTextSeekBar_isMarkProgress, true);

        if (TextUtils.isEmpty(seekTitle)) {
            seekTitle = "";
        }
        typedArray.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        seekTitleView = findViewById(R.id.tv_seek_title);
        seekBarView = findViewById(R.id.seekBar);
        seekStartView = findViewById(R.id.tv_seek_start);
        seekEndView = findViewById(R.id.tv_seek_end);
        seekProgressText = findViewById(R.id.tv_mark_progress);

        seekTitleView.setText(seekTitle);
        seekStartView.setText(String.valueOf(seekStart));
        seekEndView.setText(String.valueOf(seekEnd));

        if (isMarkProgress) {
            seekProgressText.setVisibility(VISIBLE);
        } else {
            seekProgressText.setVisibility(GONE);
        }

        if (seekEnd - seekStart != seekMax) {
            throw new IllegalStateException("进度最大值与开始结束差值不符");
        }
        this.post(new Runnable() {
            @Override
            public void run() {
                setListener();
                seekBarView.setMax(seekMax);
                seekBarView.setProgress(seekInitProgress - seekStart);
            }
        });
    }

    /**
     * 监听SeekBar拖动——改变进度刻度显示位置
     */
    private void setListener() {
        seekBarView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //int to float
                currentProgress = progress;

                int textProgress = progress + seekStart;

                //设置文本显示
                seekProgressText.setText(String.valueOf(textProgress));

                float pox = getSeekProgressTextX(currentProgress);
                seekProgressText.setX(pox);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * 获取SeekBar上方刻度值X轴位置
     * */
    private float getSeekProgressTextX(float currentProgress) {
        //获取文本宽度
        float textWidth = seekProgressText.getWidth();

        // CustomTextSeekBar本控件布局根布局的左padding值
        int paddingLeft = dp2px(16);

        //获取seekbar最左端的x位置
        float left = seekBarView.getLeft() + paddingLeft;

        //进度条的刻度值
        float max = Math.abs(seekBarView.getMax());

        //这不叫thumb的宽度,叫seekbar距左边宽度,实验了一下，seekbar 不是顶格的，两头都存在一定空间，所以xml 需要用paddingStart 和 paddingEnd 来确定具体空了多少值,我这里设置15dp;
        float thumb = dp2px(12);

        //每移动1个单位，text应该变化的距离 = (seekBar的宽度 - 两头空的空间) / 总的progress长度
        float average = (((float) seekBarView.getWidth()) - 2 * thumb) / max;

        //textview 应该所处的位置 = seekbar最左端 + seekbar左端空的空间 + 当前progress应该加的长度 - textview宽度的一半(保持居中作用)
        return left - textWidth / 2 + thumb + average * currentProgress;
    }

    /**
     * 获取SeekBar上方刻度值X轴位置
     * */
    private float getSeekProgressTextX2(float currentProgress) {
        //获取文本宽度
        float textWidth = seekProgressText.getWidth();

        // CustomTextSeekBar本控件布局根布局的左padding值
        int paddingLeft = dp2px(16);

        //获取seekbar最左端的x位置
        float left = seekBarView.getLeft() + paddingLeft;

        //进度条的刻度值
        float max = Math.abs(seekBarView.getMax());

        //这不叫thumb的宽度,叫seekbar距左边宽度,实验了一下，seekbar 不是顶格的，两头都存在一定空间，所以xml 需要用paddingStart 和 paddingEnd 来确定具体空了多少值,我这里设置15dp;
        float thumb = dp2px(12);

        //每移动1个单位，text应该变化的距离 = (seekBar的宽度 - 两头空的空间) / 总的progress长度
        float average = (((float) seekBarView.getWidth()) - 2 * thumb) / max;

        //textview 应该所处的位置 = seekbar最左端 + seekbar左端空的空间 + 当前progress应该加的长度 - textview宽度的一半(保持居中作用)
        return left - textWidth / 2 + thumb + average * currentProgress;
    }

    /**
     * 设置Title
     */
    public void setSeekTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            title = "";
        }
        seekTitle = title;
        invalidate();
    }

    public void setSeekStart(int seekStart) {
        this.seekStart = seekStart;
        invalidate();
    }

    public void setSeekEnd(int seekEnd) {
        this.seekEnd = seekEnd;
        invalidate();
    }

    public void setMarkProgress(boolean markProgress) {
        isMarkProgress = markProgress;
        invalidate();
    }

    /**
     * dp转px
     *
     * @param dpValue dp值
     * @return px值
     */
    public int dp2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
