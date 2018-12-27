package com.github.tianma8023.xposed.smscode.utils;

import android.content.Context;
import android.provider.Settings;

/**
 * Utility for android.provider.Settings
 */
public class SettingsUtils {

    private SettingsUtils() {
    }

    private static String getSecureString(Context context, String key) {
        return Settings.Secure.getString(context.getContentResolver(), key);
    }

    /**
     * Get system default SMS app package
     * @return package name of default sms app
     */
    public static String getDefaultSmsAppPackage(Context context) {
        String key  = "sms_default_application";
        return getSecureString(context, key);
    }

}
