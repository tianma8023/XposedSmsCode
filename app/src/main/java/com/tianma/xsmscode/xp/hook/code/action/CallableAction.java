package com.tianma.xsmscode.xp.hook.code.action;

import android.content.Context;
import android.os.Bundle;

import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.data.db.entity.SmsMsg;

import java.util.concurrent.Callable;

import de.robv.android.xposed.XSharedPreferences;

/**
 * Action + Callable
 */
public abstract class CallableAction implements Action<Bundle>, Callable<Bundle> {

    protected Context mPluginContext;
    protected Context mPhoneContext;
    protected SmsMsg mSmsMsg;
    protected XSharedPreferences xsp;

    public CallableAction(Context pluginContext, Context phoneContext, SmsMsg smsMsg, XSharedPreferences xsp) {
        mPluginContext = pluginContext;
        mPhoneContext = phoneContext;
        mSmsMsg = smsMsg;
        this.xsp = xsp;
    }

    @Override
    public Bundle call() {
        try {
            return action();
        } catch (Throwable t) {
            XLog.e("Error in CallableAction#call()", t);
            return null;
        }
    }
}
