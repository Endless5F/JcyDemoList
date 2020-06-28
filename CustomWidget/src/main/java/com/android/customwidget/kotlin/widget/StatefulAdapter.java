package com.android.customwidget.kotlin.widget;

import android.os.Parcelable;
import android.support.annotation.NonNull;

public interface StatefulAdapter {
    /** Saves adapter state */
    @NonNull
    Parcelable saveState();

    /** Restores adapter state */
    void restoreState(@NonNull Parcelable savedState);
}
