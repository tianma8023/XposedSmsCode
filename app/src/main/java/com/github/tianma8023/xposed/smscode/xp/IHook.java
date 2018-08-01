package com.github.tianma8023.xposed.smscode.xp;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public interface IHook {

    void onLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable;

}
