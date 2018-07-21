package com.github.tianma8023.xposed.smscode.utils;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;

import com.github.tianma8023.xposed.smscode.BuildConfig;

import java.util.List;

/**
 * AccessibilityService 相关 Utils
 */
public class AccessibilityUtils {

    private AccessibilityUtils() {
    }

    private static AccessibilityManager getAccessibilityManager(Context context) {
        return (AccessibilityManager) context.getApplicationContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    /**
     * 检查当前辅助服务是否已启用
     *
     * @param context   context
     * @param serviceId serviceId
     * @return 是否已启用
     */
    public static boolean checkAccessibilityEnabled(Context context, String serviceId) {
        AccessibilityManager accessibilityManager = getAccessibilityManager(context);
        List<AccessibilityServiceInfo> accessibilityServiceInfoList =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo info : accessibilityServiceInfoList) {
            if (info.getId().equals(serviceId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 前往开启无障碍服务界面
     *
     * @param context context
     */
    public static void gotoAccessibility(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 获取无障碍服务的ID
     *
     * @param serviceClz
     * @return
     */
    public static String getAccessibilityServiceId(Class<?> serviceClz) {
        // eg.
        // service Class: com.github.tianma8023.xposed.smscode.service.accessibility.SmsCodeAutoInputService
        // package name: com.github.tianma8023.xposed.smscode
        // accessibility service id : com.github.tianma8023.xposed.smscode/.service.accessibility.SmsCodeAutoInputService
        String packageName = BuildConfig.APPLICATION_ID;
        String serviceClzName = serviceClz.getName();
        int index = serviceClzName.indexOf(packageName);
        if (index != -1) {
            return packageName + "/" + serviceClzName.substring(index + packageName.length());
        }
        return serviceClzName;
    }

    /**
     * 获取无障碍服务的名称
     *
     * @param serviceClz
     * @return
     */
    public static String getAccessibilityServiceName(Class<?> serviceClz) {
        // eg.
        // serviceClassName = com.github.tianma8023.xposed.smscode.service.accessibility.SmsCodeAutoInputService
        // packageName = com.github.tianma8023.xposed.smscode
        // accessibility service id : ${packageName}/${serviceClassName}
        String packageName = BuildConfig.APPLICATION_ID;
        String serviceClzName = serviceClz.getName();
        return packageName + '/' + serviceClzName;
    }
}
