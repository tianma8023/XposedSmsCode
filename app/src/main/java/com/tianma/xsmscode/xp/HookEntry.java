package com.tianma.xsmscode.xp;

import android.util.Log;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.common.utils.XSPUtils;
import com.tianma.xsmscode.xp.hook.BaseHook;
import com.tianma.xsmscode.xp.hook.me.ModuleUtilsHook;
import com.tianma.xsmscode.xp.hook.permission.PermissionGranterHook;
import com.tianma.xsmscode.xp.hook.code.SmsHandlerHook;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private List<BaseHook> mHookList;

    {
        mHookList = new ArrayList<>();
        mHookList.add(new SmsHandlerHook()); // InBoundsSmsHandler Hook
        mHookList.add(new ModuleUtilsHook()); // ModuleUtils Hook
        mHookList.add(new PermissionGranterHook()); // PackageManagerService Hook
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        for (BaseHook hook : mHookList) {
            if (hook.hookInitZygote()) {
                hook.initZygote(startupParam);
            }
        }

        try {
            XSharedPreferences xsp = new XSharedPreferences(BuildConfig.APPLICATION_ID);
            if (XSPUtils.isVerboseLogMode(xsp)) {
                XLog.setLogLevel(Log.VERBOSE);
            } else {
                XLog.setLogLevel(BuildConfig.LOG_LEVEL);
            }
        } catch (Throwable t) {
            XLog.e("", t);
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        for (BaseHook hook : mHookList) {
            if (hook.hookOnLoadPackage()) {
                hook.onLoadPackage(lpparam);
            }
        }
    }
}
