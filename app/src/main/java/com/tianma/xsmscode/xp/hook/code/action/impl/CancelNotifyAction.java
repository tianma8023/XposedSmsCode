package com.tianma.xsmscode.xp.hook.code.action.impl;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.data.db.entity.SmsMsg;
import com.tianma.xsmscode.xp.hook.code.action.CallableAction;

import de.robv.android.xposed.XSharedPreferences;

public class CancelNotifyAction extends CallableAction {

    private static final int NOTIFICATION_NONE = -0xff;

    private int mNotificationId = NOTIFICATION_NONE;

    public CancelNotifyAction(Context pluginContext, Context phoneContext, SmsMsg smsMsg, XSharedPreferences xsp) {
        super(pluginContext, phoneContext, smsMsg, xsp);
    }

    public void setNotificationId(int notificationId) {
        mNotificationId = notificationId;
    }

    @Override
    public Bundle action() {
        cancelNotification();
        return null;
    }

    private void cancelNotification() {
        if (mNotificationId != NOTIFICATION_NONE) {
            NotificationManager manager = (NotificationManager) mPhoneContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) {
                return;
            }
            manager.cancel(mNotificationId);
            XLog.d("Notification auto cancelled");
        }
    }
}
