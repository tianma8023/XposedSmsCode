package com.github.tianma8023.xposed.smscode.constant;

import com.github.tianma8023.xposed.smscode.BuildConfig;

/**
 * Preference相关的常量
 */
public interface IPrefConstants {

    String REMOTE_PREF_NAME = BuildConfig.APPLICATION_ID + "_preferences";
    String REMOTE_PREF_AUTHORITY = BuildConfig.APPLICATION_ID + ".preferences";

    String KEY_ENABLE = "pref_enable";
    boolean KEY_ENABLE_DEFAULT = true;

    String KEY_SHOW_TOAST = "pref_show_toast";
    boolean KEY_SHOW_TOAST_DEFAULT = true;

    String KEY_MARK_AS_READ = "pref_mark_as_read";
    boolean KEY_MARK_AS_READ_DEFAULT = false;

    String KEY_HIDE_LAUNCHER_ICON = "pref_hide_launcher_icon";

    String KEY_AUTHOR = "pref_author";
    String KEY_DONATE_BY_ALIPAY = "pref_donate_by_alipay";
    String KEY_DONATE_BY_WECHAT = "pref_donate_by_wechat";

}
