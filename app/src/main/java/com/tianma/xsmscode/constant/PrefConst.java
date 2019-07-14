package com.tianma.xsmscode.constant;

import com.github.tianma8023.xposed.smscode.BuildConfig;

/**
 * Preference相关的常量
 */
public interface PrefConst {

    String REMOTE_PREF_NAME = BuildConfig.APPLICATION_ID + "_preferences";

    String KEY_ENABLE = "pref_enable";
    boolean ENABLE_DEFAULT = true;

    String KEY_SHOW_TOAST = "pref_show_toast";
    boolean SHOW_TOAST_DEFAULT = true;
    String KEY_COPY_TO_CLIPBOARD = "pref_copy_to_clipboard";
    boolean COPY_TO_CLIPBOARD_DEFAULT = true;

    String KEY_CHOOSE_THEME = "pref_choose_theme";
    String KEY_CURRENT_THEME_INDEX = "pref_current_theme_index";
    int CURRENT_THEME_INDEX_DEFAULT = 0;

    String KEY_EXPERIMENTAL = "pref_experimental";

    String KEY_MARK_AS_READ = "pref_mark_as_read";
    boolean MARK_AS_READ_DEFAULT = false;
    String KEY_DELETE_SMS = "pref_delete_sms";
    boolean DELETE_SMS_DEFAULT = false;
    String KEY_BLOCK_SMS = "pref_block_sms";
    boolean BLOCK_SMS_DEFAULT = false;
    String KEY_KILL_ME = "pref_kill_me";
    boolean KILL_ME_DEFAULT = false;

    String KEY_SHOW_CODE_NOTIFICATION = "pref_show_code_notification";
    boolean SHOW_CODE_NOTIFICATION_DEFAULT = false;
    String KEY_AUTO_CANCEL_CODE_NOTIFICATION = "pref_auto_cancel_code_notification";
    boolean AUTO_CANCEL_CODE_NOTIFICATION_DEFAULT = false;
    String KEY_NOTIFICATION_RETENTION_TIME = "pref_notification_retention_time";
    String NOTIFICATION_RETENTION_TIME_DEFAULT = "5";

    String KEY_ENTRY_AUTO_INPUT_CODE = "pref_entry_auto_input_code";

    String KEY_HIDE_LAUNCHER_ICON = "pref_hide_launcher_icon";

    String KEY_SMSCODE_KEYWORDS = "pref_smscode_keywords";
    String SMSCODE_KEYWORDS_DEFAULT = SmsCodeConst.VERIFICATION_KEYWORDS_REGEX;
    String KEY_SMSCODE_TEST = "pref_smscode_test";
    String KEY_CODE_RULES = "pref_code_rules";

    String KEY_ABOUT = "pref_about";
    String KEY_VERSION = "pref_version";
    String KEY_JOIN_QQ_GROUP = "pref_join_qq_group";
    String KEY_SOURCE_CODE = "pref_source_code";
    String KEY_GET_ALIPAY_PACKET = "pref_get_alipay_packet";
    String KEY_DONATE_BY_ALIPAY = "pref_donate_by_alipay";

    String KEY_ENABLE_AUTO_INPUT_CODE = "pref_enable_auto_input_code";
    boolean ENABLE_AUTO_INPUT_CODE_DEFAULT = true;

    String KEY_VERBOSE_LOG_MODE = "pref_verbose_log_mode";
    boolean VERBOSE_LOG_MODE_DEFAULT = false;

    String KEY_ENABLE_CODE_RECORDS = "pref_enable_code_records";
    boolean ENABLE_CODE_RECORDS_DEFAULT = true;
    int MAX_SMS_RECORDS_COUNT_DEFAULT = 10;
    String KEY_ENTRY_CODE_RECORDS = "pref_entry_code_records";
}
