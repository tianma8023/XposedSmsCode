package com.github.tianma8023.xposed.smscode.utils;

import com.crossbowffs.remotepreferences.RemotePreferences;

import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_AUTO_INPUT_MODE_ROOT;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_AUTO_INPUT_MODE_ROOT_DEFAULT;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_CLEAR_CLIPBOARD;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_CLEAR_CLIPBOARD_DEFAULT;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_ENABLE;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_ENABLE_DEFAULT;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_FOCUS_MODE;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_FOCUS_MODE_AUTO;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_SHOW_TOAST;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_SHOW_TOAST_DEFAULT;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_SMSCODE_KEYWORDS;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_SMSCODE_KEYWORDS_DEFAULT;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_VERBOSE_LOG_MODE;
import static com.github.tianma8023.xposed.smscode.constant.PrefConst.KEY_VERBOSE_LOG_MODE_DEFAULT;
import static com.github.tianma8023.xposed.smscode.utils.RemotePreferencesUtils.getBooleanPref;
import static com.github.tianma8023.xposed.smscode.utils.RemotePreferencesUtils.getStringPref;

public class SPUtils {

    private SPUtils() {

    }

    /**
     * 总开关是否打开
     */
    public static boolean isEnabled(RemotePreferences preferences) {
        return getBooleanPref(preferences, KEY_ENABLE, KEY_ENABLE_DEFAULT);
    }

    /**
     * 日志模式是否是verbose log模式
     */
    public static boolean isVerboseLogMode(RemotePreferences preferences) {
        return getBooleanPref(preferences,
                KEY_VERBOSE_LOG_MODE, KEY_VERBOSE_LOG_MODE_DEFAULT);
    }

    /**
     * 自动输入模式是否是root模式
     */
    public static boolean isAutoInputRootMode(RemotePreferences preferences) {
        return getBooleanPref(preferences,
                KEY_AUTO_INPUT_MODE_ROOT, KEY_AUTO_INPUT_MODE_ROOT_DEFAULT);
    }

    /**
     * 是否应该在自动输入成功之后清理剪切板
     */
    public static boolean shouldClearClipboard(RemotePreferences preferences) {
        return getBooleanPref(preferences,
                KEY_CLEAR_CLIPBOARD, KEY_CLEAR_CLIPBOARD_DEFAULT);
    }

    /**
     * 是否应该在复制验证码到系统剪切板之后显示Toast
     */
    public static boolean shouldShowToast(RemotePreferences preferences) {
        return getBooleanPref(preferences,
                KEY_SHOW_TOAST, KEY_SHOW_TOAST_DEFAULT);
    }

    /**
     * 获取对焦模式
     */
    public static String getFocusMode(RemotePreferences preferences) {
        return getStringPref(preferences,
                KEY_FOCUS_MODE, KEY_FOCUS_MODE_AUTO);
    }

    /**
     * 获取短信验证码关键字
     */
    public static String getSMSCodeKeywords(RemotePreferences preferences) {
        return getStringPref(preferences,
                KEY_SMSCODE_KEYWORDS, KEY_SMSCODE_KEYWORDS_DEFAULT);
    }
}
