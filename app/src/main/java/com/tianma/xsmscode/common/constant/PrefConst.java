package com.tianma.xsmscode.common.constant;

import com.github.tianma8023.xposed.smscode.BuildConfig;

/**
 * Preference相关的常量
 */
public interface PrefConst {

    String PREF_NAME = BuildConfig.APPLICATION_ID + "_preferences";

    // General
    String KEY_ENABLE = "pref_enable";
    String KEY_HIDE_LAUNCHER_ICON = "pref_hide_launcher_icon";
    String KEY_CHOOSE_THEME = "pref_choose_theme";

    // SMS Code
    String KEY_SHOW_TOAST = "pref_show_toast";
    String KEY_COPY_TO_CLIPBOARD = "pref_copy_to_clipboard";
    String KEY_ENABLE_AUTO_INPUT_CODE = "pref_enable_auto_input_code";
    String KEY_APP_BLOCK_ENTRY = "pref_app_block_entry";
    String KEY_BLOCK_SMS = "pref_block_sms";
    String KEY_DEDUPLICATE_SMS = "pref_deduplicate_sms";


    // Code Notification
    String KEY_SHOW_CODE_NOTIFICATION = "pref_show_code_notification";
    String KEY_AUTO_CANCEL_CODE_NOTIFICATION = "pref_auto_cancel_code_notification";
    String KEY_NOTIFICATION_RETENTION_TIME = "pref_notification_retention_time";
    String NOTIFICATION_RETENTION_TIME_DEFAULT = "5";


    // Code Record
    String KEY_ENABLE_CODE_RECORDS = "pref_enable_code_records";
    int MAX_SMS_RECORDS_COUNT_DEFAULT = 10;
    String KEY_ENTRY_CODE_RECORDS = "pref_entry_code_records";

    // Code Rules
    String KEY_SMSCODE_KEYWORDS = "pref_smscode_keywords";
    String SMSCODE_KEYWORDS_DEFAULT = SmsCodeConst.VERIFICATION_KEYWORDS_REGEX;
    String KEY_SMSCODE_TEST = "pref_smscode_test";
    String KEY_CODE_RULES = "pref_code_rules";

    // Experimental
    String KEY_MARK_AS_READ = "pref_mark_as_read";
    String KEY_DELETE_SMS = "pref_delete_sms";
    String KEY_KILL_ME = "pref_kill_me";

    // Others
    String KEY_VERBOSE_LOG_MODE = "pref_verbose_log_mode";

    // About
    String KEY_ABOUT = "pref_about";
    String KEY_VERSION = "pref_version";
    String KEY_JOIN_QQ_GROUP = "pref_join_qq_group";
    String KEY_SOURCE_CODE = "pref_source_code";
    String KEY_DONATE_BY_ALIPAY = "pref_donate_by_alipay";
    String KEY_PRIVACY_POLICY = "pref_privacy_policy";
    String KEY_PRIVACY_POLICY_ACCEPTED = "pref_privacy_policy_accepted";
}
