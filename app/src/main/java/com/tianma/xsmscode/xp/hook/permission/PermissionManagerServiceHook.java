package com.tianma.xsmscode.xp.hook.permission;

import android.os.Build;

import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.xp.helper.MethodHookWrapper;
import com.tianma.xsmscode.xp.hook.BaseSubHook;

import java.util.List;
import java.util.Set;

import androidx.annotation.RequiresApi;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import static com.tianma.xsmscode.common.constant.PermConst.PACKAGE_PERMISSIONS;

/**
 * Since Android P(API 28)<br/>
 * Hook com.android.server.pm.permission.PermissionManagerService
 */
public class PermissionManagerServiceHook extends BaseSubHook {

    // for Android 28+
    private static final String CLASS_PERMISSION_MANAGER_SERVICE = "com.android.server.pm.permission.PermissionManagerService";
    private static final String CLASS_PERMISSION_CALLBACK = "com.android.server.pm.permission.PermissionManagerInternal.PermissionCallback";
    private static final String CLASS_PACKAGE_PARSER_PACKAGE = "android.content.pm.PackageParser.Package";

    public PermissionManagerServiceHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @Override
    public void startHook() {
        try {
            hookGrantPermissions();
        } catch (Throwable e) {
            XLog.e("Failed to hook PermissionManagerService", e);
        }
    }

    private void hookGrantPermissions() {
        XLog.d("Hooking grantPermissions() for Android 28+");
        XposedHelpers.findAndHookMethod(CLASS_PERMISSION_MANAGER_SERVICE, mClassLoader, "grantPermissions",
                /* PackageParser.Package pkg   */ CLASS_PACKAGE_PARSER_PACKAGE,
                /* boolean replace             */ boolean.class,
                /* String packageOfInterest    */ String.class,
                /* PermissionCallback callback */ CLASS_PERMISSION_CALLBACK,
                new MethodHookWrapper() {
                    @Override
                    protected void after(MethodHookParam param) {
                        afterGrantPermissionsSinceP(param);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private void afterGrantPermissionsSinceP(XC_MethodHook.MethodHookParam param) {
        // android.content.pm.PackageParser.Package 对象
        Object pkg = param.args[0];

        final String _packageName = (String) XposedHelpers.getObjectField(pkg, "packageName");

        Set<String> packageSet = PACKAGE_PERMISSIONS.keySet();
        for (String packageName : packageSet) {
            if (packageName.equals(_packageName)) {
                XLog.d("PackageName: %s", packageName);
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
                        // if (!granted) {
                        //     XLog.d("Don't have " + permissionToGrant + " permission");
                        // } else {
                        //     XLog.d("Already have " + permissionToGrant + " permission");
                        //     // com.android.server.pm.permission.BasePermission bpToGrant
                        //     final Object bpToGrant = XposedHelpers.callMethod(permissions, "get", permissionToGrant);
                        //     int result = (int) XposedHelpers.callMethod(permissionsState, "revokeInstallPermission", bpToGrant);
                        //     XLog.d("Remove permission " + bpToGrant + "; result = " + result);
                        // }
                    }
                }
            }
        }
    }


}
