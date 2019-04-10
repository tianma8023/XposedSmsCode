package com.github.tianma8023.xposed.smscode.xp.hook;

import android.os.Build;

import com.github.tianma8023.xposed.smscode.constant.PermConst;
import com.github.tianma8023.xposed.smscode.utils.XLog;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hook com.android.server.pm.PackageManagerService to grant permissions.
 */
public class PermissionGranterHook extends BaseHook {

    public static final String ANDROID_PACKAGE = "android";

    private static final String CLASS_PACKAGE_MANAGER_SERVICE = "com.android.server.pm.PackageManagerService";
    private static final String CLASS_PACKAGE_PARSER_PACKAGE = "android.content.pm.PackageParser.Package";

    // for Android 28+
    private static final String CLASS_PERMISSION_MANAGER_SERVICE = "com.android.server.pm.permission.PermissionManagerService";
    private static final String CLASS_PERMISSION_CALLBACK = "com.android.server.pm.permission.PermissionManagerInternal.PermissionCallback";

    private static final Map<String, List<String>> PACKAGE_PERMISSIONS = PermConst.PACKAGE_PERMISSIONS;

    @Override
    public void onLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (ANDROID_PACKAGE.equals(lpparam.packageName) && ANDROID_PACKAGE.equals(lpparam.processName)) {
            if (Build.VERSION.SDK_INT >= 28) { // Android 9.0+
                hookPermissionManagerService(lpparam);
            } else { // Android 5.0 ~ 8.1
                hookPackageManagerService(lpparam);
            }
        }
    }

    private static void hookPackageManagerService(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            hookGrantPermissionsLPw(lpparam);
        } catch (Throwable e) {
            XLog.e("Failed to hook PackageManagerService", e);
        }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    afterGrantPermissionsLPwSinceM(param);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    afterGrantPermissionsLPwSinceKitkat(param);
                }
            } catch (Throwable e) {
                XLog.e("Hook grantPermissionsLPw() failed", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void afterGrantPermissionsLPwSinceM(XC_MethodHook.MethodHookParam param) {
        // android.content.pm.PackageParser.Package 对象
        Object pkg = param.args[0];

        final String _packageName = (String) XposedHelpers.getObjectField(pkg, "packageName");

        Set<String> packageSet = PACKAGE_PERMISSIONS.keySet();
        for (String packageName : packageSet) {
            if (packageName.equals(_packageName)) {
                XLog.d("packageName = %s", packageName);
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

                List<String> permissionsToGrant = PACKAGE_PERMISSIONS.get(packageName);
                for (String permissionToGrant : permissionsToGrant) {
                    if (!requestedPermissions.contains(permissionToGrant)) {
                        boolean granted = (boolean) XposedHelpers.callMethod(
                                permissionsState, "hasInstallPermission", permissionToGrant);
                        if (!granted) {
                            // com.android.server.pm.BasePermission bpToGrant
                            final Object bpToGrant = XposedHelpers.callMethod(permissions, "get", permissionToGrant);
                            int result = (int) XposedHelpers.callMethod(permissionsState, "grantInstallPermission", bpToGrant);
                            XLog.d("Add " + bpToGrant + "; result = " + result);
                        } else {
                            XLog.d("Already have " + permissionToGrant + " permission");
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void afterGrantPermissionsLPwSinceKitkat(XC_MethodHook.MethodHookParam param) {
        // API 19

        // android.content.pm.PackageParser.Package object
        Object pkg = param.args[0];

        final String _packageName = (String) XposedHelpers.getObjectField(pkg, "packageName");

        Set<String> packageSet = PACKAGE_PERMISSIONS.keySet();
        for (String packageName : packageSet) {
            if (packageName.equals(_packageName)) {
                XLog.d("packageName = %s", packageName);
                // com.android.server.pm.PackageSetting mExtra object
                final Object extra = XposedHelpers.getObjectField(pkg, "mExtras");
                // PackageSetting extends PackageSettingBase
                // PackageSettingBase extends GrantedPermissions
                // Android 4.4~4.4.4 api 19 HashSet<String>
                // Android 5.0 api 21 HashSet<String>
                // Android 5.1 api 22 ArraySet<String>
                final Set<String> grantedPermissions = (Set<String>)
                        XposedHelpers.getObjectField(extra, "grantedPermissions");

                // com.android.server.pm.Settings mSettings object
                final Object settings = XposedHelpers.getObjectField(param.thisObject, "mSettings");
                // HashMap<String, com.android.server.pm.BasePermission> mPermissions obj
                final Object permissions = XposedHelpers.getObjectField(settings, "mPermissions");

                List<String> permissionsToGrant = PACKAGE_PERMISSIONS.get(packageName);
                for (String permissionToGrant : permissionsToGrant) {
                    if (!grantedPermissions.contains(permissionToGrant)) {
                        // com.android.server.pm.BasePermission
                        final Object bpToGrant = XposedHelpers.
                                callMethod(permissions, "get", permissionToGrant);
                        grantedPermissions.add(permissionToGrant);

                        // granted permission gids
                        int[] gpGids = (int[]) XposedHelpers.getObjectField(extra, "gids");
                        // base permission to grant gids
                        int[] bpGids = (int[]) XposedHelpers.getObjectField(bpToGrant, "gids");
                        XposedHelpers.callStaticMethod(
                                param.thisObject.getClass(), "appendInts", gpGids, bpGids);

                        XLog.d("Add " + bpToGrant);
                    } else {
                        XLog.d("Already have " + permissionToGrant + " permission");
                    }
                }
            }
        }
    }

    private static void hookPermissionManagerService(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            hookGrantPermissions(lpparam);
        } catch (Throwable e) {
            XLog.e("Failed to hook PermissionManagerService", e);
        }
    }

    private static void hookGrantPermissions(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.d("Hooking grantPermissions() for Android 28+");
        XposedHelpers.findAndHookMethod(CLASS_PERMISSION_MANAGER_SERVICE, lpparam.classLoader, "grantPermissions",
                /* PackageParser.Package pkg   */ CLASS_PACKAGE_PARSER_PACKAGE,
                /* boolean replace             */ boolean.class,
                /* String packageOfInterest    */ String.class,
                /* PermissionCallback callback */ CLASS_PERMISSION_CALLBACK,
                /* */new GrantPermissionsHook());
    }

    private static class GrantPermissionsHook extends XC_MethodHook {

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            try {
                afterGrantPermissionsSinceP(param);
            } catch (Throwable e) {
                XLog.e("Hook grantPermissions() failed", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void afterGrantPermissionsSinceP(XC_MethodHook.MethodHookParam param) {
        // android.content.pm.PackageParser.Package 对象
        Object pkg = param.args[0];

        final String _packageName = (String) XposedHelpers.getObjectField(pkg, "packageName");

        Set<String> packageSet = PACKAGE_PERMISSIONS.keySet();
        for (String packageName : packageSet) {
            if (packageName.equals(_packageName)) {
                XLog.d("packageName = %s", packageName);
                // PackageParser$Package.mExtras 实际上是 com.android.server.pm.PackageSetting mExtras 对象
                final Object extras = XposedHelpers.getObjectField(pkg, "mExtras");
                // com.android.server.pm.permission.PermissionsState 对象
                final Object permissionsState = XposedHelpers.callMethod(extras, "getPermissionsState");

                // Manifest.xml 中声明的permission列表
                final List<String> requestedPermissions = (List<String>)
                        XposedHelpers.getObjectField(pkg, "requestedPermissions");

                // com.android.server.pm.permission.PermissionSettings mSettings 对象
                final Object settings = XposedHelpers.getObjectField(param.thisObject, "mSettings");
                // ArrayMap<String, com.android.server.pm.permission.BasePermission> mPermissions 对象
                final Object permissions = XposedHelpers.getObjectField(settings, "mPermissions");

                List<String> permissionsToGrant = PACKAGE_PERMISSIONS.get(packageName);
                for (String permissionToGrant : permissionsToGrant) {
                    if (!requestedPermissions.contains(permissionToGrant)) {
                        boolean granted = (boolean) XposedHelpers.callMethod(
                                permissionsState, "hasInstallPermission", permissionToGrant);
                        // grant permissions
                        if (!granted) {
                            // com.android.server.pm.permission.BasePermission bpToGrant
                            final Object bpToGrant = XposedHelpers.callMethod(permissions, "get", permissionToGrant);
                            int result = (int) XposedHelpers.callMethod(permissionsState, "grantInstallPermission", bpToGrant);
                            XLog.d("Add " + bpToGrant + "; result = " + result);
                        } else {
                            XLog.d("Already have " + permissionToGrant + " permission");
                        }
                        // revoke permissions
//                        if (!granted) {
//                            XLog.d("Don't have " + permissionToGrant + " permission");
//                        } else {
//                            XLog.d("Already have " + permissionToGrant + " permission");
//                            // com.android.server.pm.permission.BasePermission bpToGrant
//                            final Object bpToGrant = XposedHelpers.callMethod(permissions, "get", permissionToGrant);
//                            int result = (int) XposedHelpers.callMethod(permissionsState, "revokeInstallPermission", bpToGrant);
//                            XLog.d("Remove permission " + bpToGrant + "; result = " + result);
//                        }
                    }
                }
            }
        }
    }

}
