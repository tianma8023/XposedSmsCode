package com.github.tianma8023.xposed.smscode.utils;

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

    private ShellUtils() {
    }

    /**
     * 获取已启用的 Accessibility Service 列表(需ROOT)
     *
     * @return null if error occurs in commands.
     */
    private static List<String> getEnabledAccessibilityServices() {
        CommandResult getResult = Shell.SU.run("settings get --user current secure enabled_accessibility_services");
        if (getResult.isSuccessful()) {
            String enabledAccessibilityServices = getResult.getStdout();
            List<String> serviceList = new ArrayList<>();
            if (!TextUtils.isEmpty(enabledAccessibilityServices)) {
                serviceList.addAll(Arrays.asList(enabledAccessibilityServices.split(":")));
            }
            XLog.i("enabled services = %s", serviceList.toString());
            return serviceList;
        }
        XLog.i(getResult.getStderr());
        return null;
    }

    /**
     * 设置无障碍服务
     *
     * @param enabledServices Enabled Accessibility Service List
     * @return 是否设置成功
     */
    private static boolean setEnabledAccessibilityService(List<String> enabledServices) {
        StringBuilder sb = new StringBuilder();
        String emptyOrColon = ""; // 空字符 or 冒号分隔符
        for (String service : enabledServices) {
            sb.append(emptyOrColon).append(service);
            emptyOrColon = ":";
        }
        String enabledServicesStr = sb.toString();
        XLog.i("enabled services = %s", enabledServicesStr);
        CommandResult putResult = Shell.SU.run("settings put --user current secure enabled_accessibility_services " + enabledServicesStr);
        return putResult.isSuccessful();
    }


    /**
     * 启用指定无障碍服务（需ROOT）
     *
     * @param accessibilityServiceName Accessibility Service Name
     * @return 是否成功启用
     */
    public static boolean enableAccessibilityService(String accessibilityServiceName) {
        List<String> enabledServices = getEnabledAccessibilityServices();
        if (enabledServices == null) {
            return false;
        }
        if (enabledServices.contains(accessibilityServiceName)) {
            return true;
        }
        enabledServices.add(accessibilityServiceName);
        return setEnabledAccessibilityService(enabledServices);
    }

    /**
     * 关闭指定无障碍服务（需ROOT）
     *
     * @param accessibilityServiceName Accessibility Service Name
     * @return 是否成功关闭
     */
    public static boolean disableAccessibilityService(String accessibilityServiceName) {
        List<String> enabledServices = getEnabledAccessibilityServices();
        if (enabledServices == null) {
            return false;
        }
        if (!enabledServices.contains(accessibilityServiceName)) {
            return true;
        }
        enabledServices.remove(accessibilityServiceName);
        return setEnabledAccessibilityService(enabledServices);
    }

    public static boolean checkRootPermission() {
        CommandResult result = Shell.SU.run("ls");
        return result.isSuccessful();
    }

}
