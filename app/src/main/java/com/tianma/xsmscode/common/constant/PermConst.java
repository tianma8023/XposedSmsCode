package com.tianma.xsmscode.common.constant;

import android.Manifest;
import android.os.Build;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.tianma.xsmscode.xp.hook.code.SmsHandlerHook;

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

        // Backup import or export
        smsCodePermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        smsCodePermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

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
