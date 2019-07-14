//package com.tianma.xsmscode.receiver;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.support.v4.content.ContextCompat;
//
//import com.github.tianma8023.xposed.smscode.service.SmsCodeService;
//
//public class SmsCodeReceiver extends BroadcastReceiver {
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        if (intent.hasExtra(SmsCodeService.EXTRA_KEY_SMS_INTENT)) {
//            Intent extraSmsIntent = intent.getParcelableExtra(SmsCodeService.EXTRA_KEY_SMS_INTENT);
//            Intent smsCodeService = new Intent(context, SmsCodeService.class);
//            smsCodeService.putExtra(SmsCodeService.EXTRA_KEY_SMS_INTENT, extraSmsIntent);
//            // startForegroundService since Android Oreo
//            // startService before Android Oreo
//            ContextCompat.startForegroundService(context, smsCodeService);
//        }
//    }
//}
