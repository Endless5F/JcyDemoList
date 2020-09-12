package com.android.javalib.bean;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;

public class MainData  extends BaseRowModel {
    @ExcelProperty(index = 0)
    private int serialNumber;

    @ExcelProperty(index = 1)
    private String columnName1 = "";

    @ExcelProperty(index = 2)
    private String movieName;

    @ExcelProperty(index = 3)
    private int id;

    @ExcelProperty(index = 4)
    private String posterUrl;

    @ExcelProperty(index = 5)
    private int iconWidth;

    @ExcelProperty(index = 6)
    private int iconHeight;

    @ExcelProperty(index = 7)
    private String deepLink;

    @ExcelProperty(index = 8)
    private String iconUrl;

    @ExcelProperty(index = 9)
    private String provider;

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getColumnName1() {
        return columnName1;
    }

    public void setColumnName1(String columnName1) {
        this.columnName1 = columnName1;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public int getIconWidth() {
        return iconWidth;
    }

    public void setIconWidth(int iconWidth) {
        this.iconWidth = iconWidth;
    }

    public int getIconHeight() {
        return iconHeight;
    }

    public void setIconHeight(int iconHeight) {
        this.iconHeight = iconHeight;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public String toString() {
        return "MainData{" +
                "serialNumber=" + serialNumber +
                ", columnName1='" + columnName1 + '\'' +
                ", movieName='" + movieName + '\'' +
                ", id=" + id +
                ", posterUrl='" + posterUrl + '\'' +
                ", iconWidth=" + iconWidth +
                ", iconHeight=" + iconHeight +
                ", deepLink='" + deepLink + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", provider='" + provider + '\'' +
                '}';
    }
}
