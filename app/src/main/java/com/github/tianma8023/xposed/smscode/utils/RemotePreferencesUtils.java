package com.github.tianma8023.xposed.smscode.utils;

import android.content.Context;

import com.crossbowffs.remotepreferences.RemotePreferenceAccessException;
import com.crossbowffs.remotepreferences.RemotePreferences;
import com.github.tianma8023.xposed.smscode.constant.PrefConst;

public class RemotePreferencesUtils {

    private RemotePreferencesUtils() {
    }

    public static RemotePreferences getDefaultRemotePreferences(Context context) {
        return new RemotePreferences(context,
                PrefConst.REMOTE_PREF_AUTHORITY,
                PrefConst.REMOTE_PREF_NAME,
                true);
    }

    static boolean getBooleanPref(RemotePreferences mPreferences, String key, boolean defaultValue) {
        try {
            return mPreferences.getBoolean(key, defaultValue);
        } catch (RemotePreferenceAccessException e) {
            XLog.e("Failed to read preference: %s", key, e);
            return defaultValue;
        }
    }

    static String getStringPref(RemotePreferences preferences, String key, String defaultValue) {
        try {
            return preferences.getString(key, defaultValue);
        } catch (RemotePreferenceAccessException e) {
            XLog.e("Failed to read preference: %s", key, e);
            return defaultValue;
        }
    }
}
