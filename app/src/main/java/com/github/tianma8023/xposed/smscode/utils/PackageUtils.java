package com.github.tianma8023.xposed.smscode.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.github.tianma8023.xposed.smscode.constant.IConstants;

/**
 * 包相关工具类
 */
public class PackageUtils {

    private PackageUtils() {
    }

    public static boolean isWeChatInstalled(Context context) {
        return isPackageInstalled(context, IConstants.WECHAT_PACKAGE_NAME);
    }

    public static boolean isAlipayInstalled(Context context) {
        return isPackageInstalled(context, IConstants.ALIPAY_PACKAGE_NAME);
    }

    /**
     * 指定的包名对应的App是否已安装
     *
     */
    public static boolean isPackageInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
            return packageInfo != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

}
