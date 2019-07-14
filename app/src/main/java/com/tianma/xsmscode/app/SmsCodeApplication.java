package com.tianma.xsmscode.app;

import android.app.Application;
import android.app.NotificationManager;
import android.os.Build;

import com.github.tianma8023.xposed.smscode.R;
import com.tianma.xsmscode.constant.NotificationConst;
import com.tianma.xsmscode.migrate.TransitionTask;
import com.tianma.xsmscode.utils.NotificationUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SmsCodeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

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

    // data transition task
    private void performTransitionTask() {
        Executor singlePool = Executors.newSingleThreadExecutor();
        singlePool.execute(new TransitionTask(this));
    }

}
