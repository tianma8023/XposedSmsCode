package com.github.tianma8023.xposed.smscode.service;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

/**
 * 处理验证码的Service
 */
public class SmsCodeService extends JobIntentService {

    private static final int JOB_ID = 0x100;
    public static final String EXTRA_KEY_SMS_INTENT = "key_sms_intent";

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, SmsCodeService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Intent smsIntent = intent.getParcelableExtra(EXTRA_KEY_SMS_INTENT);
        new SmsCodeTask(this, smsIntent).run();
    }
}
