package com.github.tianma8023.xposed.smscode.constant;

import android.Manifest;

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

        // READ_SMS for Mark SMS as read
        // PERMISSIONS_TO_GRANT.add(Manifest.permission.READ_SMS);

        // JobIntentService
        // <uses-permission android:name="android.permission.WAKE_LOCK"/>
        // PERMISSIONS_TO_GRANT.add(Manifest.permission.WAKE_LOCK);

        // Enable/Disable AccessibilityService programmatically
        PERMISSIONS_TO_GRANT.add(Manifest.permission.WRITE_SETTINGS);
        PERMISSIONS_TO_GRANT.add(Manifest.permission.WRITE_SECURE_SETTINGS);

        // Backup import or export
        PERMISSIONS_TO_GRANT.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        PERMISSIONS_TO_GRANT.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

}
