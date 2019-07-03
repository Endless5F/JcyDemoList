package com.android.baselibrary.bean;

/**
 * 用户表
 * Created by jcy on 2017/5/4.
 * */
public class Member {
    private int uid;
    private String username;
    private String nickname;
    private String realname;
    private String headimg;
    private String sex;
    private int age;
    private String mobile;
    private String email;
    private String desc;
    private int weight;
    private int target;
    private String qq;
    private String uuid;
    private int height;
    private String eduid;
    private String school_name;
    private String class_name;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getEduid() {
        return eduid;
    }

    public void setEduid(String eduid) {
        this.eduid = eduid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHeadimg() {
        return headimg;
    }

    public void setHeadimg(String headimg) {
        this.headimg = headimg;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getSchool_name() {
        return school_name;
    }

    public void setSchool_name(String school_name) {
        this.school_name = school_name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    @Override
    public String toString() {
        return "Member{" +
                "uid=" + uid +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", realname='" + realname + '\'' +
                ", headimg='" + headimg + '\'' +
                ", sex='" + sex + '\'' +
                ", age='" + age + '\'' +
                ", mobile='" + mobile + '\'' +
                ", email='" + email + '\'' +
                ", desc='" + desc + '\'' +
                ", weight='" + weight + '\'' +
                ", target='" + target + '\'' +
                ", qq='" + qq + '\'' +
                ", uuid='" + uuid + '\'' +
                ", height='" + height + '\'' +
                ", eduid='" + eduid + '\'' +
                ", school_name='" + school_name + '\'' +
                ", class_name=" + class_name +
                '}';
    }
}
