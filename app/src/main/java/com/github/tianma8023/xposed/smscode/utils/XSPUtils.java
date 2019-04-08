package com.github.tianma8023.xposed.smscode.utils;

import com.github.tianma8023.xposed.smscode.constant.PrefConst;

import de.robv.android.xposed.XSharedPreferences;

public class XSPUtils {

    private XSPUtils() {

    }

    /**
     * 总开关是否打开
     */
    public static boolean isEnabled(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_ENABLE, PrefConst.ENABLE_DEFAULT);
    }

    /**
     * 日志模式是否是verbose log模式
     */
    public static boolean isVerboseLogMode(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_VERBOSE_LOG_MODE,
                PrefConst.VERBOSE_LOG_MODE_DEFAULT);
    }

    /**
     * 自动输入总开关是否打开
     */
    public static boolean autoInputCodeEnabled(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_ENABLE_AUTO_INPUT_CODE,
                PrefConst.ENABLE_AUTO_INPUT_CODE_DEFAULT);
    }

    /**
     * 是否应该在复制验证码到系统剪切板之后显示Toast
     */
    public static boolean shouldShowToast(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_SHOW_TOAST,
                PrefConst.SHOW_TOAST_DEFAULT);
    }

    /**
     * 获取短信验证码关键字
     */
    public static String getSMSCodeKeywords(XSharedPreferences preferences) {
        return preferences.getString(PrefConst.KEY_SMSCODE_KEYWORDS,
                PrefConst.SMSCODE_KEYWORDS_DEFAULT);
    }

    /**
     * 标记为已读是否打开
     */
    public static boolean markAsReadEnabled(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_MARK_AS_READ,
                PrefConst.MARK_AS_READ_DEFAULT);
    }

    /**
     * 是否删除验证码短信
     */
    public static boolean deleteSmsEnabled(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_DELETE_SMS,
                PrefConst.DELETE_SMS_DEFAULT);
    }

    /**
     * 是否复制到剪切板
     */
    public static boolean copyToClipboardEnabled(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_COPY_TO_CLIPBOARD,
                PrefConst.COPY_TO_CLIPBOARD_DEFAULT);
    }

    /**
     * 是否记录短信验证码
     */
    public static boolean recordSmsCodeEnabled(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_ENABLE_CODE_RECORDS,
                PrefConst.ENABLE_CODE_RECORDS_DEFAULT);
    }

    /**
     * 是否拦截短信通知
     */
    public static boolean blockSmsEnabled(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_BLOCK_SMS,
                PrefConst.BLOCK_SMS_DEFAULT);
    }

    /**
     * 验证码提取成功后是否杀掉模块进程
     */
    public static boolean killMeEnabled(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_KILL_ME,
                PrefConst.KILL_ME_DEFAULT);
    }
}
