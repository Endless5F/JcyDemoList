package com.android.baselibrary.http.download;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 自定义下载进度的响应体
 */
public class DownloadResponseBody extends ResponseBody {

    private ResponseBody responseBody;
    private DownloadListener progressListener;
    private BufferedSource bufferedSource;

    public DownloadResponseBody(ResponseBody responseBody, DownloadListener progressListener) {
        this.responseBody = responseBody;
        this.progressListener = progressListener;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                // read（）返回读取的字节数；如果此源已用尽，则返回-1。
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;

                if (null != progressListener) {
                    progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                }
                return bytesRead;
            }
        };
    }
}
