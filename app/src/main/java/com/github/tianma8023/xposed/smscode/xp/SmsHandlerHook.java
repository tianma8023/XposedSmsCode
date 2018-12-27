package com.github.tianma8023.xposed.smscode.xp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Telephony;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.github.tianma8023.xposed.smscode.receiver.SmsCodeReceiver;
import com.github.tianma8023.xposed.smscode.service.SmsCodeService;
import com.github.tianma8023.xposed.smscode.utils.XLog;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hook class com.android.internal.telephony.InBoundSmsHandler
 */
public class SmsHandlerHook implements IHook {

    private static final String TELEPHONY_PACKAGE = "com.android.internal.telephony";
    private static final String SMS_HANDLER_CLASS = TELEPHONY_PACKAGE + ".InboundSmsHandler";
    private static final String SMSCODE_PACKAGE = BuildConfig.APPLICATION_ID;

    private Context mModContext;
//    private Context mAppContext;

    @Override
    public void onLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if ("com.android.phone".equals(lpparam.packageName)) {
            XLog.i("SmsCode initializing");
            printDeviceInfo();
            try {
                hookSmsHandler(lpparam);
            } catch (Throwable e) {
                XLog.e("Failed to hook SmsHandler", e);
                throw e;
            }
            XLog.i("SmsCode initialize completely");
        }
    }

    @SuppressWarnings("deprecation")
    private static void printDeviceInfo() {
        XLog.i("Phone manufacturer: %s", Build.MANUFACTURER);
        XLog.i("Phone model: %s", Build.MODEL);
        XLog.i("Android version: %s", Build.VERSION.RELEASE);
        int xposedVersion;
        try {
            xposedVersion = XposedBridge.getXposedVersion();
        } catch (Throwable e) {
            xposedVersion = XposedBridge.XPOSED_BRIDGE_VERSION;
        }
        XLog.i("Xposed bridge version: %d", xposedVersion);
        XLog.i("SmsCode version: %s (%d)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
    }

    private void hookSmsHandler(XC_LoadPackage.LoadPackageParam lpparam) {
        hookConstructor(lpparam);
        hookDispatchIntent(lpparam);
    }

    private void hookConstructor(XC_LoadPackage.LoadPackageParam lpparam) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            hookConstructor24(lpparam);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            hookConstructor19(lpparam);
        }
    }

    private void hookConstructor24(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.i("Hooking InboundSmsHandler constructor for android v24+");
        XposedHelpers.findAndHookConstructor(SMS_HANDLER_CLASS, lpparam.classLoader,
                /* name                 */ String.class,
                /* context              */ Context.class,
                /* storageMonitor       */ TELEPHONY_PACKAGE + ".SmsStorageMonitor",
                /* phone                */ TELEPHONY_PACKAGE + ".Phone",
                /* cellBroadcastHandler */ TELEPHONY_PACKAGE + ".CellBroadcastHandler",
                new ConstructorHook());
    }

    private void hookConstructor19(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.i("Hooking InboundSmsHandler constructor for Android v19+");
        XposedHelpers.findAndHookConstructor(SMS_HANDLER_CLASS, lpparam.classLoader,
                /*                 name */ String.class,
                /*              context */ Context.class,
                /*       storageMonitor */ TELEPHONY_PACKAGE + ".SmsStorageMonitor",
                /*                phone */ TELEPHONY_PACKAGE + ".PhoneBase",
                /* cellBroadcastHandler */ TELEPHONY_PACKAGE + ".CellBroadcastHandler",
                new ConstructorHook());
    }

    private void hookDispatchIntent(XC_LoadPackage.LoadPackageParam lpparam) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hookDispatchIntent23(lpparam);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            hookDispatchIntent21(lpparam);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            hookDispatchIntent19(lpparam);
        }
    }

    private void hookDispatchIntent19(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.i("Hooking dispatchIntent() for Android v19+");
        XposedHelpers.findAndHookMethod(SMS_HANDLER_CLASS, lpparam.classLoader, "dispatchIntent",
                /*         intent */ Intent.class,
                /*     permission */ String.class,
                /*          appOp */ int.class,
                /* resultReceiver */ BroadcastReceiver.class,
                new DispatchIntentHook(3));
    }

    private void hookDispatchIntent21(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.i("Hooking dispatchIntent() for Android v21+");
        XposedHelpers.findAndHookMethod(SMS_HANDLER_CLASS, lpparam.classLoader, "dispatchIntent",
                /*         intent */ Intent.class,
                /*     permission */ String.class,
                /*          appOp */ int.class,
                /* resultReceiver */ BroadcastReceiver.class,
                /*           user */ UserHandle.class,
                new DispatchIntentHook(3));
    }

    private void hookDispatchIntent23(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.i("Hooking dispatchIntent() for Android v23+");
        XposedHelpers.findAndHookMethod(SMS_HANDLER_CLASS, lpparam.classLoader, "dispatchIntent",
                /*         intent */ Intent.class,
                /*     permission */ String.class,
                /*          appOp */ int.class,
                /*           opts */ Bundle.class,
                /* resultReceiver */ BroadcastReceiver.class,
                /*           user */ UserHandle.class,
                new DispatchIntentHook(4));
    }

    private class ConstructorHook extends XC_MethodHook {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            try {
                afterConstructorHandler(param);
            } catch (Throwable e) {
                XLog.e("Error occurred in constructor hook", e);
                throw e;
            }
        }
    }

    private void afterConstructorHandler(XC_MethodHook.MethodHookParam param) {
        Context context = (Context) param.args[1];
        if (mModContext == null /*|| mAppContext == null*/) {
            mModContext = context;
//            try {
//                mAppContext = mModContext.createPackageContext(SMSCODE_PACKAGE,
//                        Context.CONTEXT_IGNORE_SECURITY);
//            } catch (Exception e) {
//                XLog.e("Create app context failed: %s", e);
//            }
        }
    }

    private class DispatchIntentHook extends XC_MethodHook {
        private final int mReceiverIndex;

        DispatchIntentHook(int receiverIndex) {
            mReceiverIndex = receiverIndex;
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            try {
                beforeDispatchIntentHandler(param, mReceiverIndex);
            } catch (Throwable e) {
                XLog.e("Error occurred in dispatchIntent() hook, ", e);
                throw e;
            }
        }
    }

    private void beforeDispatchIntentHandler(XC_MethodHook.MethodHookParam param, int receiverIndex) {
        Intent intent = (Intent) param.args[0];
        String action = intent.getAction();

        // We only care about the initial SMS_DELIVER intent,
        // the rest are irrelevant
        if (!Telephony.Sms.Intents.SMS_DELIVER_ACTION.equals(action)) {
            return;
        }

//        registerCommandReceiver();

        // Send a broadcast, let receiver handle the rest of the works.
        Intent broadcastIntent = new Intent();
        broadcastIntent.setComponent(new ComponentName(SMSCODE_PACKAGE, SmsCodeReceiver.class.getName()));
        broadcastIntent.putExtra(SmsCodeService.EXTRA_KEY_SMS_INTENT, intent);
        mModContext.sendBroadcast(broadcastIntent);

//        mCountDownLatch = new CountDownLatch(1);
//        try {
//            mCountDownLatch.await(12, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        unregisterCommandReceiver();
//
//        if (mBlockSmsBroadcast) {
//            sendEventBroadcastComplete(param.thisObject);
//            param.setResult(null);
//            mBlockSmsBroadcast = false;
//        }
    }

