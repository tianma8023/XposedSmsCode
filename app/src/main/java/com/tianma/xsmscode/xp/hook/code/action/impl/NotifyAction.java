package com.tianma.xsmscode.xp.hook.code.action.impl;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.github.tianma8023.xposed.smscode.R;
import com.tianma.xsmscode.common.constant.NotificationConst;
import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.common.utils.XSPUtils;
import com.tianma.xsmscode.data.db.entity.SmsMsg;
import com.tianma.xsmscode.xp.hook.code.CopyCodeReceiver;
import com.tianma.xsmscode.xp.hook.code.action.CallableAction;

import de.robv.android.xposed.XSharedPreferences;

/**
 * 显示验证码通知
 */
public class NotifyAction extends CallableAction {

    public static final String NOTIFY_RETENTION_TIME = "notify_retention_time";
    public static final String NOTIFY_ID = "notify_id";

    public NotifyAction(Context appContext, Context phoneContext, SmsMsg smsMsg, XSharedPreferences xsp) {
        super(appContext, phoneContext, smsMsg, xsp);
    }

    @Override
    public Bundle action() {
        if (XSPUtils.showCodeNotification(xsp)) {
            return showCodeNotification(mSmsMsg);
        }
        return null;
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private Bundle showCodeNotification(SmsMsg smsMsg) {
        NotificationManager manager = (NotificationManager) mPhoneContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return null;
        }

        String company = smsMsg.getCompany();
        String smsCode = smsMsg.getSmsCode();
        String title = TextUtils.isEmpty(company) ? smsMsg.getSender() : company;
        String content = mAppContext.getString(R.string.code_notification_content, smsCode);

        int notificationId = smsMsg.hashCode();

        Intent copyCodeIntent = CopyCodeReceiver.createIntent(smsCode);
        final PendingIntent contentIntent;
        if (Build.VERSION.SDK_INT >= 31) {
            // java.lang.IllegalArgumentException: com.android.phone: Targeting S+ (version 31 and above)
            // requires that one of FLAG_IMMUTABLE or FLAG_MUTABLE be specified when creating a PendingIntent.
            //
            // Strongly consider using FLAG_IMMUTABLE, only use FLAG_MUTABLE if some functionality depends on the
            // PendingIntent being mutable, e.g. if it needs to be used with inline replies or bubbles.
            // contentIntent = PendingIntent.getBroadcast(mPhoneContext, 0, copyCodeIntent, PendingIntent.FLAG_MUTABLE);

            // https://stackoverflow.com/a/69745644
            contentIntent = PendingIntent.getBroadcast(mPhoneContext, 0,
                    copyCodeIntent, PendingIntent.FLAG_UPDATE_CURRENT | (1<<25));
        } else {
            contentIntent = PendingIntent.getBroadcast(mPhoneContext,
                    0, copyCodeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Notification notification = new NotificationCompat.Builder(mAppContext, NotificationConst.CHANNEL_ID_SMSCODE_NOTIFICATION)
                .setSmallIcon(R.drawable.ic_app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(mAppContext.getResources(), R.drawable.ic_app_icon))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(mAppContext, R.color.ic_launcher_background))
                .setGroup(NotificationConst.GROUP_KEY_SMSCODE_NOTIFICATION)
                .build();

        manager.notify(notificationId, notification);
        XLog.d("Show notification succeed");

        if (XSPUtils.autoCancelCodeNotification(xsp)) {
            long retentionTime = XSPUtils.getNotificationRetentionTime(xsp) * 1000L;
            Bundle bundle = new Bundle();
            bundle.putLong(NOTIFY_RETENTION_TIME, retentionTime);
            bundle.putInt(NOTIFY_ID, notificationId);
            return bundle;
        }
        return null;
    }
}
