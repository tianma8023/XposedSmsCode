package com.github.tianma8023.xposed.smscode.xp;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // InBoundsSmsHandler Hook
        new SmsHandlerHook().handleLoadPackage(lpparam);
        // ModuleUtils Hook
        new ModuleUtilsHook().handleLoadPackage(lpparam);
        // Wechat donate Hook
        new DonateWechatHook().handleLoadPackage(lpparam);
        // PackageManagerService Hook
        new PermissionGranterHook().handleLoadPackage(lpparam);
    }
}
