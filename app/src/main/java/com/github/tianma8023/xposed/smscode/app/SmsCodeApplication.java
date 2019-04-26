package com.github.tianma8023.xposed.smscode.app;

import android.app.Application;
import android.app.NotificationManager;
import android.os.Build;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.github.tianma8023.xposed.smscode.R;
import com.github.tianma8023.xposed.smscode.constant.NotificationConst;
import com.github.tianma8023.xposed.smscode.migrate.TransitionTask;
import com.github.tianma8023.xposed.smscode.utils.NotificationUtils;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SmsCodeApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        initBugly();

        initNotificationChannel();
        performTransitionTask();
    }

    private void initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = NotificationConst.CHANNEL_ID_SMSCODE_NOTIFICATION;
            String channelName = getString(R.string.channel_name_smscode_notification);
            NotificationUtils.createNotificationChannel(this,
                    channelId, channelName, NotificationManager.IMPORTANCE_MIN);
        }
    }

    // tencent bugly initialization
    private void initBugly() {
        CrashReport.initCrashReport(getApplicationContext(), "4a63c8499a", BuildConfig.DEBUG);
    }

    // data transition task
    private void performTransitionTask() {
        Executor singlePool = Executors.newSingleThreadExecutor();
        singlePool.execute(new TransitionTask(this));
    }

}
