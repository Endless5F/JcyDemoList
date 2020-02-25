package com.android.baselibrary.widget.decoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

/**
 * LinearLayoutManager：分割线可以使用系统提供的DividerItemDecoration
 * GridLayoutManager：
 */
public class CustomDividerItemDecoration extends RecyclerView.ItemDecoration {
    int spanCount = 0;
    private Paint mPaint;
    private int mDividerWidth;
    private Context mContext;

    public CustomDividerItemDecoration(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int top = px2dp(8);
        int left = px2dp(8);
        int right = px2dp(8);
        int bottom = px2dp(8);

        int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        spanCount = getSpanCount(parent);
        int childCount = parent.getAdapter().getItemCount();

        if (isFirstRow(parent, itemPosition, spanCount, childCount))
            //第一行顶部是否需要间隔
            top = px2dp(5);
        if (isLastRow(parent, itemPosition, spanCount, childCount)) {
            //最后一行是否需要间隔
            bottom = px2dp(5);
        }
        if (isFirstColumn(parent, itemPosition, spanCount, childCount)) {
            left = 0;
        }
        if (isLastColumn(parent, itemPosition, spanCount, childCount)) {
            right = 0;
        }

        outRect.set(left, top, right, bottom);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        draw(c, parent);
    }

    //绘制item分割线
    private void draw(Canvas canvas, RecyclerView parent) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams =
                    (RecyclerView.LayoutParams) child.getLayoutParams();

            //画水平分隔线
            int left = child.getLeft();
            int right = child.getRight();
            int top = child.getBottom() + layoutParams.bottomMargin;
            int bottom = top + mDividerWidth;
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
            //画垂直分割线
            top = child.getTop();
            bottom = child.getBottom() + mDividerWidth;
            left = child.getRight() + layoutParams.rightMargin;
            right = left + mDividerWidth;
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }

    /**
     * 判读是否是第一列
     *
     * @param parent
     * @param pos
     * @param spanCount
     * @param childCount
     * @return
     */
    private boolean isFirstColumn(RecyclerView parent, int pos, int spanCount, int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            if (pos % spanCount == 0) {
                return true;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if (pos % spanCount == 0) {// 第一列
                    return true;
                }
            } else {

            }
        }
        return false;
    }

    /**
     * 判断是否是最后一列
     *
     * @param parent
     * @param pos
     * @param spanCount
     * @param childCount
     * @return
     */
    private boolean isLastColumn(RecyclerView parent, int pos, int spanCount, int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            if ((pos + 1) % spanCount == 0) {// 如果是最后一列，则不需要绘制右边
                return true;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if ((pos + 1) % spanCount == 0) {// 最后一列
                    return true;
                }
            } else {

            }
        }
        return false;
    }

    /**
     * 判读是否是最后一行
     *
     * @param parent
     * @param pos
     * @param spanCount
     * @param childCount
     * @return
     */
    private boolean isLastRow(RecyclerView parent, int pos, int spanCount, int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            int lines = childCount % spanCount == 0 ? childCount / spanCount :
                    childCount / spanCount + 1;
            return lines == pos / spanCount + 1;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {

        }
        return false;
    }

    /**
     * 判断是否是第一行
     *
     * @param parent
     * @param pos
     * @param spanCount
     * @param childCount
     * @return
     */
    private boolean isFirstRow(RecyclerView parent, int pos, int spanCount, int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            if ((pos / spanCount + 1) == 1) {
                return true;
            } else {
                return false;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {

        }
        return false;
    }

    /**
     * 获取列数
     *
     * @param parent
     * @return
     */
    private int getSpanCount(RecyclerView parent) {
        int spanCount = -1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            spanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }
        return spanCount;
    }

    /**
     * px转dp
     *
     * @param pxValue px值
     * @return dp值
     */
    public int px2dp(float pxValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}