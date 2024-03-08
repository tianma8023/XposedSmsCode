package com.tianma.xsmscode.xp.hook.code;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.tianma.xsmscode.common.constant.PrefConst;
import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.common.utils.XSPUtils;
import com.tianma.xsmscode.data.db.entity.SmsMsg;
import com.tianma.xsmscode.xp.hook.code.action.impl.AutoInputAction;
import com.tianma.xsmscode.xp.hook.code.action.impl.CancelNotifyAction;
import com.tianma.xsmscode.xp.hook.code.action.impl.CopyToClipboardAction;
import com.tianma.xsmscode.xp.hook.code.action.impl.KillMeAction;
import com.tianma.xsmscode.xp.hook.code.action.impl.NotifyAction;
import com.tianma.xsmscode.xp.hook.code.action.impl.OperateSmsAction;
import com.tianma.xsmscode.xp.hook.code.action.impl.RecordSmsAction;
import com.tianma.xsmscode.xp.hook.code.action.impl.SmsParseAction;
import com.tianma.xsmscode.xp.hook.code.action.impl.ToastAction;
import com.tianma.xsmscode.xp.hook.code.action.impl.UploadAction;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XSharedPreferences;

public class CodeWorker {

    private final Context mPhoneContext;
    private final Context mPluginContext;
    private final XSharedPreferences xsp;
    private final Intent mSmsIntent;

    private final Handler mUIHandler;

    private final ScheduledExecutorService mScheduledExecutor;

    CodeWorker(Context pluginContext, Context phoneContext, Intent smsIntent) {
        mPluginContext = pluginContext;
        mPhoneContext = phoneContext;
        xsp = new XSharedPreferences(BuildConfig.APPLICATION_ID, PrefConst.PREF_NAME);
        mSmsIntent = smsIntent;

        mUIHandler = new Handler(Looper.getMainLooper());

        mScheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public ParseResult parse() {
        if (!XSPUtils.isEnabled(xsp)) {
            XLog.i("XposedSmsCode disabled, exiting");
            return null;
        }

        boolean verboseLog = XSPUtils.isVerboseLogMode(xsp);
        if (verboseLog) {
            XLog.setLogLevel(Log.VERBOSE);
        } else {
            XLog.setLogLevel(BuildConfig.LOG_LEVEL);
        }

        SmsParseAction smsParseAction = new SmsParseAction(mPluginContext, mPhoneContext, null, xsp);
        smsParseAction.setSmsIntent(mSmsIntent);
        ScheduledFuture<Bundle> smsParseFuture = mScheduledExecutor.schedule(smsParseAction, 0, TimeUnit.MILLISECONDS);

        final SmsMsg smsMsg;
        try {
            Bundle parseBundle = smsParseFuture.get();
            if (parseBundle == null) {
                // the SMS message doesn't contain verification code
                return null;
            }

            boolean duplicated = parseBundle.getBoolean(SmsParseAction.SMS_DUPLICATED, false);
            if (duplicated) {
                return buildParseResult();
            }

            smsMsg = parseBundle.getParcelable(SmsParseAction.SMS_MSG);
        } catch (Exception e) {
            XLog.e("Error occurs when get SmsParseAction call value", e);
            return null;
        }


        // 复制到剪切板 Action
        mUIHandler.post(new CopyToClipboardAction(mPluginContext, mPhoneContext, smsMsg, xsp));

        // 显示Toast Action
        mUIHandler.post(new ToastAction(mPluginContext, mPhoneContext, smsMsg, xsp));

        // 上传 Action
        mUIHandler.post(new UploadAction(mPluginContext, mPhoneContext, smsMsg, xsp));



        // 自动输入 Action
        if (XSPUtils.autoInputCodeEnabled(xsp)) {
            AutoInputAction autoInputAction = new AutoInputAction(mPluginContext, mPhoneContext, smsMsg, xsp);
            long autoInputDelay = XSPUtils.getAutoInputCodeDelay(xsp) * 1000L;
            mScheduledExecutor.schedule(autoInputAction, autoInputDelay, TimeUnit.MILLISECONDS);
        }

        // 显示通知 Action
        NotifyAction notifyAction = new NotifyAction(mPluginContext, mPhoneContext, smsMsg, xsp);
        ScheduledFuture<Bundle> notificationFuture = mScheduledExecutor.schedule(notifyAction, 0, TimeUnit.MILLISECONDS);

        // 记录验证码短信 Action
        RecordSmsAction recordSmsAction = new RecordSmsAction(mPluginContext, mPhoneContext, smsMsg, xsp);
        mScheduledExecutor.schedule(recordSmsAction, 0, TimeUnit.MILLISECONDS);

        // 操作验证码短信（标记为已读 或者 删除） Action
        OperateSmsAction operateSmsAction = new OperateSmsAction(mPluginContext, mPhoneContext, smsMsg, xsp);
        mScheduledExecutor.schedule(operateSmsAction, 3000, TimeUnit.MILLISECONDS);

        // 自杀 Action
        KillMeAction action = new KillMeAction(mPluginContext, mPhoneContext, smsMsg, xsp);
        mScheduledExecutor.schedule(action, 4000, TimeUnit.MILLISECONDS);

        try {
            // 清除通知
            Bundle bundle = notificationFuture.get();
            if (bundle != null && bundle.containsKey(NotifyAction.NOTIFY_RETENTION_TIME)) {
                long delay = bundle.getLong(NotifyAction.NOTIFY_RETENTION_TIME, 0L);
                int notificationId = bundle.getInt(NotifyAction.NOTIFY_ID, 0);
                CancelNotifyAction cancelNotifyAction = new CancelNotifyAction(mPluginContext, mPhoneContext, smsMsg, xsp);
                cancelNotifyAction.setNotificationId(notificationId);

                mScheduledExecutor.schedule(cancelNotifyAction, delay, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            XLog.e("Error in notification future get()", e);
        }

        return buildParseResult();
    }

    private ParseResult buildParseResult() {
        ParseResult parseResult = new ParseResult();
        parseResult.setBlockSms(XSPUtils.blockSmsEnabled(xsp));
        return parseResult;
    }
}
