package com.github.tianma8023.xposed.smscode.utils;

import com.crossbowffs.remotepreferences.RemotePreferenceAccessException;
import com.crossbowffs.remotepreferences.RemotePreferences;

public class RemotePreferencesUtils {

    private RemotePreferencesUtils() {}

    public static boolean getBooleanPref(RemotePreferences mPreferences, String key, boolean defaultValue) {
        try {
            return mPreferences.getBoolean(key, defaultValue);
        } catch (RemotePreferenceAccessException e) {
            XLog.e("Failed to read preference: %s", key, e);
            return defaultValue;
        }
    }
}
