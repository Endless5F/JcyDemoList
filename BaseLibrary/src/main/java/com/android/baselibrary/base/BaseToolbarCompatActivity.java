package com.android.baselibrary.base;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.ColorRes;
import androidx.core.view.ActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.baselibrary.R;
import com.android.baselibrary.util.ScreenUtils;
import com.android.baselibrary.util.StatusBarUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author jcy.
 * @date 2018/5/30
 * description Activity基类
 */
public abstract class BaseToolbarCompatActivity extends BaseCompatActivity {
    private static final String TAG = BaseToolbarCompatActivity.class.getSimpleName();
    HashMap<Integer, MenuItemData> menuItemDataHashMap = null;
    public Toolbar mToolbar = null;
    public LinearLayout baseView = null;
    private TextView leftText;
    private TextView middleTitle;
    private TextView rightText;
    private boolean isBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.base_activity_toolbar);
        //适配全尺寸屏幕
        //在serContentView之前调用,资源放在 xxhdpi，那么我们宽度转换为 dp 就是 1080 / 3 = 360dp
        if (ScreenUtils.isPortrait()) {
            ScreenUtils.adaptScreen4VerticalSlide(this, 360);
        } else {
            ScreenUtils.adaptScreen4HorizontalSlide(this, 360);
        }
        //统一控制状态栏颜色渐变色
        StatusBarUtils.setDrawable(this, R.drawable.shape_statusbar_toolbar_bg2);
        leftText = findViewById(R.id.toolbar_left_title);
        middleTitle = findViewById(R.id.toolbar_middle_title);
        rightText = findViewById(R.id.toolbar_right_text);
        baseView = findViewById(R.id.base_content_view);
        //setStatusBarColor(R.color.colorPrimary);
        initToolbar();
        menuItemDataHashMap = new HashMap<Integer, MenuItemData>();
    }

    @Override
    public void setContentView(int layoutId) {
        setContentView(View.inflate(this, layoutId, null));
    }

    @Override
    public void setContentView(View view) {
        if (baseView == null) {
            return;
        }
        baseView.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    /*********************************************************************************************************************************
     * 其他函数
     ********************************************************************************************************************************/

    /**
     * 初始化Toolbar
     */
    private void initToolbar() {
        mToolbar = findViewById(R.id.tool_bar);
        //暂时注释掉动态设置颜色代码
        //mToolbar.setPopupTheme(R.style.Base_ThemeOverlay_AppCompat_Light);
        //mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        //mToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mToolbar.setNavigationOnClickListener(v -> {
            if (isBack) {
                pressToolbarNavigation();
            } else {

            }
        });
        //setSupportActionBar(mToolbar);
    }

    /**
     * 设置状态栏颜色
     *
     * @param id Res
     */
    protected void setStatusBarColor(@ColorRes int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // finally change the color
            window.setStatusBarColor(getResources().getColor(id));
        } else {
        }
    }

    /**
     * 返回按钮事件
     */
    protected void pressToolbarNavigation() {
        finish();
    }

    /*********************************************************************************************************************************
     * Toolbar 相关函数
     ********************************************************************************************************************************/

    /**
     * 自定义View设置在Toolbar
     *
     * @param view 自定义View
     * @return 自定义View
     */
    protected View addCustomToolbar(View view) {
        androidx.appcompat.app.ActionBar.LayoutParams layout = new androidx.appcompat.app.ActionBar.LayoutParams(
                androidx.appcompat.app.ActionBar.LayoutParams.MATCH_PARENT,
                androidx.appcompat.app.ActionBar.LayoutParams.MATCH_PARENT);
        if (mToolbar != null) {
            mToolbar.addView(view, layout);
            mToolbar.setContentInsetsAbsolute(0, 0);
        }
        return view;
    }

    /**
     * 自定义View设置在Toolbar
     *
     * @param layoutResId 自定义View Res
     * @return 自定义View
     */
    protected View addCustomToolbar(int layoutResId) {
        if (mToolbar == null) {
            return null;
        }
        final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(layoutResId, null);
        androidx.appcompat.app.ActionBar.LayoutParams layout = new androidx.appcompat.app.ActionBar.LayoutParams(
                androidx.appcompat.app.ActionBar.LayoutParams.MATCH_PARENT,
                androidx.appcompat.app.ActionBar.LayoutParams.MATCH_PARENT);
        mToolbar.addView(actionBarLayout, layout);
        mToolbar.setContentInsetsAbsolute(0, 0);

        return actionBarLayout;
    }

    /**
     * 设置toolbar的背景色
     *
     * @param resid
     */
    protected void setCustomToolbarColor(int resid) {
        //mToolBar.setBackgroundDrawable(getResources().getDrawable(resid));废弃的接口
        mToolbar.setBackground(getResources().getDrawable(resid));
    }

    /**
     * 获取toolbar
     *
     * @return
     */
    protected Toolbar getToolBar() {
        return mToolbar;
    }

    /**
     * 设置toolbar
     *
     * @param mToolBar
     */
    protected void setToolbar(Toolbar mToolBar) {
        this.mToolbar = mToolBar;
    }

    /**
     * 隐藏toolbar
     */
    protected void hideToolbar() {
        if (mToolbar != null) {
            mToolbar.setVisibility(View.GONE);
        }
    }

    /**
     * 是否显示默认左上角返回按钮
     *
     * @param isBack 是否显示
     */
    protected void setLeftButtonIsBack(boolean isBack) {
        setLeftButtonIsBack(isBack, null);
    }

    /**
     * 是否显示默认左上角返回按钮
     *
     * @param isBack 是否显示
     */
    protected void setLeftButtonIsBack(boolean isBack, int titleId) {
        this.isBack = isBack;
        if (mToolbar != null) {
            if (isBack) {
                mToolbar.setNavigationIcon(R.drawable.base_toolbar_back);
                mToolbar.setTitle(titleId);
                mToolbar.setTitleTextColor(0xFFFFFF);
            } else {
                mToolbar.setNavigationIcon(null);
            }
        }
    }

    /**
     * 是否显示默认左上角返回按钮
     *
     * @param isBack 是否显示
     */
    protected void setLeftButtonIsBack(boolean isBack, CharSequence text) {
        this.isBack = isBack;
        if (mToolbar != null) {
            if (isBack) {
                mToolbar.setNavigationIcon(R.drawable.base_toolbar_back);
                if (text != null) {
                    mToolbar.setTitle(text);
                    mToolbar.setTitleTextColor(0xFFFFFF);
                }
            } else {
                mToolbar.setNavigationIcon(null);
            }
        }
    }

    /**
     * 设置左上返回键图标
     *
     * @param resId
     */
    protected void setLeftButtonBackResId(int resId) {
        isBack = false;
        if (mToolbar != null) {
            mToolbar.setNavigationIcon(resId);
        }
    }

    /**
     * 设置Toolbar标题
     *
     * @param title 标题
     */
    protected void setLeftText(CharSequence title) {
        if (leftText != null) {
            leftText.setText(title);
        }
    }

    protected void setLeftText(int titleId) {
        if (leftText != null) {
            leftText.setText(titleId);
        }
    }

    protected void setLeftTextOnClickLisenter(final ToolBarTextClickListener textClickListener) {
        if (leftText == null) {
            return;
        }
        if (textClickListener != null) {
            leftText.setOnClickListener(v -> textClickListener.onClick());
        }
    }

    protected void setMiddleTitle(CharSequence title) {
        //super.setTitle(title);
        if (middleTitle != null) {
            middleTitle.setText(title);
        }
    }

    protected void setMiddleTitle(int titleId) {
        //super.setTitle(title);
        if (middleTitle != null) {
            middleTitle.setText(titleId);
        }
    }

    protected void setRightText(int resId) {
        if (rightText != null) {
            rightText.setText(resId);
        }
    }

    protected void setRightText(CharSequence text) {
        if (rightText != null) {
            rightText.setText(text);
        }
    }

    protected void setRightTextOnClickListener(final ToolBarTextClickListener textClickListener) {
        if (rightText == null) {
            return;
        }
        if (textClickListener != null) {
            rightText.setOnClickListener(v -> textClickListener.onClick());
        }
    }

    public interface ToolBarTextClickListener {
        void onClick();
    }


    protected View setSubContentView(int viewId) {
        //add subview
        View contentView = getViewRootActivity().getLayoutInflater().inflate(
                viewId, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        baseView.addView(contentView, lp);
        return contentView;
    }

    protected View setSubContentView(View view) {
        //add subview
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        baseView.addView(view, lp);
        return view;
    }


    /*********************************************************************************************************************************
     * Menu 相关函数
     ********************************************************************************************************************************/

    /**
     * 此方法用于初始化菜单，其中menu参数就是即将要显示的Menu实例。 返回true则显示该menu,false 则不显示;
     * (只会在第一次初始化菜单时调用) Inflate the menu; this adds items to the action bar
     * if it is present.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 在onCreateOptionsMenu执行后，菜单被显示前调用；如果菜单已经被创建，则在菜单显示前被调用。 同样的，
     * 返回true则显示该menu,false 则不显示; （可以通过此方法动态的改变菜单的状态，比如加载不同的菜单等）
     */
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        refreshMenu(menu, false);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * 菜单项被点击时调用，也就是菜单项的监听方法。
     * 通过这几个方法，可以得知，对于Activity，同一时间只能显示和监听一个Menu 对象。
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                pressToolbarNavigation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 添加右侧按钮
     *
     * @param menuItemData 菜单项类
     *                     只能添加两个不隐藏的
     */
    protected void addRightButton(MenuItemData menuItemData) {
        if (menuItemDataHashMap == null) {
            menuItemDataHashMap = new HashMap<Integer, MenuItemData>();
        }
        menuItemDataHashMap.put(menuItemData.itemId, menuItemData);
        onMenuItemDataChanged();
    }

    /**
     * 移除MenuItem
     *
     * @param menuId 菜单ID项
     */
    protected void removeMenuItem(int menuId) {
        if (menuItemDataHashMap.containsKey(menuId)) {
            menuItemDataHashMap.remove(menuId);
            onMenuItemDataChanged();
        }
    }

    /**
     * 清空MenuItem数据
     */
    protected void clearMenu() {
        if (menuItemDataHashMap != null) {
            menuItemDataHashMap.clear();
            onMenuItemDataChanged();
        }
    }

    /**
     * 通过itemId获取MenuItemData
     *
     * @param itemId MenuItemData索引值
     * @return MenuItemData
     */
    protected MenuItemData getMenuItemData(int itemId) {
        MenuItemData menuItemData = null;
        if (menuItemDataHashMap != null) {
            menuItemData = menuItemDataHashMap.get(itemId);
        }
        return menuItemData;
    }

    /**
     * 刷新按钮
     */
    public void onMenuItemDataChanged() {
        try {
            refreshMenu(mToolbar.getMenu(), true);
        } catch (Throwable h) {

        }
    }

    /**
     * 刷新菜单
     *
     * @param menu          菜单
     * @param isToolbarMenu 是不是Toolbar菜单
     */
    private void refreshMenu(Menu menu, boolean isToolbarMenu) {
        menu.clear();
        if (menuItemDataHashMap == null || menuItemDataHashMap.isEmpty()) {
            return;
        }
        Iterator iterator = menuItemDataHashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            final MenuItemData itemData = (MenuItemData) entry.getValue();
            initMenuItemData(menu, itemData, isToolbarMenu);
        }
    }

    /**
     * 初始化MenuItem
     *
     * @param menu          菜单
     * @param itemData      MenuItemData 菜单项
     * @param isToolBarMenu 是不是Toolbar菜单
     */
    private void initMenuItemData(Menu menu, final MenuItemData itemData, boolean isToolBarMenu) {
        if (!isToolBarMenu && itemData.showAction == MenuItemData.SHOW_ACTION_AT_TITLE) {
            return;
        }
        final MenuItem menuItem = menu.add(0, itemData.itemId,
                itemData.itemId, itemData.titleResId);
        if (itemData.drawableResId > 0) {
            menuItem.setIcon(itemData.drawableResId);
        }
        if (itemData.actionProvider != null) {
            MenuItemCompat.setActionProvider(menuItem, itemData.actionProvider);
        }
        if (itemData.showAction == MenuItemData.SHOW_ACTION_AT_TITLE) {
            MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        menuItem.setOnMenuItemClickListener(item -> {
            if (itemData.listener != null) {
                itemData.listener.onClick();
            }
            return true;
        });
    }

    /**
     * 获取菜单列表
     *
     * @return HashMap<Integer                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               ,                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               MenuItemData> MenuList
     */
    protected HashMap<Integer, MenuItemData> getMenuList() {
        return menuItemDataHashMap;
    }

    /**
     * MenuItemData类
     * Toolbar右侧按钮类
     */
    public class MenuItemData {
        public static final int SHOW_ACTION_AT_TITLE = 0;
        public static final int SHOW_ACTION_HIDE_IN_MENU = 1;

        public MenuItemData(int itemId, int titleResId, int drawableResId,
                            RightBtnClickListener listener) {
            this.itemId = itemId;
            this.titleResId = titleResId;
            this.drawableResId = drawableResId;
            this.listener = listener;
            this.showAction = SHOW_ACTION_AT_TITLE;
        }

        public MenuItemData(int itemId, int titleResId, int drawableResId,
                            int showAction, RightBtnClickListener listener) {
            this.itemId = itemId;
            this.titleResId = titleResId;
            this.drawableResId = drawableResId;
            this.listener = listener;
            this.showAction = showAction;
        }

        public MenuItemData(int itemId, int titleResId, int drawableResId,
                            int showAction, ActionProvider actionProvider) {
            this.itemId = itemId;
            this.titleResId = titleResId;
            this.drawableResId = drawableResId;
            this.showAction = showAction;
            this.actionProvider = actionProvider;
        }

        public MenuItemData(int itemId, int titleResId, int drawableResId,
                            int showAction, boolean isExpand) {
            this.itemId = itemId;
            this.titleResId = titleResId;
            this.drawableResId = drawableResId;
            this.showAction = showAction;
            this.isExpand = isExpand;
        }

        public int itemId;//唯一ID,给actionbar的按钮排序用
        public int titleResId;//标题的ResId
        public int drawableResId;//图标的ResId
        public int showAction;//是否显示再标题栏 SHOW_ACTION_AT_TITLE 显示 , SHOW_ACTION_HIDE_IN_MENU 隐藏在列表里
        public boolean isExpand;//如果是搜索框，是否展开
        public ActionProvider actionProvider;
        RightBtnClickListener listener;//menu的点击事件
    }

    /**
     * 右侧按钮点击监听接口
     */
    public interface RightBtnClickListener {
        void onClick();
    }
}