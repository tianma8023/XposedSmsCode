package com.tianma.xsmscode.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;

import com.github.tianma8023.xposed.smscode.BuildConfig;

import java.util.ArrayList;
import java.util.Arrays;
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
     * @param serviceClz accessibility service class
     * @return ID of accessibility service
     */
    public static String getServiceId(Class<? extends AccessibilityService> serviceClz) {
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
     * @param serviceClz accessibility service class
     * @return name of accessibility service
     */
    public static String getServiceName(Class<? extends AccessibilityService> serviceClz) {
        // eg.
        // serviceClassName = com.github.tianma8023.xposed.smscode.service.accessibility.SmsCodeAutoInputService
        // packageName = com.github.tianma8023.xposed.smscode
        // accessibility service id : ${packageName}/${serviceClassName}
        String packageName = BuildConfig.APPLICATION_ID;
        String serviceClzName = serviceClz.getName();
        return packageName + '/' + serviceClzName;
    }


    /**
     * 获取已启用的 Accessibility Service 列表(无需ROOT)
     *
     * @param context context
     * @return null if error occurs in commands.
     */
    private static List<String> getEnabledAccessibilityServices(Context context) {
        String enabledAccessibilityServices = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        List<String> serviceList = new ArrayList<>();
        if (!TextUtils.isEmpty(enabledAccessibilityServices)) {
            serviceList.addAll(Arrays.asList(enabledAccessibilityServices.split(":")));
        }
        XLog.d("Enabled services = %s", serviceList.toString());
        return serviceList;
    }

    /**
     * 设置无障碍服务
     *
     * @param context context
     * @param enabledServices Enabled Accessibility Service List
     * @return 是否设置成功
     */
    private static boolean setEnabledAccessibilityServices(Context context, List<String> enabledServices) {
        StringBuilder sb = new StringBuilder();
        String emptyOrColon = ""; // 空字符 or 冒号分隔符
        for (String service : enabledServices) {
            sb.append(emptyOrColon).append(service);
            emptyOrColon = ":";
        }
        String enabledServicesStr = sb.toString();
        return Settings.Secure.putString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, enabledServicesStr);
    }


    /**
     * 启用指定无障碍服务（无需ROOT）
     *
     * @param context context
     * @param accessibilityServiceName Accessibility Service Name
     * @return 是否成功启用
     */
    public static boolean enableAccessibilityService(Context context, String accessibilityServiceName) {
        try {
            List<String> enabledServices = getEnabledAccessibilityServices(context);
            boolean enabled;
            if (enabledServices == null) {
                enabled = false;
            } else if (enabledServices.contains(accessibilityServiceName)) {
                enabled = true;
            } else {
                enabledServices.add(accessibilityServiceName);
                enabled = setEnabledAccessibilityServices(context, enabledServices);
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                if (enabled) {
                    // need to let accessibility_enabled = 1
                    boolean accessibilityEnabled = isAccessibilityEnabled(context);
                    XLog.d("Accessibility_enabled: " + accessibilityEnabled);
                    if (!accessibilityEnabled) {
                        accessibilityEnabled = setAccessibilityEnabled(context, true);
                        XLog.d("Put accessibility_enabled: " + accessibilityEnabled);
                        if (!accessibilityEnabled) {
                            return false;
                        }
                    }
                }
            }
            return enabled;
        } catch (Exception e) {
            XLog.e("Error occurs enableAccessibilityService by Settings", e);
            return false;
        }
    }

    /**
     * 关闭指定无障碍服务（无需ROOT）
     *
     * @param context context
     * @param accessibilityServiceName Accessibility Service Name
     * @return 是否成功关闭
     */
    public static boolean disableAccessibilityService(Context context, String accessibilityServiceName) {
        try {
            List<String> enabledServices = getEnabledAccessibilityServices(context);
            if (enabledServices == null) {
                return false;
            }
            if (!enabledServices.contains(accessibilityServiceName)) {
                return true;
            }
            enabledServices.remove(accessibilityServiceName);
            return setEnabledAccessibilityServices(context, enabledServices);
        } catch (Exception e) {
            XLog.e("Error occurs disableAccessibilityService by Settings", e);
            return false;
        }
    }

    private static boolean setAccessibilityEnabled(Context context, boolean enabled) {
//        return Settings.Secure.putInt(context.getContentResolver(),
//                Settings.Secure.ACCESSIBILITY_ENABLED, enabled ? 1 : 0);
        return Settings.Secure.putString(context.getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED, enabled ? "1" : "0");

    }

    private static boolean isAccessibilityEnabled(Context context) {
//        return Settings.Secure.getInt(context.getContentResolver(),
//                Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1;
        String value = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED);
        return !TextUtils.isEmpty(value) && "1".equals(value.trim());
    }
}
