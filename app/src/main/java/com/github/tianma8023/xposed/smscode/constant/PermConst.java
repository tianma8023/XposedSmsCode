package com.github.tianma8023.xposed.smscode.constant;

import android.Manifest;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * Permission Constants
 */
public class PermConst {

    public final static List<String> PERMISSIONS_TO_GRANT;

    static {
        PERMISSIONS_TO_GRANT = new ArrayList<>();

        // <!-- umeng sdk integration -->
        // <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
        // <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
        // <uses-permission android:name="android.permission.INTERNET"/>
        // <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
        PERMISSIONS_TO_GRANT.add(Manifest.permission.ACCESS_NETWORK_STATE);
        PERMISSIONS_TO_GRANT.add(Manifest.permission.ACCESS_WIFI_STATE);
        PERMISSIONS_TO_GRANT.add(Manifest.permission.INTERNET);
        PERMISSIONS_TO_GRANT.add(Manifest.permission.READ_PHONE_STATE);

        // JobIntentService
        // <uses-permission android:name="android.permission.WAKE_LOCK"/>
        // PERMISSIONS_TO_GRANT.add(Manifest.permission.WAKE_LOCK);

        // Enable/Disable AccessibilityService programmatically
        PERMISSIONS_TO_GRANT.add(Manifest.permission.WRITE_SETTINGS);
        PERMISSIONS_TO_GRANT.add(Manifest.permission.WRITE_SECURE_SETTINGS);

        // Backup import or export
        PERMISSIONS_TO_GRANT.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        PERMISSIONS_TO_GRANT.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // READ_SMS for Mark SMS as read & Delete extracted verification SMS
        PERMISSIONS_TO_GRANT.add(Manifest.permission.READ_SMS);
        // api version < android M
        PERMISSIONS_TO_GRANT.add("android.permission.WRITE_SMS");

        int sdkInt = Build.VERSION.SDK_INT;
        // Permission for grant AppOps permissions
        if (sdkInt >= 28) {
            // Android P
            PERMISSIONS_TO_GRANT.add("android.permission.MANAGE_APP_OPS_MODES");
        } else {
            // android 4.4 ~ 8.1
            PERMISSIONS_TO_GRANT.add("android.permission.UPDATE_APP_OPS_STATS");
        }
    }

}
