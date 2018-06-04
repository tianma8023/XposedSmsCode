package com.github.tianma8023.xposed.smscode.utils;

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
    public static boolean containsChinese(String text) {
        String regex = "[\u4e00-\u9fa5]|【|】|。";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
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

    /* 匹配度：纯数字, 匹配度最高*/
    private static final int LEVEL_DIGITAL = 2;
    /* 匹配度：数字+字母 混合, 匹配度其次*/
    private static final int LEVEL_TEXT = 1;
    /* 匹配度：纯字母, 匹配度最低*/
    private static final int LEVEL_CHARACTER = 0;
    private static final int LEVEL_NONE = -1;

    private static int getMatchLevel(String matchedStr) {
        if (matchedStr.matches("^[0-9]*$"))
            return LEVEL_DIGITAL;
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
}
