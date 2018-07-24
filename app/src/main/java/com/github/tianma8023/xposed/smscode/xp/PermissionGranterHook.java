package com.github.tianma8023.xposed.smscode.xp;

import android.os.Build;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.github.tianma8023.xposed.smscode.constant.IPermissionConstant;
import com.github.tianma8023.xposed.smscode.utils.XLog;

import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hook com.android.server.pm.PackageManagerService to grant permissions.
 */
public class PermissionGranterHook {

    private static final String SMSCODE_PACKAGE = BuildConfig.APPLICATION_ID;

    private static final String CLASS_PACKAGE_MANAGER_SERVICE = "com.android.server.pm.PackageManagerService";
    private static final String CLASS_PACKAGE_PARSER_PACKAGE = "android.content.pm.PackageParser.Package";

    private static final List<String> PERMISSIONS_TO_GRANT = IPermissionConstant.PERMISSIONS_TO_GRANT;

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if ("android".equals(lpparam.packageName) && "android".equals(lpparam.processName)) {
            try {
                hookPackageManagerService(lpparam);
            } catch (Exception e) {
                XLog.e("Failed to hook PackageManagerService", e);
            }
        }
    }

    private static void hookPackageManagerService(XC_LoadPackage.LoadPackageParam lpparam) {
        hookGrantPermissionsLPw(lpparam);
    }

    private static void hookGrantPermissionsLPw(XC_LoadPackage.LoadPackageParam lpparam) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            hookGrantPermissionsLPwSinceLollipop(lpparam);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            hookGrantPermissionsLPwSinceKitkat(lpparam);
        }
    }

    private static void hookGrantPermissionsLPwSinceKitkat(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.d("Hooking grantPermissionsLPw() for Android 19+");
        XposedHelpers.findAndHookMethod(CLASS_PACKAGE_MANAGER_SERVICE, lpparam.classLoader, "grantPermissionsLPw",
                /* PackageParser.Package pkg */ CLASS_PACKAGE_PARSER_PACKAGE,
                /* boolean replace           */ boolean.class,
                new GrantPermissionsLPwHook());
    }

    private static void hookGrantPermissionsLPwSinceLollipop(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.d("Hooking grantPermissionsLPw() for Android 21+");
        XposedHelpers.findAndHookMethod(CLASS_PACKAGE_MANAGER_SERVICE, lpparam.classLoader, "grantPermissionsLPw",
                /* PackageParser.Package pkg */ CLASS_PACKAGE_PARSER_PACKAGE,
                /* boolean replace           */ boolean.class,
                /* String packageOfInterest  */ String.class,
                new GrantPermissionsLPwHook());
    }

    private static class GrantPermissionsLPwHook extends XC_MethodHook {

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            try {
                afterGrantPermissionsLPwHandler(param);
            } catch (Exception e) {
                XLog.e("Hook grantPermissionsLPw() failed", e);
            }
        }

    }

    @SuppressWarnings("unchecked")
    private static void afterGrantPermissionsLPwHandler(XC_MethodHook.MethodHookParam param) {
        // android.content.pm.PackageParser.Package 对象
        Object pkg = param.args[0];

        final String packageName = (String) XposedHelpers.getObjectField(pkg, "packageName");

        // PackageParser$Package.mExtras 实际上是 com.android.server.pm.PackageSetting mExtras 对象
        final Object extras = XposedHelpers.getObjectField(pkg, "mExtras");
        // com.android.server.pm.PermissionsState 对象
        final Object permissionsState = XposedHelpers.callMethod(extras, "getPermissionsState");

        // Manifest.xml 中声明的permission列表
        final List<String> requestedPermissions = (List<String>)
                XposedHelpers.getObjectField(pkg, "requestedPermissions");

        // com.android.server.pm.Settings mSettings 对象
        final Object settings = XposedHelpers.getObjectField(param.thisObject, "mSettings");
        // ArrayMap<String, com.android.server.pm.BasePermission> mPermissions 对象
        final Object permissions = XposedHelpers.getObjectField(settings, "mPermissions");

        if (SMSCODE_PACKAGE.equals(packageName)) {
            for(String permissionToGrant : PERMISSIONS_TO_GRANT) {
                if (!requestedPermissions.contains(permissionToGrant)) {
                    boolean granted =
                            (boolean) XposedHelpers.callMethod(permissionsState, "hasInstallPermission", permissionToGrant);
                    if (!granted) {
                        // Add permission:

                        // com.android.server.pm.BasePermission pReadSMS
                        final Object pReadSMS = XposedHelpers.callMethod(permissions, "get", permissionToGrant);
                        int result = (int) XposedHelpers.callMethod(permissionsState, "grantInstallPermission", pReadSMS);
                        XLog.i("Add permission " + pReadSMS + "; result = " + result);
                    } else {
                        XLog.i("Already have " + permissionToGrant + " permission");
                    }
                }
            }
        }
    }

}
