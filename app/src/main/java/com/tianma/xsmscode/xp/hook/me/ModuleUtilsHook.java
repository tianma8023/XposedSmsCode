package com.tianma.xsmscode.xp.hook.me;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.tianma.xsmscode.common.utils.ModuleUtils;
import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.xp.hook.BaseHook;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hook class ModuleUtils
 */
public class ModuleUtilsHook extends BaseHook {

    private static final String SMSCODE_PACKAGE = BuildConfig.APPLICATION_ID;
    private static final int MODULE_VERSION = BuildConfig.MODULE_VERSION;

    @Override
    public void onLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (SMSCODE_PACKAGE.equals(lpparam.packageName)) {
            try {
                XLog.i("Hooking current Xposed module status...");
                hookModuleUtils(lpparam);
            } catch (Throwable e) {
                XLog.e("Failed to hook current Xposed module status.");
            }
        }

    }

    private void hookModuleUtils(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        String className = ModuleUtils.class.getName();

        XposedHelpers.findAndHookMethod(className, lpparam.classLoader,
                "getModuleVersion",
                XC_MethodReplacement.returnConstant(MODULE_VERSION));
    }

}
