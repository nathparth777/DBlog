package com.example.dblog;

import android.app.Application;

public class Global extends Application {
    public int  data;

    public int getData(){
        return data;
    }

    public void setData(int  d){
        data=d;
    }
}
