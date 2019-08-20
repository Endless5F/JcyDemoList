package com.android.sourcecodeanalysis;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.MessageQueue;
import android.support.annotation.RequiresApi;
import android.telecom.Call;
import android.util.Log;

import java.io.IOException;
import java.net.HttpRetryException;
import java.net.ProtocolException;
import java.util.ArrayList;

public class MainActivity extends Activity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}

