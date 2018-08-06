package com.github.tianma8023.xposed.smscode.utils;

import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.jaredrummler.android.shell.CommandResult;
import com.jaredrummler.android.shell.Shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Shell Command Utils
 */
public class ShellUtils {

    private static final String ENABLED_ACCESSIBILITY_SERVICES = Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES;
    private static final String ACCESSIBILITY_ENABLED = Settings.Secure.ACCESSIBILITY_ENABLED;

    private ShellUtils() {
    }

    /**
     * 获取已启用的 Accessibility Service 列表(需ROOT)
     *
     * @return null if error occurs in commands.
     */
    private static List<String> getEnabledAccessibilityServices() {
        CommandResult getResult = Shell.SU.run("settings get secure " + ENABLED_ACCESSIBILITY_SERVICES);
        if (getResult.isSuccessful()) {
            String enabledAccessibilityServices = getResult.getStdout();
            List<String> serviceList = new ArrayList<>();
            if (!TextUtils.isEmpty(enabledAccessibilityServices)) {
                serviceList.addAll(Arrays.asList(enabledAccessibilityServices.split(":")));
            }
            XLog.d("enabled services = %s", serviceList.toString());
            return serviceList;
        }
        XLog.e(getResult.getStderr());
        return null;
    }

    /**
     * 设置无障碍服务
     *
     * @param enabledServices Enabled Accessibility Service List
     * @return 是否设置成功
     */
    private static boolean setEnabledAccessibilityServices(List<String> enabledServices) {
        StringBuilder sb = new StringBuilder();
        String emptyOrColon = ""; // 空字符 or 冒号分隔符
        for (String service : enabledServices) {
            sb.append(emptyOrColon).append(service);
            emptyOrColon = ":";
        }
        String enabledServicesStr = sb.toString();
        CommandResult putResult = Shell.SU.run("settings put secure " +
                                    ENABLED_ACCESSIBILITY_SERVICES + " \"" + enabledServicesStr + "\"");
        return putResult.isSuccessful();
    }


    /**
     * 启用指定无障碍服务（需ROOT）
     *
     * @param accessibilityServiceName Accessibility Service Name
     * @return 是否成功启用
     */
    public static boolean enableAccessibilityService(String accessibilityServiceName) {
        try {
            List<String> enabledServices = getEnabledAccessibilityServices();
            boolean enabled;
            if (enabledServices == null) {
                enabled = false;
            } else if (enabledServices.contains(accessibilityServiceName)) {
                enabled = true;
            } else {
                enabledServices.add(accessibilityServiceName);
                enabled = setEnabledAccessibilityServices(enabledServices);
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (enabled) {
                    // need to let accessibility_enabled = 1
                    boolean accessibilityEnabled = isAccessibilityEnabled();
                    XLog.d("accessibility_enabled: " + accessibilityEnabled);
                    if (!accessibilityEnabled) {
                        accessibilityEnabled = setAccessibilityEnabled(true);
                        if (!accessibilityEnabled)
                            return false;
                    }
                    XLog.d("put accessibility_enabled: " + accessibilityEnabled);
                }
            }
            return enabled;
        } catch (Exception e) {
            XLog.e("error occurs enableAccessibilityService by Shell", e);
            return false;
        }
    }

    /**
     * 关闭指定无障碍服务（需ROOT）
     *
     * @param accessibilityServiceName Accessibility Service Name
     * @return 是否成功关闭
     */
    public static boolean disableAccessibilityService(String accessibilityServiceName) {
        try {
            List<String> enabledServices = getEnabledAccessibilityServices();
            if (enabledServices == null) {
                return false;
            }
            if (!enabledServices.contains(accessibilityServiceName)) {
                return true;
            }
            enabledServices.remove(accessibilityServiceName);
            return setEnabledAccessibilityServices(enabledServices);
        } catch (Exception e) {
            XLog.e("error occurs disableAccessibilityService by Shell", e);
            return false;
        }
    }

    private static boolean setAccessibilityEnabled(boolean enabled) {
        CommandResult putResult = Shell.SU.run("settings put secure " + ACCESSIBILITY_ENABLED + " " +
                (enabled ? 1 : 0));
        return putResult.isSuccessful();
    }

    private static boolean isAccessibilityEnabled() {
        CommandResult getResult = Shell.SU.run("settings get secure " + ACCESSIBILITY_ENABLED);
        if (getResult.isSuccessful()) {
            String numStr = getResult.getStdout();
            return "1".equals(numStr.trim());
        }
        return false;
    }

    public static boolean checkRootPermission() {
        CommandResult result = Shell.SU.run("ls");
        return result.isSuccessful();
    }

}
