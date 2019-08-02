package com.tianma.xsmscode.ui.app;

import android.app.NotificationManager;
import android.os.Build;

import com.github.tianma8023.xposed.smscode.R;
import com.tianma.xsmscode.common.constant.NotificationConst;
import com.tianma.xsmscode.common.utils.NotificationUtils;
import com.tianma.xsmscode.data.eventbus.MyEventBusIndex;
import com.tianma.xsmscode.feature.migrate.TransitionTask;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;

public class SmsCodeApplication extends DaggerApplication {

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerApplicationComponent.factory().create(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        installDefaultEventBus();
        initNotificationChannel();
        performTransitionTask();
    }

    private void installDefaultEventBus() {
        EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();
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
