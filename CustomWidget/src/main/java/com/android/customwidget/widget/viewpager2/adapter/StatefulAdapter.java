package com.android.customwidget.widget.viewpager2.adapter;

import android.os.Parcelable;
import android.support.annotation.NonNull;

public interface StatefulAdapter {
    /** Saves adapter state */
    @NonNull
    Parcelable saveState();

    /** Restores adapter state */
    void restoreState(@NonNull Parcelable savedState);
}
