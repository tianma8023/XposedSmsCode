package com.github.tianma8023.xposed.smscode.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.github.tianma8023.xposed.smscode.R;
import com.github.tianma8023.xposed.smscode.constant.INotificationConstants;

public class SmsCodeApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = INotificationConstants.CHANNEL_ID_FOREGROUND_SERVICE;
            String channelName = getString(R.string.channel_name_foreground_service);
            createNotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MIN);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

}
