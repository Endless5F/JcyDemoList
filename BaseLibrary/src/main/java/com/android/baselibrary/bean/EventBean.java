package com.android.baselibrary.bean;

/**
 * 万能接口bean
 * Created by jcy on 2017/5/4.
 * */
public class EventBean {
    private String username;
    private int age;
    private String mobile;

    public EventBean(String username, int age, String mobile) {
        this.username = username;
        this.age = age;
        this.mobile = mobile;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "EventBean{" +
                "username='" + username + '\'' +
                ", age=" + age +
                ", mobile='" + mobile + '\'' +
                '}';
    }
}
