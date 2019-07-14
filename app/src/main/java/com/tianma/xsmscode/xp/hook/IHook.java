package com.tianma.xsmscode.xp.hook;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public interface IHook {

    void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable;

    void onLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable;

}
