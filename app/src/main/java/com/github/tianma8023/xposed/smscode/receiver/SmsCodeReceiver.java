package com.github.tianma8023.xposed.smscode.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.tianma8023.xposed.smscode.service.SmsCodeService;

public class SmsCodeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(SmsCodeService.EXTRA_KEY_SMS_INTENT)) {
            SmsCodeService.enqueueWork(context, intent);
        }
    }
}
