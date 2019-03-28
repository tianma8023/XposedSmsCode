package com.github.tianma8023.xposed.smscode.constant;

import android.Manifest;
import android.os.Build;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.github.tianma8023.xposed.smscode.xp.hook.SmsHandlerHook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Permission Constants
 */
public class PermConst {

    public final static Map<String, List<String>> PACKAGE_PERMISSIONS;

    static {
        PACKAGE_PERMISSIONS = new HashMap<>();

        List<String> smsCodePermissions = new ArrayList<>();

        // umeng sdk integration
        // <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
        // <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
        // <uses-permission android:name="android.permission.INTERNET"/>
        // <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
        // tencent bugly integration
        // <uses-permission android:name="android.permission.READ_LOGS" />
        smsCodePermissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        smsCodePermissions.add(Manifest.permission.ACCESS_WIFI_STATE);
        smsCodePermissions.add(Manifest.permission.INTERNET);
        smsCodePermissions.add(Manifest.permission.READ_PHONE_STATE);
        smsCodePermissions.add(Manifest.permission.READ_LOGS);

        // JobIntentService
        // <uses-permission android:name="android.permission.WAKE_LOCK"/>
        // smsCodePermissions.add(Manifest.permission.WAKE_LOCK);

        // Enable/Disable AccessibilityService programmatically
//        smsCodePermissions.add(Manifest.permission.WRITE_SETTINGS);
//        smsCodePermissions.add(Manifest.permission.WRITE_SECURE_SETTINGS);

        // Backup import or export
        smsCodePermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        smsCodePermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

//        // READ_SMS for Mark SMS as read & Delete extracted verification SMS
//        smsCodePermissions.add(Manifest.permission.READ_SMS);
//        // api version < android M
//        smsCodePermissions.add("android.permission.WRITE_SMS");
//
//        // Permission for grant AppOps permissions
//        if (Build.VERSION.SDK_INT >= 28) {
//            // Android P
//            smsCodePermissions.add("android.permission.MANAGE_APP_OPS_MODES");
//        } else {
//            // android 4.4 ~ 8.1
//            smsCodePermissions.add("android.permission.UPDATE_APP_OPS_STATS");
//        }

        String smsCodePackage = BuildConfig.APPLICATION_ID;
        PACKAGE_PERMISSIONS.put(smsCodePackage, smsCodePermissions);

        List<String> phonePermissions = new ArrayList<>();
        // permission for InputManager#injectInputEvent();
        phonePermissions.add("android.permission.INJECT_EVENTS");

        // permission for kill background process - ActivityManagerService#
        phonePermissions.add(Manifest.permission.KILL_BACKGROUND_PROCESSES);

        // READ_SMS for Mark SMS as read & Delete extracted verification SMS
        phonePermissions.add(Manifest.permission.READ_SMS);
        // api version < android M
        phonePermissions.add("android.permission.WRITE_SMS");

        // Permission for grant AppOps permissions
        if (Build.VERSION.SDK_INT >= 28) {
            // Android P
            phonePermissions.add("android.permission.MANAGE_APP_OPS_MODES");
        } else {
            // android 4.4 ~ 8.1
            phonePermissions.add("android.permission.UPDATE_APP_OPS_STATS");
        }

        PACKAGE_PERMISSIONS.put(SmsHandlerHook.ANDROID_PHONE_PACKAGE, phonePermissions);
    }

}
