package com.android.baselibrary.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.baselibrary.R;

import java.util.ArrayList;
import java.util.List;

public class WeekList extends FrameLayout implements View.OnClickListener {

    List<ItemState> itemStates = new ArrayList<>();
    private TextView one;
    private TextView two;
    private TextView three;
    private TextView four;
    private TextView five;
    private TextView six;
    private TextView seven;

    public WeekList(Context context) {
        super(context);
        initView(context);
    }

    public WeekList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public WeekList(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View inflate = View.inflate(context, R.layout.week_list, this);
        one = (TextView) inflate.findViewById(R.id.one);
        two = (TextView) inflate.findViewById(R.id.two);
        three = (TextView) inflate.findViewById(R.id.three);
        four = (TextView) inflate.findViewById(R.id.four);
        five = (TextView) inflate.findViewById(R.id.five);
        six = (TextView) inflate.findViewById(R.id.six);
        seven = (TextView) inflate.findViewById(R.id.seven);
//        int screenWidth = ScreenUtils.getScreenWidth(context);
//        int screenHeight = ScreenUtils.getScreenHeight(context);
//        int i = ScreenUtils.dp2px(context, 16);
//        int itemWidth = (screenWidth - 2 * i) / 7;
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams();
        one.setOnClickListener(this);
        two.setOnClickListener(this);
        three.setOnClickListener(this);
        four.setOnClickListener(this);
        five.setOnClickListener(this);
        six.setOnClickListener(this);
        seven.setOnClickListener(this);

        for (int i = 0; i < 7; i++) {
            itemStates.add(new ItemState(i, false));
        }
    }

    /**
     * 最终需要获取的状态串
     */
    public String getSelectStateString() {
        StringBuilder result = new StringBuilder();
        for (ItemState item : itemStates) {
            if (item.isSelect) {
                result.append(1);
            } else {
                result.append(0);
            }
        }
        if ("".equals(result.toString())) {
            return "0000000";
        } else {
            return result.toString();
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.one) {
            setItemState(itemStates.get(0), one);

        } else if (i == R.id.two) {
            setItemState(itemStates.get(1), two);

        } else if (i == R.id.three) {
            setItemState(itemStates.get(2), three);

        } else if (i == R.id.four) {
            setItemState(itemStates.get(3), four);

        } else if (i == R.id.five) {
            setItemState(itemStates.get(4), five);

        } else if (i == R.id.six) {
            setItemState(itemStates.get(5), six);

        } else if (i == R.id.seven) {
            setItemState(itemStates.get(6), seven);

        }
    }

    private void setItemState(ItemState itemState, TextView textView) {
        if (itemState.isSelect) {
            itemState.setSelect(false);
            textView.setBackgroundResource(R.drawable.oval_week_list);
        } else {
            itemState.setSelect(true);
            textView.setBackgroundResource(R.drawable.oval_week_list_select);
        }
    }

    private class ItemState {
        private int day;
        private boolean isSelect;

        ItemState(int day, boolean isSelect) {
            this.day = day;
            this.isSelect = isSelect;
        }

        public boolean isSelect() {
            return isSelect;
        }

        public void setSelect(boolean select) {
            isSelect = select;
        }
    }
}
