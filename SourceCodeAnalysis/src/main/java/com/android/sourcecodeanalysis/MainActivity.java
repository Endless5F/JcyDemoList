package com.android.sourcecodeanalysis;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.MessageQueue;
import android.support.annotation.RequiresApi;
import android.telecom.Call;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.CacheRequest;
import java.net.HttpRetryException;
import java.net.ProtocolException;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MainActivity extends Activity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}