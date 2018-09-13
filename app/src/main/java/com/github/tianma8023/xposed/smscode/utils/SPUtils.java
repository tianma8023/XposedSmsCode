package com.github.tianma8023.xposed.smscode.utils;

import com.crossbowffs.remotepreferences.RemotePreferences;
import com.github.tianma8023.xposed.smscode.constant.PrefConst;

import static com.github.tianma8023.xposed.smscode.constant.PrefConst.CLEAR_CLIPBOARD_DEFAULT;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.ENABLE_DEFAULT;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.FOCUS_MODE_AUTO;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_CLEAR_CLIPBOARD;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_ENABLE;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_FOCUS_MODE;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_SHOW_TOAST;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_SMSCODE_KEYWORDS;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_VERBOSE_LOG_MODE;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.SHOW_TOAST_DEFAULT;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.SMSCODE_KEYWORDS_DEFAULT;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.VERBOSE_LOG_MODE_DEFAULT;
import static com.github.tianma8023.xposed.smscode.utils.RemotePreferencesUtils.getBoolean;
import static com.github.tianma8023.xposed.smscode.utils.RemotePreferencesUtils.getInt;
import static com.github.tianma8023.xposed.smscode.utils.RemotePreferencesUtils.getString;
import static com.github.tianma8023.xposed.smscode.utils.RemotePreferencesUtils.putInt;
import static com.github.tianma8023.xposed.smscode.utils.RemotePreferencesUtils.putString;

public class SPUtils {

    private static final String KEY_AUTO_INPUT_MODE_ACCESSIBILITY = "pref_auto_input_mode_accessibility";
    private static final boolean AUTO_INPUT_MODE_ACCESSIBILITY_DEFAULT = false;
    private static final String KEY_AUTO_INPUT_MODE_ROOT = "pref_auto_input_mode_root";
    private static final boolean AUTO_INPUT_MODE_ROOT_DEFAULT = false;

    // 本地的版本号
    private static final String LOCAL_VERSION_CODE = "local_version_code";
    private static final int LOCAL_VERSION_CODE_DEFAULT = 16;

    private SPUtils() {

    }

    /**
     * 总开关是否打开
     */
    public static boolean isEnabled(RemotePreferences preferences) {
        return getBoolean(preferences, KEY_ENABLE, ENABLE_DEFAULT);
    }

    /**
     * 日志模式是否是verbose log模式
     */
    public static boolean isVerboseLogMode(RemotePreferences preferences) {
        return getBoolean(preferences,
                KEY_VERBOSE_LOG_MODE, VERBOSE_LOG_MODE_DEFAULT);
    }

    /**
     * 自动输入模式是否是root模式
     */
    public static boolean isAutoInputRootMode(RemotePreferences preferences) {
        return getBoolean(preferences,
                KEY_AUTO_INPUT_MODE_ROOT, AUTO_INPUT_MODE_ROOT_DEFAULT);
    }

    /**
     * 自动输入模式是否是无障碍模式(仅用于兼容之前版本)
     */
    public static boolean isAutoInputAccessibilityMode(RemotePreferences preferences) {
        return getBoolean(preferences,
                KEY_AUTO_INPUT_MODE_ACCESSIBILITY, AUTO_INPUT_MODE_ACCESSIBILITY_DEFAULT);
    }


    /**
     * 设置自动输入模式
     */
    public static void setAutoInputMode(RemotePreferences preferences, String autoInputMode) {
        putString(preferences, PrefConst.KEY_AUTO_INPUT_MODE, autoInputMode);
    }

    /**
     * 获取自动输入模式
     */
    public static String getAutoInputMode(RemotePreferences preferences) {
        return getString(preferences,
                PrefConst.KEY_AUTO_INPUT_MODE, PrefConst.AUTO_INPUT_MODE_DEFAULT);
    }

    /**
     * 是否应该在自动输入成功之后清理剪切板
     */
    public static boolean shouldClearClipboard(RemotePreferences preferences) {
        return getBoolean(preferences,
                KEY_CLEAR_CLIPBOARD, CLEAR_CLIPBOARD_DEFAULT);
    }

    /**
     * 是否应该在复制验证码到系统剪切板之后显示Toast
     */
    public static boolean shouldShowToast(RemotePreferences preferences) {
        return getBoolean(preferences,
                KEY_SHOW_TOAST, SHOW_TOAST_DEFAULT);
    }

    /**
     * 获取对焦模式
     */
    public static String getFocusMode(RemotePreferences preferences) {
        return getString(preferences,
                KEY_FOCUS_MODE, FOCUS_MODE_AUTO);
    }

    /**
     * 获取短信验证码关键字
     */
    public static String getSMSCodeKeywords(RemotePreferences preferences) {
        return getString(preferences,
                KEY_SMSCODE_KEYWORDS, SMSCODE_KEYWORDS_DEFAULT);
    }

    /**
     * 获取本地记录的版本号
     */
    public static int getLocalVersionCode(RemotePreferences preferences) {
        // 如果不存在,则默认返回16,即v1.4.5版本
        return getInt(preferences, LOCAL_VERSION_CODE, LOCAL_VERSION_CODE_DEFAULT);
    }

    /**
     * 设置当前版本号
     */
    public static void setLocalVersionCode(RemotePreferences preferences, int versionCode) {
        putInt(preferences, LOCAL_VERSION_CODE, versionCode);
    }
}
