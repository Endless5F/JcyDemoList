package com.android.architecture.demolist.databinding;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.android.architecture.BR;

/**
 * 首先我们需要在getter方法上添加Bindable注解后，Bindable注解会自动生成一个BR类，该类位于appmodule包下，
 * 通过BR类我们设置更新的数据，当Model中的数据发生变化时，setter方法中的notifyPropertyChanged()就会通知UI更新数据了。
 *
 * */
public class ObservableObjects extends BaseObservable {
    private String userName;
    private String userPassword;
    public ObservableObjects(String userName, String userPassword) {
        this.userName = userName;
        this.userPassword = userPassword;
    }

    @Bindable
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        notifyPropertyChanged(BR.userName);
    }

    @Bindable
    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
        notifyPropertyChanged(BR.userPassword);
    }
}