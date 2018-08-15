package com.example.joney.demo20;

import android.app.Application;

/**
 * Created by joney on 2018/7/12.   用于处理转子系数值 包括数据的读写操作
 */

public class Data extends Application {

    private String SP_0,SP_1,SP_2,SP_3,SP_4;  //5个转子号的系数定义

    @Override
    public void onCreate() {
        SP_0 = "100";
        SP_1 = "159";
        SP_2 = "900";
        SP_3 = "2832";
        SP_4 = "14200";
        super.onCreate();
    }
    public String getSP_0() {
        return this.SP_0;
    }
    public void setSP_0(String SP) {
        this.SP_0 = SP;
    }
    public String getSP_1() {
        return this.SP_1;
    }
    public void setSP_1(String SP) {
        this.SP_1 = SP;
    }public String getSP_2() {
        return this.SP_2;
    }
    public void setSP_2(String SP) {
        this.SP_2 = SP;
    }public String getSP_3() {
        return this.SP_3;
    }
    public void setSP_3(String SP) {
        this.SP_3 = SP;
    }public String getSP_4() {
        return this.SP_4;
    }
    public void setSP_4(String SP) {
        this.SP_4 = SP;
    }

}

