package com.android.baselibrary.bean;

/**
 * Created by jcy on 2017/5/26.
 */

public class AppInfo {

    /**
     * id : 2
     * title : 11
     * VersionC : v1.1
     * file_size : 1964
     * download_url : http://api.appapi.com/upload/46cde4f6e200d221/a5c9db2ae5cc9386.apk
     * update_describe : aaa
     * create_time : 1495610193
     */

    private int id;
    private String title;
    private String version;
    private String file_size;
    private String download_url;
    private String update_describe;
    private int create_time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFile_size() {
        return file_size;
    }

    public void setFile_size(String file_size) {
        this.file_size = file_size;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public String getUpdate_describe() {
        return update_describe;
    }

    public void setUpdate_describe(String update_describe) {
        this.update_describe = update_describe;
    }

    public int getCreate_time() {
        return create_time;
    }

    public void setCreate_time(int create_time) {
        this.create_time = create_time;
    }
}
