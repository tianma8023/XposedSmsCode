package com.tianma.xsmscode.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;

import com.github.tianma8023.xposed.smscode.BuildConfig;

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

    /**
     * Request ignore battery optimization
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("BatteryLife")
    public static void requestIgnoreBatteryOptimization(Context context) {
        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
        context.startActivity(intent);
    }

    /**
     * Go to ignore battery optimization settings.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void gotoIgnoreBatteryOptimizationSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        context.startActivity(intent);
    }
}
