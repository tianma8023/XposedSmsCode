package com.tianma.xsmscode.xp.hook.permission;

import static com.tianma.xsmscode.common.constant.PermConst.PACKAGE_PERMISSIONS;

import android.os.Build;
import android.os.UserHandle;

import androidx.annotation.RequiresApi;

import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.xp.helper.MethodHookWrapper;
import com.tianma.xsmscode.xp.hook.BaseSubHook;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Since Android 13(API 33+)<br/>
 * Hook com.android.server.pm.permission.PermissionManagerService
 */
public class PermissionManagerServiceHook33 extends BaseSubHook {
    // IMPORTANT: There are two types of permissions: install and runtime.

    // Android 13, API 33
    private static final String CLASS_PERMISSION_MANAGER_SERVICE = "com.android.server.pm.permission.PermissionManagerServiceImpl";

    private static final String CLASS_ANDROID_PACKAGE = "com.android.server.pm.parsing.pkg.AndroidPackage";
    private static final String CLASS_PERMISSION_CALLBACK = CLASS_PERMISSION_MANAGER_SERVICE + ".PermissionCallback";


    public PermissionManagerServiceHook33(ClassLoader classLoader) {
        super(classLoader);
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Override
    public void startHook() {
        try {
            hookGrantPermissions();
        } catch (Throwable e) {
            XLog.e("Failed to hook PermissionManagerService", e);
        }
    }

    private void hookGrantPermissions() {
        XLog.d("Hooking grantPermissions() for Android 33+");
        Method method = findTargetMethod();
        if (method == null) {
            XLog.e("Cannot find the method to grant relevant permission");
            return;
        }
        XposedBridge.hookMethod(method, new MethodHookWrapper() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                afterRestorePermissionStateSinceAndroid13(param);
            }
        });
    }

    private Method findTargetMethod() {
        Class<?> pmsClass = XposedHelpers.findClass(CLASS_PERMISSION_MANAGER_SERVICE, mClassLoader);
        Class<?> androidPackageClass = XposedHelpers.findClass(CLASS_ANDROID_PACKAGE, mClassLoader);
        Class<?> callbackClass = XposedHelpers.findClassIfExists(CLASS_PERMISSION_CALLBACK, mClassLoader);

        // 精确匹配
        Method method = XposedHelpers.findMethodExactIfExists(pmsClass, "restorePermissionState",
                /* AndroidPackage pkg          */ androidPackageClass,
                /* boolean replace             */ boolean.class,
                /* String packageOfInterest    */ String.class,
                /* PermissionCallback callback */ callbackClass,
                /* int filterUserId            */ int.class);

        if (method == null) { // method restorePermissionState() not found
            // 参数类型精确匹配
            Method[] _methods = XposedHelpers.findMethodsByExactParameters(pmsClass, Void.TYPE,
                    /* AndroidPackage pkg          */ androidPackageClass,
                    /* boolean replace             */ boolean.class,
                    /* String packageOfInterest    */ String.class,
                    /* PermissionCallback callback */ callbackClass,
                    /* int filterUserId            */ int.class);
            if (_methods != null && _methods.length > 0) {
                method = _methods[0];
            }
        }
        return method;
    }

    @SuppressWarnings("unchecked")
    private void afterRestorePermissionStateSinceAndroid13(XC_MethodHook.MethodHookParam param) {
        // com.android.server.pm.parsing.pkg.AndroidPackage 对象
        Object pkg = param.args[0];

        final String _packageName = (String) XposedHelpers.callMethod(pkg, "getPackageName");

        Set<String> packageSet = PACKAGE_PERMISSIONS.keySet();
        for (String packageName : packageSet) {
            if (packageName.equals(_packageName)) {
                XLog.d("PackageName: %s", packageName);

                // PermissionManagerServiceImpl 对象
                Object pmsImpl = param.thisObject;

                // UserHandle.USER_ALL
                int filterUserId = (int) param.args[4];
                final int USER_ALL = XposedHelpers.getStaticIntField(UserHandle.class, "USER_ALL");
                final int[] userIds = filterUserId == USER_ALL
                        ? (int[]) XposedHelpers.callMethod(pmsImpl, "getAllUserIds")
                        : new int[]{filterUserId};

                List<String> permissionsToGrant = PACKAGE_PERMISSIONS.get(packageName);

                if (userIds != null) {
                    // PackageManagerInternal 对象 mPackageManagerInt
                    Object mPackageManagerInt = XposedHelpers.getObjectField(pmsImpl, "mPackageManagerInt");

                    // PackageStateInternal 对象 ps
                    // final PackageStateInternal ps = mPackageManagerInt.getPackageStateInternal(pkg.getPackageName());
                    final Object ps = XposedHelpers.callMethod(mPackageManagerInt, "getPackageStateInternal", packageName);

                    // Manifest.xml 中声明的permission列表
                    // List<String> requestPermissions = pkg.getRequestedPermissions();
                    final List<String> requestedPermissions = (List<String>)
                            XposedHelpers.callMethod(pkg, "getRequestedPermissions");

                    // com.android.server.pm.permission.DevicePermissionState 对象
                    final Object mState = XposedHelpers.getObjectField(pmsImpl, "mState");

                    // com.android.server.pm.permission.PermissionRegistry 对象
                    Object mRegistry = XposedHelpers.getObjectField(pmsImpl, "mRegistry");

                    for (final int userId : userIds) {
                        // com.android.server.pm.permission.UserPermissionState 对象
                        Object userState = XposedHelpers.callMethod(mState, "getOrCreateUserState", userId);
                        int appId = (int) XposedHelpers.callMethod(ps, "getAppId");
                        //  com.android.server.pm.permission.UidPermissionState 对象
                        Object uidState = XposedHelpers.callMethod(userState, "getOrCreateUidState", appId);

                        for (String permissionToGrant : permissionsToGrant) {
                            if (!requestedPermissions.contains(permissionToGrant)) {
                                boolean granted = (boolean) XposedHelpers.callMethod(uidState, "isPermissionGranted", permissionToGrant);
                                if (!granted) {
                                    // permission not grant before
                                    // final Permission bp = mRegistry.getPermission(permName);
                                    final Object bpToGrant = XposedHelpers.callMethod(mRegistry, "getPermission", permissionToGrant);
                                    // uidState.grantPermission(bp)
                                    boolean result = (boolean) XposedHelpers.callMethod(uidState, "grantPermission", bpToGrant);
                                    XLog.d("Add " + permissionToGrant + "; result = " + result);
                                } else {
                                    // permission has been granted already
                                    XLog.d("Already have " + permissionToGrant + " permission");
                                }
                            }
                        }
                    }
                }
            }
        }
    }


}
