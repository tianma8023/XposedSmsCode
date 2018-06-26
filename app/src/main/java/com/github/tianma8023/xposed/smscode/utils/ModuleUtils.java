package com.github.tianma8023.xposed.smscode.utils;

/**
 * 当前Xposed模块相关工具类
 */
public class ModuleUtils {

    private ModuleUtils() {}

    /**
     * 返回模块版本 <br/>
     * 注意：该方法被本模块Hook住，返回的值是 BuildConfig.MODULE_VERSION，如果没被Hook则返回-1
     */
    private static int getModuleVersion() {
        return -1;
    }

    /**
     * 当前模块是否在XposedInstaller中被启用
     */
    public static boolean isModuleEnabled() {
        return getModuleVersion() > 0;
    }

}
