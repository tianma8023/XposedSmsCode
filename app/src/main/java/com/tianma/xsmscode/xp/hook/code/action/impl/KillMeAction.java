package com.tianma.xsmscode.xp.hook.code.action.impl;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.common.utils.XSPUtils;
import com.tianma.xsmscode.data.db.entity.SmsMsg;
import com.tianma.xsmscode.xp.hook.code.action.CallableAction;

import de.robv.android.xposed.XSharedPreferences;

public class KillMeAction extends CallableAction {

    public KillMeAction(Context pluginContext, Context phoneContext, SmsMsg smsMsg, XSharedPreferences xsp) {
        super(pluginContext, phoneContext, smsMsg, xsp);
    }

    @Override
    public Bundle action() {
        killMe();
        return null;
    }

    private void killMe() {
        if (XSPUtils.killMeEnabled(xsp)) {
            killBackgroundProcess(BuildConfig.APPLICATION_ID);
        }
    }

    /**
     * android.app.ActivityManager#killBackgroundProcess()
     */
    @SuppressLint("MissingPermission")
    private void killBackgroundProcess(String packageName) {
        try {
            ActivityManager activityManager = (ActivityManager) mPluginContext.getSystemService(Context.ACTIVITY_SERVICE);

            if (activityManager != null) {
                activityManager.killBackgroundProcesses(packageName);
                XLog.d("Kill %s background process succeed", packageName);
            }
        } catch (Throwable e) {
            XLog.e("Error occurs when kill background process %s", packageName, e);
        }
    }
}
