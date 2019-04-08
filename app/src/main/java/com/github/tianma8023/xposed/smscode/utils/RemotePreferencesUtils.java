//package com.github.tianma8023.xposed.smscode.utils;
//
//import android.content.Context;
//
//import com.crossbowffs.remotepreferences.RemotePreferenceAccessException;
//import com.crossbowffs.remotepreferences.RemotePreferences;
//import com.github.tianma8023.xposed.smscode.constant.PrefConst;
//
//public class RemotePreferencesUtils {
//
//    private RemotePreferencesUtils() {
//    }
//
//    public static RemotePreferences getDefaultRemotePreferences(Context context) {
//        return new RemotePreferences(context,
//                PrefConst.REMOTE_PREF_AUTHORITY,
//                PrefConst.REMOTE_PREF_NAME,
//                true);
//    }
//
//    static boolean getBoolean(RemotePreferences mPreferences, String key, boolean defaultValue) {
//        try {
//            return mPreferences.getBoolean(key, defaultValue);
//        } catch (RemotePreferenceAccessException e) {
//            XLog.e("Failed to read preference: %s", key, e);
//            return defaultValue;
//        }
//    }
//
//    static void putBoolean(RemotePreferences preferences, String key, boolean value) {
//        try {
//            preferences.edit().putBoolean(key, value).apply();
//        } catch (RemotePreferenceAccessException e) {
//            XLog.d("Failed to write preference: %s", key, e);
//        }
//    }
//
//    static String getString(RemotePreferences preferences, String key, String defaultValue) {
//        try {
//            return preferences.getString(key, defaultValue);
//        } catch (RemotePreferenceAccessException e) {
//            XLog.e("Failed to read preference: %s", key, e);
//            return defaultValue;
//        }
//    }
//
//    static void putString(RemotePreferences preferences, String key, String value) {
//        try {
//            preferences.edit().putString(key, value).apply();
//        } catch (RemotePreferenceAccessException e) {
//            XLog.d("Failed to write preference: %s", key, e);
//        }
//    }
//
//    static int getInt(RemotePreferences preferences, String key, int defaultValue) {
//        try {
//            return preferences.getInt(key, defaultValue);
//        } catch (RemotePreferenceAccessException e) {
//            XLog.e("Failed to read preference: %s", key, e);
//            return defaultValue;
//        }
//    }
//
//    static void putInt(RemotePreferences preferences, String key, int value) {
//        try {
//            preferences.edit().putInt(key, value).apply();
//        } catch (RemotePreferenceAccessException e) {
//            XLog.d("Failed to write preference: %s", key, e);
//        }
//    }
//}
