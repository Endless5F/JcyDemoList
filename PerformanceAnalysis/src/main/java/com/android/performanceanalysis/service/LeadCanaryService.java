package com.android.performanceanalysis.service;

import com.android.performanceanalysis.utils.FileUtils;
import com.squareup.leakcanary.AnalysisResult;
import com.squareup.leakcanary.DisplayLeakService;
import com.squareup.leakcanary.HeapDump;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class LeadCanaryService extends DisplayLeakService {
    @Override
    protected void afterDefaultHandling(HeapDump heapDump, AnalysisResult result, String leakInfo) {
        super.afterDefaultHandling(heapDump, result, leakInfo);
        // 泄漏信息上传云端或者保存本地
        saveLocal(result, leakInfo);
    }

    private void saveLocal(AnalysisResult result, String leakInfo) {
        if (result != null) {
            String leakPath = getApplication().getCacheDir().getAbsolutePath() + "/LeakCanary" +
                    "/LeakCanary.log";
            File file = new File(leakPath);
            FileUtils.createFileDir(file);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String leadMessage = "Time" + simpleDateFormat.toString() +
                    "\\n AnalysisResult{" +
                    "leakFound=" + result.leakFound +
                    ", excludedLeak=" + result.excludedLeak +
                    ", className='" + result.className + '\'' +
                    ", leakTrace=" + result.leakTrace +
                    ", failure=" + result.failure +
                    ", retainedHeapSize=" + result.retainedHeapSize +
                    ", analysisDurationMs=" + result.analysisDurationMs +
                    "} \\r\\n";

            ByteArrayInputStream byteArrayInputStream =
                    new ByteArrayInputStream(leadMessage.getBytes());
            try {
                FileUtils.writeFile(byteArrayInputStream, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
