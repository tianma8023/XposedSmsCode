package com.github.tianma8023.xposed.smscode.xp.service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.hardware.input.InputManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import com.github.tianma8023.xposed.smscode.aidl.ISmsCodeService;
import com.github.tianma8023.xposed.smscode.utils.XLog;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedHelpers;

public class CodeService extends ISmsCodeService.Stub {

    private static final String SERVICE_MANAGER_CLS = "android.os.ServiceManager";

    private static final String SERVICE_NAME = "xposed_sms_code";

    // System Context
    private Context mContext;
    private static ISmsCodeService mClient;

    public CodeService(Context context) {
        mContext = context;
    }

    public static void register(Context context, ClassLoader classLoader) {
        if (context == null) {
            XLog.e("context is null");
        }
        Class<?> svcManager = XposedHelpers.findClass(SERVICE_MANAGER_CLS, classLoader);

        CodeService codeService = new CodeService(context);
        XposedHelpers.callStaticMethod(svcManager,
                /* methodName */"addService",
                /* name       */getServiceName(),
                /* service    */ codeService,
                /* allowIsolated */ true);

        XLog.d("register code service succeed");
    }

    private static String getServiceName() {
        // 5.0 之后，selinux "user." 前缀
//        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? "user." : "") + SERVICE_NAME;
        return SERVICE_NAME;
    }

    @SuppressLint("PrivateApi")
    public static ISmsCodeService getService() {
        if (mClient == null) {
            try {
                Class<?> svcManager = Class.forName(SERVICE_MANAGER_CLS);
                Method getServiceMethod = svcManager.getDeclaredMethod("getService", String.class);
                IBinder binder = (IBinder) getServiceMethod.invoke(null, getServiceName());
                mClient = ISmsCodeService.Stub.asInterface(binder);
            } catch (Exception e) {
                XLog.e("error occurs when get CodeService.", e);
            }
        }
        return mClient;
    }

    @Override
    public void autoInputText(String text) throws RemoteException {
//        printProcessName();
        inputText(text);
    }

    private void printProcessName() {
        try {
            Class<?> activityThreadClass = XposedHelpers.findClass("android.app.ActivityThread", Thread.currentThread().getContextClassLoader());
            String processName = (String) XposedHelpers.callStaticMethod(activityThreadClass, "currentProcessName");
            String packageName = (String) XposedHelpers.callStaticMethod(activityThreadClass, "currentPackageName");
            XLog.i("CodeService processName: %s", processName);
            XLog.i("CodeService packageName: %s", packageName);
            XLog.i("Current ProcessName: %s", getCurrentProcessName());
        } catch (Exception e) {
            XLog.e("error occurs when print process name.", e);
        }
    }

    private String getCurrentProcessName() {
        String curProcessName = "";
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
                if (processInfo.pid == pid) {
                    curProcessName = processInfo.processName;
                    break;
                }
            }
        }
        return curProcessName;
    }

    /**
     * refer: com.android.commands.input.Input#sendText()
     * @param text
     */
    private void inputText(String text) {
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
    private void injectKeyEvent(KeyEvent keyEvent) {
        try {
            InputManager inputManager = (InputManager) XposedHelpers.callStaticMethod(InputManager.class, "getInstance");

            int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH =
                    XposedHelpers.getStaticIntField(InputManager.class, "INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH");

            Class<?>[] paramTypes = {
                    KeyEvent.class,
                    int.class,
            };

            Object[] args = {
                    keyEvent,
                    INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH,
            };
            XposedHelpers.callMethod(inputManager, "injectInputEvent", paramTypes, args);
        } catch (Exception e) {
            e.printStackTrace();
            XLog.e("error occurs when injectKeyEvent", e);
        }
    }
}
