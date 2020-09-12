package com.android.httpserver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.httpserver.utils.LogUtils;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.Multimap;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.async.http.body.Part;
import com.koushikdutta.async.http.body.UrlEncodedFormBody;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;

import static com.koushikdutta.async.http.body.Part.CONTENT_DISPOSITION;

/**
 * @author jiaochengyun.ex
 */
public class WebService extends Service {

    public static final int HTTP_PORT = 54345;
    public static final File DIR = new File(Environment.getExternalStorageDirectory() + File.separator + "TEST");

    static final String TAG = "WebService";
    static final String ACTION_START_WEB_SERVICE = "com.baidusoso.wifitransfer.action.START_WEB_SERVICE";
    static final String ACTION_STOP_WEB_SERVICE = "com.baidusoso.wifitransfer.action.STOP_WEB_SERVICE";

    private static final String TEXT_CONTENT_TYPE = "text/html;charset=utf-8";
    private static final String CSS_CONTENT_TYPE = "text/css;charset=utf-8";
    private static final String BINARY_CONTENT_TYPE = "application/octet-stream";
    private static final String JS_CONTENT_TYPE = "application/javascript";
    private static final String PNG_CONTENT_TYPE = "application/x-png";
    private static final String JPG_CONTENT_TYPE = "application/jpeg";
    private static final String SWF_CONTENT_TYPE = "application/x-shockwave-flash";
    private static final String WOFF_CONTENT_TYPE = "application/x-font-woff";
    private static final String TTF_CONTENT_TYPE = "application/x-font-truetype";
    private static final String SVG_CONTENT_TYPE = "image/svg+xml";
    private static final String EOT_CONTENT_TYPE = "image/vnd.ms-fontobject";
    private static final String MP3_CONTENT_TYPE = "audio/mp3";
    private static final String MP4_CONTENT_TYPE = "video/mpeg4";
    FileUploadHolder fileUploadHolder = new FileUploadHolder();
    private AsyncHttpServer server = new AsyncHttpServer();
    private AsyncServer mAsyncServer = new AsyncServer();

