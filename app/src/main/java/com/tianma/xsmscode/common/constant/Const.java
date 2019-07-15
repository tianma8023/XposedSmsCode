package com.tianma.xsmscode.common.constant;

import com.github.tianma8023.xposed.smscode.BuildConfig;

/**
 * Constant about 3rd app
 */
public interface Const {

    /* Alipay begin */
    String ALIPAY_PACKAGE_NAME = "com.eg.android.AlipayGphone";
    String ALIPAY_QRCODE_URI_PREFIX = "alipayqr://platformapi/startapp?saId=10000007&qrcode=";
    String ALIPAY_QRCODE_URL = "HTTPS://QR.ALIPAY.COM/FKX074142EKXD0OIMV8B60";
    String ALIPAY_RED_PACKET_CODE = "638310579";
    /* Alipay end */

    /* QQ begin */
    String QQ_GROUP_KEY = "jWGrWgSGLGQ0NyyRsKqRlrApRCzecuNA";
    /* QQ end */

    /* Wechat begin */
    String WECHAT_PACKAGE_NAME = "com.tencent.mm";
    String WECHAT_LAUNCHER_UI = WECHAT_PACKAGE_NAME + ".ui.LauncherUI";
    String WECHAT_QR_REWARD_SELECT_MONEY_UI = WECHAT_PACKAGE_NAME +
            ".plugin.collect.reward.ui.QrRewardSelectMoneyUI";
    String WECHAT_KEY_EXTRA_DONATE = "TianmaDonate";
    String WECHAT_QRCODE_URL = "m01pPa@:hEyGJ5P*a1@$xPI";
    /* Wechat end */

    /* Xposed SmsCode begin */
    String HOME_ACTIVITY_ALIAS = BuildConfig.APPLICATION_ID + ".HomeActivityAlias";

    String PROJECT_SOURCE_CODE_URL = "https://github.com/tianma8023/XposedSmsCode";
    String PROJECT_DOC_BASE_URL = "https://tianma8023.github.io/SmsCodeExtractor";
    String DOC_SMS_CODE_RULE_HELP ="sms_code_rule_help";
    /* Xposed SmsCode end */

    /* Taichi begin */
    String TAICHI_PACKAGE_NAME = "me.weishu.exp";
    String TAICHI_MAIN_PAGE = "me.weishu.exp.ui.MainActivity";
    /* Taichi end */

    /* Xposed Installer begin */
    String XPOSED_PACKAGE = "de.robv.android.xposed.installer";
    // Old Xposed installer
    String XPOSED_OPEN_SECTION_ACTION = XPOSED_PACKAGE + ".OPEN_SECTION";
    String XPOSED_EXTRA_SECTION = "section";
    // New Xposed installer
    String XPOSED_ACTIVITY = XPOSED_PACKAGE + ".WelcomeActivity";
    String XPOSED_EXTRA_FRAGMENT = "fragment";
    /* Xposed Installer end */
}
