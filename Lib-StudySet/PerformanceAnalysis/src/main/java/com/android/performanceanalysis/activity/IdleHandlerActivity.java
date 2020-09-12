package com.android.performanceanalysis.activity;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.performanceanalysis.R;

public class IdleHandlerActivity extends AppCompatActivity {

    private static final String TAG = "IdleHandlerActivity";
    private static int idleHandlerRunCount = 0;
    @SuppressLint("HandlerLeak")
    private Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: msg:" + msg.what);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idle_handler);
        TextView idle_tv = findViewById(R.id.idle_tv);

        idle_tv.setOnClickListener(v -> {
            Log.e(TAG, "点击了TextView....");
            for (int i = 1; i < 5; i++) {
                mainHandler.sendEmptyMessage(i);
            }
        });


        Looper.myQueue().addIdleHandler(new RunOnceHandler());
        Looper.myQueue().addIdleHandler(new KeepAliveHandler());
    }

    // KeepAliveHandler的运行是有一定不可控的, 它只是在ui线程空闲时触发, 但是什么时候ui线程空闲, 很难把握的
    class KeepAliveHandler implements MessageQueue.IdleHandler {
        /**
         * 返回值为true，则保持此Idle一直在Handler中
         */
        @Override
        public boolean queueIdle() {
            idleHandlerRunCount++;
            Log.d(TAG, "KeepAliveHandler.queueIdle...第" + idleHandlerRunCount + "次运行");
            return true;
        }

    }

    class RunOnceHandler implements MessageQueue.IdleHandler {
        @Override
        public boolean queueIdle() {
            Log.d(TAG, "RunOnceHandler.queueIdle()...只运行一次");
            return false;
        }
    }
}