    public static void start(Context context) {
        Intent intent = new Intent(context, WebService.class);
        intent.setAction(ACTION_START_WEB_SERVICE);
        context.startService(intent);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, WebService.class);
        intent.setAction(ACTION_STOP_WEB_SERVICE);
        context.startService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_START_WEB_SERVICE.equals(action)) {
                startServer();
            } else if (ACTION_STOP_WEB_SERVICE.equals(action)) {
                stopSelf();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
        }
        if (mAsyncServer != null) {
            mAsyncServer.stop();
        }
        System.out.println("服务结束啦...");
        Toast.makeText(this, "服务结束啦...", Toast.LENGTH_SHORT).show();
    }

    /**
     * 基础：
     * 获取x-www-form-urlencoded请求体：UrlEncodedFormBody body = (UrlEncodedFormBody) request.getBody();
     * 获取multipart/form-data请求体：MultipartFormDataBody body = (MultipartFormDataBody) request.getBody()
     * 发送文本：response.send("Hello world!");
     * 发送文件流：
     * BufferedInputStream bInputStream = ...
     * 第一种. response.sendStream(bInputStream, bInputStream.available());
     * 第二种. response.sendFile(...);
     * 发送Json：response.send(new JSONObject());
     * 发送Header：response.getHeaders().add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), "utf-8"));
     * 发送特定的响应码：response.code(500);//这个一定要和send()或者end()一起使用
     * <p>
     * 参考链接1：https://juejin.im/entry/5a5ea8c26fb9a01cb74e64c1
     * 参考链接2：https://blog.csdn.net/gorgle/article/details/52788701
     */
    private void startServer() {
        System.out.println("服务开启啦...");
        Toast.makeText(this, "服务开启啦...", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, DIR.getAbsolutePath(), Toast.LENGTH_LONG).show();
        server.get("/images/.*", this::sendResources);
        server.get("/scripts/.*", this::sendResources);
        server.get("/css/.*", this::sendResources);
        //index page
        server.get("/", (AsyncHttpServerRequest request, AsyncHttpServerResponse response) -> {
            try {
                response.send(getIndexContent());
            } catch (IOException e) {
                e.printStackTrace();
                response.code(500).end();
            }
        });

        // query upload list
        server.get("/files/get", (AsyncHttpServerRequest request, AsyncHttpServerResponse response) -> {
            JSONArray array = new JSONArray();
            File dir = DIR;
            if (dir.exists() && dir.isDirectory()) {
                String[] fileNames = dir.list();
                if (fileNames != null) {
                    for (String fileName : fileNames) {
                        File file = new File(dir, fileName);
                        if (file.exists() && file.isFile()) {
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("name", fileName);
                                long fileLen = file.length();
                                DecimalFormat df = new DecimalFormat("0.00");
                                if (fileLen > 1024 * 1024) {
                                    jsonObject.put("size", df.format(fileLen * 1f / 1024 / 1024) + "MB");
                                } else if (fileLen > 1024) {
                                    jsonObject.put("size", df.format(fileLen * 1f / 1024) + "KB");
                                } else {
                                    jsonObject.put("size", fileLen + "B");
                                }
                                array.put(jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            response.send(array.toString());
        });

        // delete
        server.post("/delete", (AsyncHttpServerRequest request, AsyncHttpServerResponse response) -> {
            LogUtils.i("delete");
            final UrlEncodedFormBody body = (UrlEncodedFormBody) request.getBody();
            String files = body.get().getString("files");
            try {
                files = URLDecoder.decode(files, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String[] fileArray = files.split(",");
            JSONArray jsonArray = new JSONArray();
            for (String name : fileArray) {
                File file = new File(DIR, name);
                try {
                    if (file.exists() && file.isFile()) {
                        file.delete();
                    }
                } catch (Exception exception) {
                    LogUtils.i(exception.toString());
                } finally {
                    // 0：删除成功，1：删除失败
                    int code = file.exists() ? 1 : 0;
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("name", file.getName());
                        jsonObject.put("code", code);
                        jsonArray.put(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            response.send(jsonArray.toString());
        });

        /*
         * download两种方式
         * <p>
         * 1. regex = /files*    url方式：/files?name=xxx.jpg&time=xxx
         *    Multimap query = request.getQuery();
         *    String path = query.getString("name");
         * <p>
         * 2. regex = /files/.*  url方式：/files/xxx.jpg
         *    String path = request.getPath().replace("/files/", "");
         */
        server.get("/files/.*", (AsyncHttpServerRequest request, AsyncHttpServerResponse response) -> {
            String path = request.getPath().replace("/files/", "");
            try {
                path = URLDecoder.decode(path, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            File file = new File(DIR, path);
            if (file.exists() && file.isFile()) {
                try {
                    response.getHeaders().add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                response.sendFile(file);
                return;
            }
            response.code(404).send("Not found!");
        });

        /*
         * upload
         * 获取请求路径：通过request.getPath()
         * 获取GET参数：通过request.getQuery()
         * 获取POST参数：这个比较麻烦，github上也没有任何参考，经研究发现：
         *  Form格式：需要把body转换成AsyncHttpRequestBody<Multimap>
         *  JSON格式：需要把body转换成AsyncHttpRequestBody<JSONObject>
         *
         * 支持跨域，可以在response的header中增加字段：
         * response.getHeaders().add("Access-Control-Allow-Origin", "*");
         * response.send(json);
         */
        server.post("/upload", (AsyncHttpServerRequest request, AsyncHttpServerResponse response) -> {
                    LogUtils.i("upload " + request.toString());
                    try {
                        // 注意此处，防止类型转换异常，客户端请求时需要将整体Content-type设置为MultipartBody.FORM(multipart/form-data)
                        MultipartFormDataBody body = (MultipartFormDataBody) request.getBody();
                        body.setMultipartCallback((Part part) -> {
                            // 参数key
                            String name = part.getName();
                            switch (name != null ? name : "") {
                                case "fileList":
                                    if (body.getDataCallback() == null) {
                                        body.setDataCallback((emitter, bb) -> {
                                            try {
                                                String fileList = URLDecoder.decode(new String(bb.getAllByteArray()), "UTF-8");
                                                LogUtils.i("fileList：" + fileList);
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }
                                            bb.recycle();
                                        });
                                    }
                                    return;
                                case "fileName":
                                    if (body.getDataCallback() == null) {
                                        body.setDataCallback((DataEmitter emitter, ByteBufferList bb) -> {
                                            try {
                                                String fileName = URLDecoder.decode(new String(bb.getAllByteArray()), "UTF-8");
                                                fileUploadHolder.setFileName(fileName);
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }
                                            bb.recycle();
                                        });
                                    }
                                    return;
                                default:
                                    break;
                            }

                            LogUtils.i("getRawHeaders：" + part.getRawHeaders());
                            // 获取客户端配置的CONTENT_DISPOSITION头信息字段中的数据
                            Headers rawHeaders = part.getRawHeaders();
                            Multimap nameValuePairs = Multimap.parseSemicolonDelimited(rawHeaders.get(CONTENT_DISPOSITION));
                            String filelength = nameValuePairs.getString("filelength");
                            LogUtils.i("filelength：" + filelength);
                            if (part.isFile()) {
                                // isFile()方法，Headers头信息Content-Disposition中，需要配置"filename="xxx""才可以
                                body.setDataCallback((DataEmitter emitter, ByteBufferList bb) -> {
                                    fileUploadHolder.write(bb.getAllByteArray());
                                    bb.recycle();
                                });
                            }
                        });
                        request.setEndCallback((Exception e) -> {
                            fileUploadHolder.reset();
                            response.send("SUCCESS setEndCallback");
                        });
                    } catch (Exception c) {
                        LogUtils.e(c.toString());
                    } finally {
                        LogUtils.i("upload finally");
                    }
                }
        );

        server.get("/progress/.*", (final AsyncHttpServerRequest request, final AsyncHttpServerResponse response) -> {
                    JSONObject res = new JSONObject();
                    String path = request.getPath().replace("/progress/", "");
                    if (path.equals(fileUploadHolder.fileName)) {
                        try {
                            res.put("fileName", fileUploadHolder.fileName);
                            res.put("size", fileUploadHolder.totalSize);
                            res.put("progress", fileUploadHolder.fileOutPutStream == null ? 1 : 0.1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    response.send(res);
                }
        );
        server.listen(mAsyncServer, HTTP_PORT);
    }

    private String getIndexContent() throws IOException {
        BufferedInputStream bInputStream = null;
        try {
            bInputStream = new BufferedInputStream(getAssets().open("wifi/index.html"));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len = 0;
            byte[] tmp = new byte[10240];
            while ((len = bInputStream.read(tmp)) > 0) {
                baos.write(tmp, 0, len);
            }
            return new String(baos.toByteArray(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (bInputStream != null) {
                try {
                    bInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendResources(final AsyncHttpServerRequest request, final AsyncHttpServerResponse response) {
        try {
            String fullPath = request.getPath();
            fullPath = fullPath.replace("%20", " ");
            String resourceName = fullPath;
            if (resourceName.startsWith("/")) {
                resourceName = resourceName.substring(1);
            }
            if (resourceName.indexOf("?") > 0) {
                resourceName = resourceName.substring(0, resourceName.indexOf("?"));
            }
            if (!TextUtils.isEmpty(getContentTypeByResourceName(resourceName))) {
                response.setContentType(getContentTypeByResourceName(resourceName));
            }
            BufferedInputStream bInputStream = new BufferedInputStream(getAssets().open("wifi/" + resourceName));
            response.sendStream(bInputStream, bInputStream.available());
        } catch (IOException e) {
            e.printStackTrace();
            response.code(404).end();
        }
    }

    private String getContentTypeByResourceName(String resourceName) {
        if (resourceName.endsWith(".css")) {
            return CSS_CONTENT_TYPE;
        } else if (resourceName.endsWith(".js")) {
            return JS_CONTENT_TYPE;
        } else if (resourceName.endsWith(".swf")) {
            return SWF_CONTENT_TYPE;
        } else if (resourceName.endsWith(".png")) {
            return PNG_CONTENT_TYPE;
        } else if (resourceName.endsWith(".jpg") || resourceName.endsWith(".jpeg")) {
            return JPG_CONTENT_TYPE;
        } else if (resourceName.endsWith(".woff")) {
            return WOFF_CONTENT_TYPE;
        } else if (resourceName.endsWith(".ttf")) {
            return TTF_CONTENT_TYPE;
        } else if (resourceName.endsWith(".svg")) {
            return SVG_CONTENT_TYPE;
        } else if (resourceName.endsWith(".eot")) {
            return EOT_CONTENT_TYPE;
        } else if (resourceName.endsWith(".mp3")) {
            return MP3_CONTENT_TYPE;
        } else if (resourceName.endsWith(".mp4")) {
            return MP4_CONTENT_TYPE;
        }
        return "";
    }

    public class FileUploadHolder {
        private String fileName;
        private File recievedFile;
        private BufferedOutputStream fileOutPutStream;
        private long totalSize;


        public BufferedOutputStream getFileOutPutStream() {
            return fileOutPutStream;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
            totalSize = 0;
            if (!DIR.exists()) {
                DIR.mkdirs();
            }
            this.recievedFile = new File(DIR, this.fileName);
            Log.d(TAG, recievedFile.getAbsolutePath());
            try {
                fileOutPutStream = new BufferedOutputStream(new FileOutputStream(recievedFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        public void reset() {
            if (fileOutPutStream != null) {
                try {
                    fileOutPutStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            fileOutPutStream = null;
        }

        public void write(byte[] data) {
            if (fileOutPutStream != null) {
                try {
                    fileOutPutStream.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            totalSize += data.length;
            LogUtils.e("totalSize：" + totalSize);
        }
    }
}
