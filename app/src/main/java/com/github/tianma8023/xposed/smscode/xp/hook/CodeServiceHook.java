package com.github.tianma8023.xposed.smscode.xp.hook;

import android.content.Context;
import android.os.Build;

import com.github.tianma8023.xposed.smscode.utils.XLog;
import com.github.tianma8023.xposed.smscode.xp.service.CodeService;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class CodeServiceHook extends BaseHook {

    private static final String AMS_CLS = "com.android.server.am.ActivityManagerService";

    @Override
    public boolean hookInitZygote() {
        return true;
    }

    @Override
    public boolean hookOnLoadPackage() {
        return true;
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                hookSinceLollipop();
            } else {
                hookBeforeLollipop();
            }
        } catch (Throwable e) {
            XLog.e("error occurs when hook AMS", e);
        }
    }



    private void hookSinceLollipop() throws Throwable {
        // android 5.0+
        XLog.i("hooking ActivityThread...");
        Class<?> activityThread = Class.forName("android.app.ActivityThread");
        XposedBridge.hookAllMethods(activityThread, "systemMain", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XLog.i("hooking ActivityThread#systemMain()...");
                final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Class<?> ams = XposedHelpers.findClass(AMS_CLS, classLoader);

                XposedHelpers.findAndHookConstructor(ams,
                        Context.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                try {
                                    XLog.i("Hooking ActivityManagerService$access()...");
                                    CodeService.register((Context) param.args[0], classLoader);
                                } catch (Throwable e) {
                                    XLog.e("error occurs when register system service", e);
                                }
                            }
                        });
            }
        });
    }

    private void hookBeforeLollipop() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?> ams = XposedHelpers.findClass(AMS_CLS, classLoader);
        XposedBridge.hookAllMethods(ams,
                "main",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        CodeService.register((Context) param.getResult(), classLoader);
                    }
                });
    }
}
