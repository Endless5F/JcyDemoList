package com.android.baselibrary.http.download;

/**
 * 下载进度listener
 */
public interface DownloadListener {
    void update(long bytesRead, long contentLength, boolean done);
}
