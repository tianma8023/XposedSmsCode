package com.github.tianma8023.xposed.smscode.xp;

import com.github.tianma8023.xposed.smscode.xp.hook.AbsHook;
import com.github.tianma8023.xposed.smscode.xp.hook.DonateWechatHook;
import com.github.tianma8023.xposed.smscode.xp.hook.ModuleUtilsHook;
import com.github.tianma8023.xposed.smscode.xp.hook.PermissionGranterHook;
import com.github.tianma8023.xposed.smscode.xp.hook.SmsHandlerHook;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private List<AbsHook> mHookList;

    {
        mHookList = new ArrayList<>();
        mHookList.add(new SmsHandlerHook()); // InBoundsSmsHandler Hook
        mHookList.add(new ModuleUtilsHook()); // ModuleUtils Hook
        mHookList.add(new DonateWechatHook()); // Wechat donate Hook
        mHookList.add(new PermissionGranterHook()); // PackageManagerService Hook
//        mHookList.add(new NotificationManagerHook()); // NotificationManager Hook
//        mHookList.add(new CodeServiceHook()); // CodeService Hook
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable{
        for(AbsHook hook : mHookList) {
            if (hook.hookInitZygote()) {
                hook.initZygote(startupParam);
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        for (AbsHook hook : mHookList) {
            if (hook.hookOnLoadPackage()) {
                hook.onLoadPackage(lpparam);
            }
        }
    }
}
