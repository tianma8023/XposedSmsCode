package com.tianma.xsmscode.ui.block;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.tianma.xsmscode.data.db.entity.AppInfo;

public class AppInfoUtils {

    private AppInfoUtils() {
    }

    public static AppInfo getAppInfo(PackageManager pm, PackageInfo packageInfo) {
        String label = pm.getApplicationLabel(packageInfo.applicationInfo).toString();
        String packageName = packageInfo.packageName;
        return new AppInfo(label, packageName);
    }

    public static AppInfo getAppInfo(PackageManager pm, ApplicationInfo applicationInfo) {
        String label = pm.getApplicationLabel(applicationInfo).toString();
        String packageName = applicationInfo.packageName;
        return new AppInfo(label, packageName);
    }

}
