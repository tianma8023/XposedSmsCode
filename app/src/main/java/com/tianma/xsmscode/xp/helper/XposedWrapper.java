package com.tianma.xsmscode.xp.helper;


import com.tianma.xsmscode.common.utils.XLog;

import java.lang.reflect.Member;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Xposed Wrapper Utils
 */
public class XposedWrapper {

    private XposedWrapper() {
    }

    public static Class<?> findClass(String className, ClassLoader classLoader) {
        try {
            return XposedHelpers.findClass(className, classLoader);
        } catch (Throwable t) {
            XLog.e("Class not found: %s", className);
            return null;
        }
    }

    public static XC_MethodHook.Unhook findAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
        try {
            return XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
        } catch (Throwable t) {
            XLog.e("Error in hook %s#%s", className, methodName, t);
            return null;
        }
    }

    public static XC_MethodHook.Unhook findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        try {
            return XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
        } catch (Throwable t) {
            XLog.e("Error in hook %s#%s", clazz.getName(), methodName, t);
            return null;
        }
    }

    public static XC_MethodHook.Unhook hookMethod(Member hookMethod, XC_MethodHook callback) {
        try {
            return XposedBridge.hookMethod(hookMethod, callback);
        } catch (Throwable t) {
            XLog.e("Error in hookMethod: %s", hookMethod.getName(), t);
            return null;
        }
    }

    public static Set<XC_MethodHook.Unhook> hookAllConstructors(Class<?> hookClass, XC_MethodHook callback) {
        try {
            return XposedBridge.hookAllConstructors(hookClass, callback);
        } catch (Throwable t) {
            XLog.e("Error in hookAllConstructors: %s", hookClass.getName(), t);
            return null;
        }
    }

    public static Set<XC_MethodHook.Unhook> hookAllMethods(Class<?> hookClass, String methodName, XC_MethodHook callback) {
        try {
            return XposedBridge.hookAllMethods(hookClass, methodName, callback);
        } catch (Throwable t) {
            XLog.e("Error in hookAllMethods: %s", hookClass.getName(), t);
            return null;
        }
    }

}
