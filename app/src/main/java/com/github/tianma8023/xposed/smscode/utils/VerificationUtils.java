package com.github.tianma8023.xposed.smscode.utils;

import android.content.Context;

import com.crossbowffs.remotepreferences.RemotePreferences;
import com.github.tianma8023.xposed.smscode.constant.IPrefConstants;
import com.github.tianma8023.xposed.smscode.constant.ISmsCodeConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 验证码相关Utils
 */
public class VerificationUtils {

    private VerificationUtils() {
    }

    /**
     * 是否包含中文
     *
     * @param text
     * @return
     */
    private static boolean containsChinese(String text) {
        String regex = "[\u4e00-\u9fa5]|。";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }

    /**
     * 是否是验证码短信
     *
     * @param context context
     * @param content content
     * @return
     */
    private static boolean isVerificationMsg(Context context, String content) {
        return containsVerificationKeywords(context, content);
    }

    /**
     * 是否包含验证码短信关键字
     *
     * @param context context
     * @param content content
     * @return
     */
    public static boolean containsVerificationKeywords(Context context, String content) {
        String keywordsRegex = loadVerificationKeywords(context);
        Pattern pattern = Pattern.compile(keywordsRegex);
        Matcher matcher = pattern.matcher(content);
        return matcher.find();
    }

    private static String loadVerificationKeywords(Context context) {
        RemotePreferences preferences = RemotePreferencesUtils.getDefaultRemotePreferences(context);
        return RemotePreferencesUtils.getStringPref(preferences, IPrefConstants.KEY_SMSCODE_KEYWORDS, IPrefConstants.KEY_SMSCODE_KEYWORDS_DEFAULT);
    }

    /**
     * 解析文本中的验证码并返回，如果不存在返回空字符
     */
    public static String parseVerificationCodeIfExists(Context context, String content) {
        String result = "";
        if (isVerificationMsg(context, content)) {
            if (containsChinese(content)) {
                result = getVerificationCodeCN(content);
            } else {
                result = getVerificationCodeEN(content);
            }
        }
        return result;
    }

    /**
     * 是否是中文验证码短信
     *
     * @return
     */
    public static boolean isVerificationMsgCN(String content) {
        boolean result = false;
        for (String keyWord : ISmsCodeConstants.VERIFICATION_KEY_WORDS_CN) {
            if (content.contains(keyWord)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * 是否是英文验证码短信
     *
     * @param content
     * @return
     */
    public static boolean isVerificationMsgEN(String content) {
        boolean result = false;
        for (String keyWord : ISmsCodeConstants.VERIFICATION_KEY_WORDS_EN) {
            if (content.contains(keyWord)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * 获取中文短信中包含的验证码
     *
     * @param content
     * @return
     */
    public static String getVerificationCodeCN(String content) {
        String regex = "[a-zA-Z0-9]{4,8}";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        String verificationCode = "";
        int maxMatchLevel = LEVEL_NONE;
        while (m.find()) {
            final String matchedStr = m.group();
            final int curLevel = getMatchLevel(matchedStr);
            if (curLevel > maxMatchLevel) {
                maxMatchLevel = curLevel;
                verificationCode = matchedStr;
            }
        }
        return verificationCode;
    }

    /* 匹配度：6位纯数字，匹配度最高 */
    private static final int LEVEL_DIGITAL_6 = 4;
    /* 匹配度：4位纯数字，匹配度次之 */
    private static final int LEVEL_DIGITAL_4 = 3;
    /* 匹配度：纯数字, 匹配度最高*/
    private static final int LEVEL_DIGITAL_OTHERS = 2;
    /* 匹配度：数字+字母 混合, 匹配度其次*/
    private static final int LEVEL_TEXT = 1;
    /* 匹配度：纯字母, 匹配度最低*/
    private static final int LEVEL_CHARACTER = 0;
    private static final int LEVEL_NONE = -1;

    private static int getMatchLevel(String matchedStr) {
        if (matchedStr.matches("^[0-9]{6}$"))
            return LEVEL_DIGITAL_6;
        if (matchedStr.matches("^[0-9]{4}$"))
            return LEVEL_DIGITAL_4;
        if (matchedStr.matches("^[0-9]*$"))
            return LEVEL_DIGITAL_OTHERS;
        if (matchedStr.matches("^[a-zA-Z]*$"))
            return LEVEL_CHARACTER;
        return LEVEL_TEXT;
    }

    public static String getVerificationCodeEN(String content) {
        String regex = "[0-9]{4,8}";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        if (m.find()) {
            return m.group();
        }
        return "";
    }

    public static boolean maybeVerificationCode(String text) {
        return text.matches("[a-zA-Z0-9]{4,8}");
    }
}
