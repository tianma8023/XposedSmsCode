package com.github.tianma8023.xposed.smscode.xp.hook;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.github.tianma8023.xposed.smscode.utils.XSPUtils;
import com.github.tianma8023.xposed.smscode.utils.SettingsUtils;
import com.github.tianma8023.xposed.smscode.utils.XLog;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hook android.app.NotificationManager for block code SMS notification.
 */
public class NotificationManagerHook extends AbsHook  {

    private Context mContext;
    private XSharedPreferences mPreferences;

    @Override
    public void onLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (mContext != null) {
            String defSmsAppPackage = SettingsUtils.getDefaultSmsAppPackage(mContext);
            if (!TextUtils.isEmpty(defSmsAppPackage) && defSmsAppPackage.equals(lpparam.packageName)) {
                hookNotificationManager(lpparam);
            }
        } else {
            hookNotificationManager(lpparam);
        }
    }

    private void hookNotificationManager(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            hookConstructor();
            hookNotify();
        } catch (Exception e) {
            XLog.e("failed to hook NotificationManager");
        }
    }

    private void hookConstructor() {
        XposedHelpers.findAndHookConstructor(NotificationManager.class,
                Context.class,
                Handler.class,
                new ConstructorHook());
    }

    private class ConstructorHook extends XC_MethodHook {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            afterConstructor(param);
        }
    }

    private void afterConstructor(XC_MethodHook.MethodHookParam param) {
        mContext = (Context) param.args[0];
        if (mPreferences == null) {
            mPreferences = new XSharedPreferences(BuildConfig.APPLICATION_ID);
        }
    }

    /**
     * NotificationManager#notify(String tag, int id, Notification notification)
     */
    private void hookNotify() {
        XposedHelpers.findAndHookMethod(NotificationManager.class,
                "notify",
                /*           tag */ String.class,
                /*            id */ int.class,
                /*  notification */ Notification.class,
                new NotifyHook());
    }

    private class NotifyHook extends XC_MethodHook {

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            beforeNotify(param);
        }
    }

    /**
     * before the method NotificationManager#notify()
     */
    private void beforeNotify(XC_MethodHook.MethodHookParam param) {
        if (!XSPUtils.isEnabled(mPreferences)) {
            return;
        }
        if (!XSPUtils.blockSmsEnabled(mPreferences)) {
            return;
        }

        String defSmsAppPackage = SettingsUtils.getDefaultSmsAppPackage(mContext);
        if (!mContext.getPackageName().equals(defSmsAppPackage)) {
            return;
        }

        Notification notification = (Notification) param.args[2];
        // message content
        CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
//        XLog.d("notification: %s", notification.toString());
//        XLog.d("notification extras: %s", notification.extras);
        XLog.d("notification title: %s", title);
        XLog.d("notification text: %s", text);
        XLog.d("notification ----------------------------");
        XLog.d("notification ----------------------------");
        if (TextUtils.isEmpty(text)) {
            return;
        }
//        if (SmsCodeUtils.containsCodeKeywords(mContext, text.toString())) {
////            NotificationManager manager = (NotificationManager) param.thisObject;
////            String tag = (String) param.args[0];
////            int id = (int) param.args[1];
////            manager.cancel(tag, id);
//            param.setResult(null);
//            XLog.d("blocked code SMS notification");
//        }
        param.setResult(null);
    }

}
