package com.github.tianma8023.xposed.smscode;

import android.app.Application;

import com.github.tianma8023.xposed.smscode.utils.XLog;

public class App extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        XLog.i("Application onCreate");
    }
}
