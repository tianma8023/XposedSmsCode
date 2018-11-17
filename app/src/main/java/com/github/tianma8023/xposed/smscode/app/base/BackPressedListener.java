package com.github.tianma8023.xposed.smscode.app.base;

public interface BackPressedListener {

    boolean onInterceptBackPressed();

    void onBackPressed();

}
