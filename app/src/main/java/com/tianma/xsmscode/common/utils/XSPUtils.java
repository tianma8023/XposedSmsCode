package com.tianma.xsmscode.common.utils;

import com.tianma.xsmscode.common.constant.PrefConst;

import de.robv.android.xposed.XSharedPreferences;

public class XSPUtils {

    private XSPUtils() {

    }

    /**
     * 总开关是否打开
     */
    public static boolean isEnabled(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_ENABLE, true);
    }

    /**
     * 日志模式是否是verbose log模式
     */
    public static boolean isVerboseLogMode(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_VERBOSE_LOG_MODE, false);
    }

    /**
     * 自动输入总开关是否打开
     */
    public static boolean autoInputCodeEnabled(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_ENABLE_AUTO_INPUT_CODE, true);
    }

    /**
     * 是否应该在复制验证码到系统剪切板之后显示Toast
     */
    public static boolean shouldShowToast(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_SHOW_TOAST, true);
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
        return preferences.getBoolean(PrefConst.KEY_MARK_AS_READ, false);
    }

    /**
     * 是否删除验证码短信
     */
    public static boolean deleteSmsEnabled(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_DELETE_SMS, false);
    }

    /**
     * 是否复制到剪切板
     */
    public static boolean copyToClipboardEnabled(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_COPY_TO_CLIPBOARD, false);
    }

    /**
     * 是否记录短信验证码
     */
    public static boolean recordSmsCodeEnabled(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_ENABLE_CODE_RECORDS, true);
    }

    /**
     * 是否拦截短信通知
     */
    public static boolean blockSmsEnabled(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_BLOCK_SMS, false);
    }

    /**
     * 验证码提取成功后是否杀掉模块进程
     */
    public static boolean killMeEnabled(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_KILL_ME, false);
    }

    /**
     * 是否显示验证码通知
     */
    public static boolean showCodeNotification(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_SHOW_CODE_NOTIFICATION, true);
    }

    /**
     * 是否自动清除验证码通知
     */
    public static boolean autoCancelCodeNotification(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_AUTO_CANCEL_CODE_NOTIFICATION, false);
    }

    /**
     * 获取验证码通知保留时间
     */
    public static int getNotificationRetentionTime(XSharedPreferences preferences) {
        String value = preferences.getString(PrefConst.KEY_NOTIFICATION_RETENTION_TIME,
                PrefConst.NOTIFICATION_RETENTION_TIME_DEFAULT);
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 是否过滤掉重复短信
     */
    public static boolean deduplicateSms(XSharedPreferences preferences) {
        return preferences.getBoolean(PrefConst.KEY_DEDUPLICATE_SMS, false);
    }
}