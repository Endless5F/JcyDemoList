package com.android.baselibrary.strategy.httpProcessor.net.download;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.android.baselibrary.app.AppGlobal;
import com.android.baselibrary.strategy.httpProcessor.net.callBack.IRequest;
import com.android.baselibrary.strategy.httpProcessor.net.callBack.ISuccess;
import com.android.baselibrary.util.file.FileTools;

import java.io.File;
import java.io.InputStream;

import okhttp3.ResponseBody;

final class SaveFileTask extends AsyncTask<Object, Void, File> {

    private final IRequest REQUEST;
    private final ISuccess SUCCESS;

    SaveFileTask(IRequest REQUEST, ISuccess SUCCESS) {
        this.REQUEST = REQUEST;
        this.SUCCESS = SUCCESS;
    }

    @Override
    protected File doInBackground(Object... params) {
        String downloadDir = (String) params[0];
        String extension = (String) params[1];
        final ResponseBody body = (ResponseBody) params[2];
        final String name = (String) params[3];
        final InputStream is = body.byteStream();
        if (downloadDir == null || downloadDir.equals("")) {
            downloadDir = "down_loads";
        }
        if (extension == null || extension.equals("")) {
            extension = "";
        }
        if (name == null) {
            return FileTools.writeToDisk(is, downloadDir, extension.toUpperCase(), extension);
        } else {
            return FileTools.writeToDisk(is, downloadDir, name);
        }
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        if (SUCCESS != null) {
            SUCCESS.onSuccess(file.getPath());
        }
        if (REQUEST != null) {
            REQUEST.onRequestEnd();
        }
        autoInstallApk(file);
    }

    private void autoInstallApk(File file) {
        if (FileTools.getExtension(file.getPath()).equals("apk")) {
            final Intent install = new Intent();
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.setAction(Intent.ACTION_VIEW);
            install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            AppGlobal.getApplicationContext().startActivity(install);
        }
    }
}
