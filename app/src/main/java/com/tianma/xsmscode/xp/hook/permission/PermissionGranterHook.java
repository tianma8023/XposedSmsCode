package com.tianma.xsmscode.xp.hook.permission;

import android.os.Build;

import com.tianma.xsmscode.xp.hook.BaseHook;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hook com.android.server.pm.PackageManagerService to grant permissions.
 */
public class PermissionGranterHook extends BaseHook {

    public static final String ANDROID_PACKAGE = "android";

    @Override
    public void onLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (ANDROID_PACKAGE.equals(lpparam.packageName) && ANDROID_PACKAGE.equals(lpparam.processName)) {
            ClassLoader classLoader = lpparam.classLoader;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // Android 9.0+
                new PermissionManagerServiceHook(classLoader).startHook();
            } else { // Android 5.0 ~ 8.1
                new PackageManagerServiceHook(classLoader).startHook();
            }
        }
    }

}
