package com.tianma.xsmscode.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.tianma.xsmscode.common.constant.PrefConst;


/**
 * Common Shared preferences utils.
 */
public class PreferencesUtils {

    private PreferencesUtils() {

    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PrefConst.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean contains(Context context, String key) {
        return getPreferences(context).contains(key);
    }

    public static String getString(Context context, String key, String defValue) {
        return getPreferences(context).getString(key, defValue);
    }

    public static void putString(Context context, String key, String value) {
        getPreferences(context).edit().putString(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        return getPreferences(context).getBoolean(key, defValue);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        getPreferences(context).edit().putBoolean(key, value).apply();
    }

    public static int getInt(Context context, String key, int defValue) {
        return getPreferences(context).getInt(key, defValue);
    }

    public static void putInt(Context context, String key, int value) {
        getPreferences(context).edit().putInt(key, value).apply();
    }

    public static float getFloat(Context context, String key, float defValue) {
        return getPreferences(context).getFloat(key, defValue);
    }

    public static void putFloat(Context context, String key, float value) {
        getPreferences(context).edit().putFloat(key, value).apply();
    }

    public static long getLong(Context context, String key, long defValue) {
        return getPreferences(context).getLong(key, defValue);
    }

    public static void putLong(Context context, String key, long value) {
        getPreferences(context).edit().putLong(key, value).apply();
    }

}
