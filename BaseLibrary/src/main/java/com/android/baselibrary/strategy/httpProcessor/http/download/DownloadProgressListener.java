package com.android.baselibrary.strategy.httpProcessor.http.download;

/**
 * 下载进度listener
 */
public interface DownloadProgressListener {
    void update(long bytesRead, long contentLength, boolean done);
}
