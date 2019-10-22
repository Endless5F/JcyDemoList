package com.android.performanceanalysis.task;

import com.android.performanceanalysis.launchstarter.task.Task;
import com.facebook.stetho.Stetho;

public class InitStethoTask  extends Task {
    @Override
    public void run() {
        Stetho.initializeWithDefaults(mContext);
    }
}
