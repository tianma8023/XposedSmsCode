package com.github.tianma8023.xposed.smscode.xp.hook;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.input.InputManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Telephony;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.github.tianma8023.xposed.smscode.utils.XLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hook class com.android.internal.telephony.InBoundSmsHandler
 */
public class SmsHandlerHook extends AbsHook {

    public static final String ANDROID_PHONE_PACKAGE = "com.android.phone";

    private static final String TELEPHONY_PACKAGE = "com.android.internal.telephony";
    private static final String SMS_HANDLER_CLASS = TELEPHONY_PACKAGE + ".InboundSmsHandler";
    private static final String SMSCODE_PACKAGE = BuildConfig.APPLICATION_ID;

    private Context mPhoneContext;
    private Context mAppContext;

    @Override
    public void onLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (ANDROID_PHONE_PACKAGE.equals(lpparam.packageName)) {
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
        if (mPhoneContext == null /*|| mAppContext == null*/) {
            mPhoneContext = context;
            try {
                mAppContext = mPhoneContext.createPackageContext(SMSCODE_PACKAGE,
                        Context.CONTEXT_IGNORE_SECURITY);
            } catch (Exception e) {
                XLog.e("Create app context failed: %s", e);
            }
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

        ParseResult parseResult = new SmsCodeWorker(mAppContext, mPhoneContext, intent).parse();
        if (parseResult != null) {// parse succeed
            if (parseResult.isAutoInput()) {
                boolean inputResult = autoInputText(parseResult.getSmsMsg().getSmsCode());
                XLog.d("auto input result %b", inputResult);
            }

            if (parseResult.isBlockSms()) {
                XLog.d("blocking code SMS...");
                deleteRawTableAndSendMessage(param.thisObject, param.args[receiverIndex]);
                param.setResult(null);
            }
        }
    }

    private static final int EVENT_BROADCAST_COMPLETE = 3;

    private void deleteRawTableAndSendMessage(Object inboundSmsHandler, Object smsReceiver) {
        long token = Binder.clearCallingIdentity();
        try {
            deleteFromRawTable(inboundSmsHandler, smsReceiver);
        } catch (Throwable e) {
            XLog.e("error occurs when delete SMS data from raw table", e);
        } finally {
            Binder.restoreCallingIdentity(token);
        }

        sendEventBroadcastComplete(inboundSmsHandler);
    }

    private void sendEventBroadcastComplete(Object inboundSmsHandler) {
        XLog.d("send event(EVENT_BROADCAST_COMPLETE)");
        XposedHelpers.callMethod(inboundSmsHandler, "sendMessage", EVENT_BROADCAST_COMPLETE);
    }

    private void deleteFromRawTable(Object inboundSmsHandler, Object smsReceiver) throws ReflectiveOperationException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            deleteFromRawTable24(inboundSmsHandler, smsReceiver);
        } else {
            deleteFromRawTable19(inboundSmsHandler, smsReceiver);
        }
    }

    private void deleteFromRawTable19(Object inboundSmsHandler, Object smsReceiver) throws ReflectiveOperationException {
        XLog.d("delete raw SMS data from database on Android 19+");
        Object deleteWhere = XposedHelpers.getObjectField(smsReceiver, "mDeleteWhere");
        Object deleteWhereArgs = XposedHelpers.getObjectField(smsReceiver, "mDeleteWhereArgs");

        callDeclaredMethod(SMS_HANDLER_CLASS, inboundSmsHandler, "deleteFromRawTable",
                /* String deleteWhere       */ deleteWhere,
                /* String[] deleteWhereArgs */ deleteWhereArgs);
    }

    private void deleteFromRawTable24(Object inboundSmsHandler, Object smsReceiver) throws ReflectiveOperationException {
        XLog.d("delete raw SMS data from database on Android 24+");
        Object deleteWhere = XposedHelpers.getObjectField(smsReceiver, "mDeleteWhere");
        Object deleteWhereArgs = XposedHelpers.getObjectField(smsReceiver, "mDeleteWhereArgs");
        final int MARK_DELETED = 2;

        callDeclaredMethod(SMS_HANDLER_CLASS, inboundSmsHandler, "deleteFromRawTable",
                /* String deleteWhere       */ deleteWhere,
                /* String[] deleteWhereArgs */ deleteWhereArgs,
                /* int deleteType           */ MARK_DELETED);
    }

    private static Object callDeclaredMethod(String className, Object obj, String methodName, Object... args) throws InvocationTargetException, IllegalAccessException {
        // XposedHelpers#callMethod() 方法，不能反射调用 private 的方法
        // 而本方法可以反射调用指定类的 private 方法
        Class<?> clz = XposedHelpers.findClass(className, obj.getClass().getClassLoader());
        Method method = XposedHelpers.findMethodBestMatch(clz, methodName, args);
        return method.invoke(obj, args);
    }

    private boolean autoInputText(String text) {
        try {
            XLog.d("try auto input by InputManagerService");
            sendText(text);
            return true;
        } catch (Throwable throwable) {
            XLog.e("error occurs when auto input text", throwable);
            return false;
        }
    }

    /**
     * refer: com.android.commands.input.Input#sendText()
     *
     * @throws Throwable throwable throws if the caller has no android.permission.INJECT_EVENTS permission
     * @param text
     */
    private void sendText(String text) throws Throwable {
        int source = InputDevice.SOURCE_KEYBOARD;

        StringBuilder sb = new StringBuilder(text);

        boolean escapeFlag = false;
        for (int i = 0; i < sb.length(); i++) {
            if (escapeFlag) {
                escapeFlag = false;
                if (sb.charAt(i) == 's') {
                    sb.setCharAt(i, ' ');
                    sb.deleteCharAt(--i);
                }
            }
            if (sb.charAt(i) == '%') {
                escapeFlag = true;
            }
        }

        char[] chars = sb.toString().toCharArray();

        KeyCharacterMap kcm = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
        KeyEvent[] events = kcm.getEvents(chars);
        for (KeyEvent keyEvent : events) {
            if (source != keyEvent.getSource()) {
                keyEvent.setSource(source);
            }
            injectKeyEvent(keyEvent);
        }
    }

    /**
     * refer com.android.commands.input.Input#injectKeyEvent()
     */
    @SuppressLint("PrivateApi")
    private void injectKeyEvent(KeyEvent keyEvent) throws Throwable {
        InputManager inputManager = (InputManager) XposedHelpers.callStaticMethod(InputManager.class, "getInstance");

        int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH =
                XposedHelpers.getStaticIntField(InputManager.class, "INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH");

        Class<?>[] paramTypes = {KeyEvent.class, int.class,};
        Object[] args = {keyEvent, INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH,};

        XposedHelpers.callMethod(inputManager, "injectInputEvent", paramTypes, args);
    }

}
