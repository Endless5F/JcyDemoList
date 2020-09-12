package com.android.httpserver;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.httpserver.utils.LogUtils;
import com.android.httpserver.utils.MainHandlerUtils;
import com.android.httpserver.utils.OkHttpUtils;
import com.android.httpserver.utils.PermissionsUtils;
import com.android.httpserver.utils.WifiUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jcy
 * <p>
 * 基于https://github.com/koush/AndroidAsync库
 * 基于WIFI传书：https://github.com/baidusoso/WifiTransfer
 * 基于WIFI隔空APK安装：https://github.com/MZCretin/WifiTransfer-master
 */
public class WebActivity extends AppCompatActivity {

    private EditText ipEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        ipEdit = findViewById(R.id.ip_edit);
        ipEdit.setText("http://" + WifiUtils.getWifiIp(this) + ":" + WebService.HTTP_PORT + "/");

        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        PermissionsUtils.getInstance().chekPermissions(this, permissions,
                new PermissionsUtils.IPermissionsResult() {
                    @Override
                    public void passPermissons() {
                        Toast.makeText(WebActivity.this, "权限通过，可以做其他事情!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void forbitPermissons() {
                        Toast.makeText(WebActivity.this, "权限不通过!", Toast.LENGTH_SHORT).show();
                    }
                });

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebService.start(WebActivity.this);
            }
        });

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebService.stop(WebActivity.this);
            }
        });

        // TODO 已完成
        findViewById(R.id.getData).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OkHttpUtils.builder()
                        .url(ipEdit.getText().toString() + "files/get")
                        .success(new OkHttpUtils.ISuccess() {
                            @Override
                            public void onSuccess(String response) {
                                Toast.makeText(WebActivity.this, response, Toast.LENGTH_LONG).show();
                            }
                        }).build().get(true);
            }
        });

        // TODO 已完成
        EditText downloadEdit = findViewById(R.id.download_edit);
        findViewById(R.id.downloadFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OkHttpUtils okHttpUtils = OkHttpUtils.builder()
                        .url(ipEdit.getText().toString() + "files/" + downloadEdit.getText().toString())
                        .path(WebService.DIR + File.separator + "Test")
                        .download(new OkHttpUtils.IDownLoad() {
                            @Override
                            public void startDownload(long fileLength) {
                                LogUtils.e("fileLength：" + fileLength);
                            }

                            @Override
                            public void pauseDownload() {

                            }

                            @Override
                            public void finishDownload(String path) {
                                LogUtils.e("path：" + path);
                                MainHandlerUtils.getInstance().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(WebActivity.this, "下载完成",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void downloadProgress(long progress) {
                                LogUtils.e("progress：" + progress);
                            }

                            @Override
                            public void downloadError() {
                                LogUtils.e("downloadError");
                            }
                        }).build();
                okHttpUtils.download();
                // 停止下载
//                okHttpUtils.stopDownload();
            }
        });

        // TODO 已完成
        EditText deleteEdit = findViewById(R.id.delete_edit);
        findViewById(R.id.deleteFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OkHttpUtils.builder()
                        .url(ipEdit.getText().toString() + "delete")
                        .params("files", deleteEdit.getText().toString())
                        .success(response -> {
                            Toast.makeText(WebActivity.this, "success：" + response,
                                    Toast.LENGTH_LONG).show();
                            LogUtils.e("success：" + response);
                        })
                        .failure(() -> {
                            Toast.makeText(WebActivity.this, "failure", Toast.LENGTH_SHORT).show();
                        }).build().post(true);
            }
        });

        // TODO 已完成
        findViewById(R.id.upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadRequest();
            }
        });


    }

    private void uploadRequest() {
        EditText uploadEdit = findViewById(R.id.upload_edit);
        File file = new File(Environment.getExternalStorageDirectory().getPath(),
                uploadEdit.getText().toString());
//        List<File> fileList = new ArrayList<>();
//        File file1 = new File(Environment.getExternalStorageDirectory().getPath(), "123456.mp4");
//        fileList.add(file1);
//        File file2 = new File(Environment.getExternalStorageDirectory().getPath(), "234567.mp4");
//        fileList.add(file2);
        if (file.exists()) {
            long l = System.currentTimeMillis();
            OkHttpUtils.builder()
                    .url(ipEdit.getText().toString() + "upload")
                    .file(file)
//                    .files(fileList)
                    .progress(new OkHttpUtils.IProgress() {
                        @Override
                        public void onProgress(long totalBytes, long remainingBytes, boolean done) {
                            LogUtils.i("onProgress：" + (totalBytes - remainingBytes) * 100 / totalBytes + "%");
                        }
                    })
                    .success(new OkHttpUtils.ISuccess() {
                        @Override
                        public void onSuccess(String response) {
                            long l1 = System.currentTimeMillis();
                            LogUtils.e("upload：" + (l1 - l));
                            Toast.makeText(WebActivity.this, "onSuccess：" + (l1 - l),
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .failure(new OkHttpUtils.IFailure() {
                        @Override
                        public void onFailure() {
                            Toast.makeText(WebActivity.this, "上传失败了...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .build().postFile(true);
        } else {
            Toast.makeText(WebActivity.this, "file 不存在", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 多一个参数this
        PermissionsUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions,
                grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebService.stop(this);
    }
}
