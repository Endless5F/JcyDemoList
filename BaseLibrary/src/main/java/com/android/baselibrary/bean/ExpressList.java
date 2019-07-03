package com.android.baselibrary.bean;

import java.util.List;

public class ExpressList {


    /**
     * message : ok
     * nu : 11111111111
     * ischeck : 0
     * condition : 00
     * com : yuantong
     * status : 200
     * state : 5
     * data : [{"time":"2019-01-08 11:28:37","ftime":"2019-01-08 11:28:37","context":"4街区菜场对面50栋102圆通妈妈驿站已发出自提短信,请上门自提,联系电话025-58868397","location":null},{"time":"2019-01-08 11:27:37","ftime":"2019-01-08 11:27:37","context":"快件已到达4街区菜场对面50栋102圆通妈妈驿站,联系电话025-58868397","location":null}]
     */

    private String message;
    private String nu;
    private String ischeck;
    private String condition;
    private String com;
    private String status;
    private String state;
    private List<DataBean> data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNu() {
        return nu;
    }

    public void setNu(String nu) {
        this.nu = nu;
    }

    public String getIscheck() {
        return ischeck;
    }

    public void setIscheck(String ischeck) {
        this.ischeck = ischeck;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getCom() {
        return com;
    }

    public void setCom(String com) {
        this.com = com;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * time : 2019-01-08 11:28:37
         * ftime : 2019-01-08 11:28:37
         * context : 4街区菜场对面50栋102圆通妈妈驿站已发出自提短信,请上门自提,联系电话025-58868397
         * location : null
         */

        private String time;
        private String ftime;
        private String context;
        private Object location;

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getFtime() {
            return ftime;
        }

        public void setFtime(String ftime) {
            this.ftime = ftime;
        }

        public String getContext() {
            return context;
        }

        public void setContext(String context) {
            this.context = context;
        }

        public Object getLocation() {
            return location;
        }

        public void setLocation(Object location) {
            this.location = location;
        }
    }
}
