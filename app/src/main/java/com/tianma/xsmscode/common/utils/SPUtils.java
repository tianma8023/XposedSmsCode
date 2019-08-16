package com.tianma.xsmscode.common.utils;

import android.content.Context;

import com.tianma.xsmscode.common.constant.PrefConst;

public class SPUtils {

    // 本地的版本号
    private static final String LOCAL_VERSION_CODE = "local_version_code";
    private static final int LOCAL_VERSION_CODE_DEFAULT = 16;

    private SPUtils() {

    }

    /**
     * 获取本地记录的版本号
     */
    public static int getLocalVersionCode(Context context) {
        // 如果不存在,则默认返回16,即v1.4.5版本
        return PreferencesUtils.getInt(context,
                LOCAL_VERSION_CODE, LOCAL_VERSION_CODE_DEFAULT);
    }


    /**
     * 设置当前版本号
     */
    public static void setLocalVersionCode(Context context, int versionCode) {
        PreferencesUtils.putInt(context, LOCAL_VERSION_CODE, versionCode);
    }

    /**
     * 获取短信验证码关键字
     */
    public static String getSMSCodeKeywords(Context context) {
        return PreferencesUtils.getString(context,
                PrefConst.KEY_SMSCODE_KEYWORDS, PrefConst.SMSCODE_KEYWORDS_DEFAULT);
    }


}