//    private CountDownLatch mCountDownLatch;
//    private boolean mBlockSmsBroadcast = false;
//
//    public static final String ACTION_HANDLE_SMS = SMSCODE_PACKAGE + ".action_handle_sms";
//    public static final String EXTRA_BLOCK_SMS_BROADCAST = "extra_block_sms_broadcast";
//
//    private class CommandReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (ACTION_HANDLE_SMS.equals(action)) {
//                mBlockSmsBroadcast = intent.getBooleanExtra(EXTRA_BLOCK_SMS_BROADCAST, false);
//                if (mCountDownLatch != null && mCountDownLatch.getCount() == 1) {
//                    mCountDownLatch.countDown();
//                }
//            }
//        }
//    }
//
//    private CommandReceiver mCommandReceiver;
//
//    private void registerCommandReceiver() {
//        if (mCommandReceiver == null) {
//            mCommandReceiver = new CommandReceiver();
//            IntentFilter intentFilter = new IntentFilter();
//            intentFilter.addAction(ACTION_HANDLE_SMS);
//            mModContext.registerReceiver(mCommandReceiver, intentFilter);
//        }
//    }
//
//    private void unregisterCommandReceiver() {
//        if (mCommandReceiver != null) {
//            mModContext.unregisterReceiver(mCommandReceiver);
//            mCommandReceiver = null;
//        }
//    }
//
//    private static final int EVENT_BROADCAST_COMPLETE = 3;
//
//    private void sendEventBroadcastComplete(Object inboundSmsHandler) {
//        XLog.d("send event(EVENT_BROADCAST_COMPLETE)");
//        XposedHelpers.callMethod(inboundSmsHandler, "sendMessage", EVENT_BROADCAST_COMPLETE);
//    }

}
