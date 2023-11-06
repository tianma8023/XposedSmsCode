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

            final int sdkInt = Build.VERSION.SDK_INT;
            if (sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                new PermissionManagerServiceHook34(classLoader).startHook();
            } else if (sdkInt >= Build.VERSION_CODES.TIRAMISU){ // Android 13+
                new PermissionManagerServiceHook33(classLoader).startHook();
            } else if (sdkInt >= Build.VERSION_CODES.S) { // Android 12~12L
                new PermissionManagerServiceHook31(classLoader).startHook();
            } else if (sdkInt >= Build.VERSION_CODES.R){ // Android 11
                new PermissionManagerServiceHook30(classLoader).startHook();
            } else if (sdkInt >= Build.VERSION_CODES.P) { // Android 9.0~10
                new PermissionManagerServiceHook(classLoader).startHook();
            } else { // Android 5.0 ~ 8.1
                new PackageManagerServiceHook(classLoader).startHook();
            }
        }
    }

}
