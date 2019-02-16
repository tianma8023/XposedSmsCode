package com.github.tianma8023.xposed.smscode.utils;

import android.content.Context;
import android.content.Intent;

/**
 * 当前Xposed模块相关工具类
 */
public class ModuleUtils {

    public enum Section {
        INSTALL("install", 0),
        MODULES("modules", 1);

        private final String mSection;
        private final int mFragment;

        Section(String section, int fragment) {
            mSection = section;
            mFragment = fragment;
        }
    }

    private static final String XPOSED_PACKAGE = "de.robv.android.xposed.installer";

    // Old Xposed installer
    private static final String XPOSED_OPEN_SECTION_ACTION = XPOSED_PACKAGE + ".OPEN_SECTION";
    private static final String XPOSED_EXTRA_SECTION = "section";

    // New Xposed installer
    private static final String XPOSED_ACTIVITY = XPOSED_PACKAGE + ".WelcomeActivity";
    private static final String XPOSED_EXTRA_FRAGMENT = "fragment";

    private ModuleUtils() {
    }

    /**
     * 返回模块版本 <br/>
     * 注意：该方法被本模块Hook住，返回的值是 BuildConfig.MODULE_VERSION，如果没被Hook则返回-1
     */
    public static int getModuleVersion() {
        XLog.d("getModuleVersion()");
        return -1;
    }

    /**
     * 当前模块是否在XposedInstaller中被启用
     */
    public static boolean isModuleEnabled() {
        return getModuleVersion() > 0;
    }

    private static boolean startOldXposedActivity(Context context, String section) {
        Intent intent = new Intent(XPOSED_OPEN_SECTION_ACTION);
        intent.putExtra(XPOSED_EXTRA_SECTION, section);
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean startNewXposedActivity(Context context, int fragment) {
        Intent intent = new Intent();
        intent.setClassName(XPOSED_PACKAGE, XPOSED_ACTIVITY);
        intent.putExtra(XPOSED_EXTRA_FRAGMENT, fragment);
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean startXposedActivity(Context context, Section section) {
        return startNewXposedActivity(context, section.mFragment)
                || startOldXposedActivity(context, section.mSection);
    }
}
