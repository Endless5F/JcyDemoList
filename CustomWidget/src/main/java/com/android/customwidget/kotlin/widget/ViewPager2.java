package com.android.customwidget.kotlin.widget;

/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static android.support.v7.util.DiffUtil.DiffResult.NO_POSITION;
import static java.lang.annotation.RetentionPolicy.CLASS;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.customwidget.R;
import com.android.customwidget.kotlin.widget.linkage.CustomLinearLayoutManager;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.List;

/**
 * Work in progress: go/viewpager2
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class ViewPager2 extends ViewGroup {
    // reused in layout(...)
    private final Rect mTmpContainerRect = new Rect();
    private final Rect mTmpChildRect = new Rect();

    int mCurrentItem;
    ScrollEventAdapter mScrollEventAdapter;
    RecyclerView mRecyclerView;
    private int mPendingCurrentItem = NO_POSITION;
    private LinearLayoutManager mLayoutManager;
    private Parcelable mPendingAdapterState;

    public ViewPager2(Context context) {
        super(context);
        initialize(context, null);
    }

    public ViewPager2(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public ViewPager2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    @RequiresApi(21)
    public ViewPager2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        // TODO(b/70663531): handle attrs, defStyleAttr, defStyleRes
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        mRecyclerView = new RecyclerView(context);
        mRecyclerView.setId(ViewCompat.generateViewId());

        mLayoutManager = new CustomLinearLayoutManager(context);
        ((CustomLinearLayoutManager)mLayoutManager).setScrollEnabled(false);
//        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        setOrientation(context, attrs);

        mScrollEventAdapter = new ScrollEventAdapter(this);
        mRecyclerView.setLayoutParams(
                new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // TODO(b/70666992): add automated test for orientation change
        new PagerSnapHelper().attachToRecyclerView(mRecyclerView);

        attachViewToParent(mRecyclerView, 0, mRecyclerView.getLayoutParams());
    }

    private void setOrientation(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewPager2);
        try {
            setOrientation(
                    a.getInt(R.styleable.ViewPager2_android_orientation, Orientation.HORIZONTAL));
        } finally {
            a.recycle();
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
//        Parcelable superState = super.onSaveInstanceState();
//        SavedState ss = new SavedState(superState);
//
//        ss.mRecyclerViewId = mRecyclerView.getId();
//
//        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
//        if (adapter instanceof FragmentStateAdapter) {
//            ss.mAdapterState = ((FragmentStateAdapter) adapter).saveState();
//        }
//
//        return ss;

        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.mRecyclerViewId = mRecyclerView.getId();
        ss.mCurrentItem = mPendingCurrentItem == NO_POSITION ? mCurrentItem : mPendingCurrentItem;

        if (mPendingAdapterState != null) {
            ss.mAdapterState = mPendingAdapterState;
        } else {
            RecyclerView.Adapter<?> adapter = mRecyclerView.getAdapter();
            if (adapter instanceof StatefulAdapter) {
                ss.mAdapterState = ((StatefulAdapter) adapter).saveState();
            }
        }

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        if (ss.mAdapterState != null) {
            RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
            if (adapter instanceof FragmentStateAdapter) {
                ((FragmentStateAdapter) adapter).restoreState(new Parcelable[]{ss.mAdapterState});
            }
        }
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        // RecyclerView changed an id, so we need to reflect that in the saved state
        Parcelable state = container.get(getId());
        if (state instanceof SavedState) {
            final int previousRvId = ((SavedState) state).mRecyclerViewId;
            final int currentRvId = mRecyclerView.getId();
            container.put(currentRvId, container.get(previousRvId));
            container.remove(previousRvId);
        }

        super.dispatchRestoreInstanceState(container);

        // State of ViewPager2 and its child (RecyclerView) has been restored now
        restorePendingState();
    }

    static class SavedState extends BaseSavedState {
        int mRecyclerViewId;
        int mCurrentItem;
        Parcelable mAdapterState;

        @RequiresApi(24)
        SavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
            readValues(source, loader);
        }

        SavedState(Parcel source) {
            super(source);
            readValues(source, null);
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        private void readValues(Parcel source, ClassLoader loader) {
            mRecyclerViewId = source.readInt();
            mCurrentItem = source.readInt();
            mAdapterState = source.readParcelable(loader);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mRecyclerViewId);
            out.writeInt(mCurrentItem);
            out.writeParcelable(mAdapterState, flags);
        }

        public static final Creator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return Build.VERSION.SDK_INT >= 24
                        ? new SavedState(source, loader)
                        : new SavedState(source);
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
                return createFromParcel(source, null);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    /**
     * TODO(b/70663708): decide on an Adapter class. Here supporting RecyclerView.Adapter.
     *
     * @see RecyclerView# setAdapter(adapter)
     */
    public <VH extends RecyclerView.ViewHolder> void setAdapter(final RecyclerView.Adapter<VH> adapter) {
        mCurrentItem = 0;
        restorePendingState();
        mRecyclerView.setAdapter(new RecyclerView.Adapter<VH>() {
            private final RecyclerView.Adapter<VH> mAdapter = adapter;

            @NonNull
            @Override
            public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                VH viewHolder = mAdapter.onCreateViewHolder(parent, viewType);

                LayoutParams layoutParams = viewHolder.itemView.getLayoutParams();
                if (layoutParams.width != LayoutParams.MATCH_PARENT
                        || layoutParams.height != LayoutParams.MATCH_PARENT) {
                    // TODO(b/70666614): decide if throw an exception or wrap in FrameLayout
                    // ourselves; consider accepting exact size equal to parent's exact size
                    throw new IllegalStateException(String.format(
                            "Item's root view must fill the whole %s (use match_parent)",
                            ViewPager2.this.getClass().getSimpleName()));
                }

                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull VH holder, int position) {
                if (mCurrentItem != position) {
                    mCurrentItem = position;
                }
                mAdapter.onBindViewHolder(holder, position);
            }

            @Override
            public int getItemCount() {
                return mAdapter.getItemCount();
            }
        });
    }

    /**
     * TODO(b/70663708): decide on an Adapter class. Here supporting {@link Fragment}s.
     *
     * @param fragmentRetentionPolicy allows for future parameterization of Fragment memory
     *                                strategy, similar to what @link FragmentPagerAdapter and
     *                                @link FragmentStatePagerAdapter provide.
     */
    public void setAdapter(FragmentManager fragmentManager, FragmentProvider fragmentProvider,
                           @FragmentRetentionPolicy int fragmentRetentionPolicy) {
        if (fragmentRetentionPolicy != FragmentRetentionPolicy.SAVE_STATE) {
            throw new IllegalArgumentException("Currently only SAVE_STATE policy is supported");
        }

        mRecyclerView.setAdapter(new FragmentStateAdapter(fragmentManager, fragmentProvider));
    }

    /**
     * Similar in behavior to @link FragmentStatePagerAdapter
     * <p>
     * Lifecycle within {@link RecyclerView}:
     * <ul>
     * <li>{@link RecyclerView.ViewHolder} initially an empty {@link FrameLayout}, serves as a
     * re-usable container for a {@link Fragment} in later stages.
     * <li>{@link RecyclerView.Adapter#onBindViewHolder} we ask for a {@link Fragment} for the
     * position. If we already have the fragment, or have previously saved its state, we use those.
     * <li> @link RecyclerView.Adapter#onAttachedToWindow we attach the {@link Fragment} to a
     * container.
     * <li>{@link RecyclerView.Adapter#onViewRecycled} and
     * {@link RecyclerView.Adapter#onFailedToRecycleView} we remove, save state, destroy the
     * {@link Fragment}.
     * </ul>
     */
    private static class FragmentStateAdapter extends RecyclerView.Adapter<FragmentViewHolder> {
        private final List<Fragment> mFragments = new ArrayList<>();

        private final List<Fragment.SavedState> mSavedStates = new ArrayList<>();
        // TODO: handle current item's menuVisibility userVisibleHint as FragmentStatePagerAdapter

        private final FragmentManager mFragmentManager;
        private final FragmentProvider mFragmentProvider;

        private FragmentStateAdapter(FragmentManager fragmentManager,
                                     FragmentProvider fragmentProvider) {
            this.mFragmentManager = fragmentManager;
            this.mFragmentProvider = fragmentProvider;
        }

        @NonNull
        @Override
        public FragmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return FragmentViewHolder.create(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull FragmentViewHolder holder, int position) {
            if (ViewCompat.isAttachedToWindow(holder.getContainer())) {
                // this should never happen; if it does, it breaks our assumption that attaching
                // a Fragment can reliably happen inside onViewAttachedToWindow
                throw new IllegalStateException(
                        String.format("View %s unexpectedly attached to a window.",
                                holder.getContainer()));
            }

            holder.mFragment = getFragment(position);
        }

        private Fragment getFragment(int position) {
            Fragment fragment = mFragmentProvider.getItem(position);
            if (mSavedStates.size() > position) {
                Fragment.SavedState savedState = mSavedStates.get(position);
                if (savedState != null) {
                    fragment.setInitialSavedState(savedState);
                }
            }
            while (mFragments.size() <= position) {
                mFragments.add(null);
            }
            mFragments.set(position, fragment);
            return fragment;
        }

        @Override
        public void onViewAttachedToWindow(@NonNull FragmentViewHolder holder) {
            if (holder.mFragment.isAdded()) {
                return;
            }
            mFragmentManager.beginTransaction().add(holder.getContainer().getId(),
                    holder.mFragment).commitNowAllowingStateLoss();
        }

        @Override
        public int getItemCount() {
            return mFragmentProvider.getCount();
        }

        @Override
        public void onViewRecycled(@NonNull FragmentViewHolder holder) {
            removeFragment(holder);
        }

        @Override
        public boolean onFailedToRecycleView(@NonNull FragmentViewHolder holder) {
            // This happens when a ViewHolder is in a transient state (e.g. during custom
            // animation). We don't have sufficient information on how to clear up what lead to
            // the transient state, so we are throwing away the ViewHolder to stay on the
            // conservative side.
            removeFragment(holder);
            return false; // don't recycle the view
        }

        private void removeFragment(@NonNull FragmentViewHolder holder) {
            removeFragment(holder.mFragment, holder.getAdapterPosition());
            holder.mFragment = null;
        }

        /**
         * Removes a Fragment and commits the operation.
         */
        private void removeFragment(Fragment fragment, int position) {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            removeFragment(fragment, position, fragmentTransaction);
            fragmentTransaction.commitNowAllowingStateLoss();
        }

        /**
         * Adds a remove operation to the transaction, but does not commit.
         */
        private void removeFragment(Fragment fragment, int position,
                                    @NonNull FragmentTransaction fragmentTransaction) {
            if (fragment == null) {
                return;
            }

            if (fragment.isAdded()) {
                while (mSavedStates.size() <= position) {
                    mSavedStates.add(null);
                }
                mSavedStates.set(position, mFragmentManager.saveFragmentInstanceState(fragment));
            }

            mFragments.set(position, null);
            fragmentTransaction.remove(fragment);
        }

        @Nullable
        Parcelable[] saveState() {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            for (int i = 0; i < mFragments.size(); i++) {
                removeFragment(mFragments.get(i), i, fragmentTransaction);
            }
            fragmentTransaction.commitNowAllowingStateLoss();
            return mSavedStates.toArray(new Fragment.SavedState[mSavedStates.size()]);
        }

        void restoreState(@NonNull Parcelable[] savedStates) {
            for (Parcelable savedState : savedStates) {
                mSavedStates.add((Fragment.SavedState) savedState);
            }
        }
    }

    private static class FragmentViewHolder extends RecyclerView.ViewHolder {
        private Fragment mFragment;

        private FragmentViewHolder(FrameLayout container) {
            super(container);
        }

        static FragmentViewHolder create(ViewGroup parent) {
            FrameLayout container = new FrameLayout(parent.getContext());
            container.setLayoutParams(
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
            container.setId(ViewCompat.generateViewId());
            return new FragmentViewHolder(container);
        }

        FrameLayout getContainer() {
            return (FrameLayout) itemView;
        }
    }

    /**
     * Provides {@link Fragment}s for pages
     */
    public interface FragmentProvider {
        /**
         * Return the Fragment associated with a specified position.
         */
        Fragment getItem(int position);

        /**
         * Return the number of pages available.
         */
        int getCount();
    }

    @Retention(CLASS)
    @IntDef({FragmentRetentionPolicy.SAVE_STATE})
    public @interface FragmentRetentionPolicy {
        /** Approach similar to @link FragmentStatePagerAdapter */
        int SAVE_STATE = 0;
    }

    @Override
    public void onViewAdded(View child) {
        // TODO(b/70666620): consider adding a support for Decor views
        throw new IllegalStateException(
                getClass().getSimpleName() + " does not support direct child views");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO(b/70666622): consider margin support
        // TODO(b/70666626): consider delegating all this to RecyclerView
        // TODO(b/70666625): write automated tests for this

        measureChild(mRecyclerView, widthMeasureSpec, heightMeasureSpec);
        int width = mRecyclerView.getMeasuredWidth();
        int height = mRecyclerView.getMeasuredHeight();
        int childState = mRecyclerView.getMeasuredState();

        width += getPaddingLeft() + getPaddingRight();
        height += getPaddingTop() + getPaddingBottom();

        width = Math.max(width, getSuggestedMinimumWidth());
        height = Math.max(height, getSuggestedMinimumHeight());

        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, childState),
                resolveSizeAndState(height, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = mRecyclerView.getMeasuredWidth();
        int height = mRecyclerView.getMeasuredHeight();

        // TODO(b/70666626): consider delegating padding handling to the RecyclerView to avoid
        // an unnatural page transition effect: http://shortn/_Vnug3yZpQT
        mTmpContainerRect.left = getPaddingLeft();
        mTmpContainerRect.right = r - l - getPaddingRight();
        mTmpContainerRect.top = getPaddingTop();
        mTmpContainerRect.bottom = b - t - getPaddingBottom();

        Gravity.apply(Gravity.TOP | Gravity.START, width, height, mTmpContainerRect, mTmpChildRect);
        mRecyclerView.layout(mTmpChildRect.left, mTmpChildRect.top, mTmpChildRect.right,
                mTmpChildRect.bottom);
    }

    @Retention(CLASS)
    @IntDef({Orientation.HORIZONTAL, Orientation.VERTICAL})
    public @interface Orientation {
        int HORIZONTAL = RecyclerView.HORIZONTAL;
        int VERTICAL = RecyclerView.VERTICAL;
    }

//    @Override
//    protected void onRestoreInstanceState(Parcelable state) {
//        if (!(state instanceof SavedState)) {
//            super.onRestoreInstanceState(state);
//            return;
//        }
//
//        SavedState ss = (SavedState) state;
//        super.onRestoreInstanceState(ss.getSuperState());
//        mPendingCurrentItem = ss.mCurrentItem;
//        mPendingAdapterState = ss.mAdapterState;
//    }

    private void restorePendingState() {
        if (mPendingCurrentItem == NO_POSITION) {
            // No state to restore, or state is already restored
            return;
        }
        RecyclerView.Adapter<?> adapter = getAdapter();
        if (adapter == null) {
            return;
        }
        if (mPendingAdapterState != null) {
            if (adapter instanceof StatefulAdapter) {
                ((StatefulAdapter) adapter).restoreState(mPendingAdapterState);
            }
            mPendingAdapterState = null;
        }
        // Now we have an adapter, we can clamp the pending current item and set it
        mCurrentItem = Math.max(0, Math.min(mPendingCurrentItem, adapter.getItemCount() - 1));
        mPendingCurrentItem = NO_POSITION;
        mRecyclerView.scrollToPosition(mCurrentItem);
//        mAccessibilityProvider.onRestorePendingState();
    }

    public void setCurrentItemInternal(int item, boolean smoothScroll) {

        // 1. Preprocessing (check state, validate item, decide if update is necessary, etc)

        RecyclerView.Adapter<?> adapter = getAdapter();
        if (adapter == null) {
            // Update the pending current item if we're still waiting for the adapter
            if (mPendingCurrentItem != NO_POSITION) {
                mPendingCurrentItem = Math.max(item, 0);
            }
            return;
        }
        if (adapter.getItemCount() <= 0) {
            // Adapter is empty
            return;
        }
        item = Math.max(item, 0);
        item = Math.min(item, adapter.getItemCount() - 1);

        if (item == mCurrentItem && mScrollEventAdapter.isIdle()) {
            // Already at the correct page
            return;
        }
        if (item == mCurrentItem && smoothScroll) {
            // Already scrolling to the correct page, but not yet there. Only handle instant scrolls
            // because then we need to interrupt the current smooth scroll.
            return;
        }

        // 2. Update the item internally

        double previousItem = mCurrentItem;
        mCurrentItem = item;
//        mAccessibilityProvider.onSetNewCurrentItem();

        if (!mScrollEventAdapter.isIdle()) {
            // Scroll in progress, overwrite previousItem with actual current position
            previousItem = mScrollEventAdapter.getRelativeScrollPosition();
        }

        // 3. Perform the necessary scroll actions on RecyclerView

        mScrollEventAdapter.notifyProgrammaticScroll(item, smoothScroll);
        if (!smoothScroll) {
            mRecyclerView.scrollToPosition(item);
            return;
        }

        // For smooth scroll, pre-jump to nearby item for long jumps.
        if (Math.abs(item - previousItem) > 3) {
            mRecyclerView.scrollToPosition(item > previousItem ? item - 3 : item + 3);
            // TODO(b/114361680): call smoothScrollToPosition synchronously (blocked by b/114019007)
            mRecyclerView.post(new SmoothScrollToPosition(item, mRecyclerView));
        } else {
            mRecyclerView.smoothScrollToPosition(item);
        }
    }

    private static class SmoothScrollToPosition implements Runnable {
        private final int mPosition;
        private final RecyclerView mRecyclerView;

        SmoothScrollToPosition(int position, RecyclerView recyclerView) {
            mPosition = position;
            mRecyclerView = recyclerView; // to avoid a synthetic accessor
        }

        @Override
        public void run() {
            mRecyclerView.smoothScrollToPosition(mPosition);
        }
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public @Nullable
    RecyclerView.Adapter getAdapter() {
        return mRecyclerView.getAdapter();
    }

    /**
     * @param orientation @{link {@link ViewPager2.Orientation}}
     */
    public void setOrientation(@Orientation int orientation) {
        mLayoutManager.setOrientation(orientation);
    }

    public @Orientation int getOrientation() {
        return mLayoutManager.getOrientation();
    }

    /**
     * Callback interface for responding to changing state of the selected page.
     */
    public abstract static class OnPageChangeCallback {
        /**
         * This method will be invoked when the current page is scrolled, either as part
         * of a programmatically initiated smooth scroll or a user initiated touch scroll.
         *
         * @param position             Position index of the first page currently being displayed.
         *                             Page position+1 will be visible if positionOffset is nonzero.
         * @param positionOffset       Value from [0, 1) indicating the offset from the page at position.
         * @param positionOffsetPixels Value in pixels indicating the offset from position.
         */
        public void onPageScrolled(int position, float positionOffset,
                                   @Px int positionOffsetPixels) {
        }

        /**
         * This method will be invoked when a new page becomes selected. Animation is not
         * necessarily complete.
         *
         * @param position Position index of the new selected page.
         */
        public void onPageSelected(int position) {
        }

        /**
         * Called when the scroll state changes. Useful for discovering when the user begins
         * dragging, when a fake drag is started, when the pager is automatically settling to the
         * current page, or when it is fully stopped/idle. {@code state} can be one of @link
         * #SCROLL_STATE_IDLE, @link #SCROLL_STATE_DRAGGING or @link #SCROLL_STATE_SETTLING.
         */
        public void onPageScrollStateChanged(int state) {
        }
    }
}
