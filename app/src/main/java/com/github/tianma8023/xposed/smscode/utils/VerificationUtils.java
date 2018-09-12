package com.github.tianma8023.xposed.smscode.utils;

import android.content.Context;

import com.crossbowffs.remotepreferences.RemotePreferences;
import com.github.tianma8023.xposed.smscode.constant.SmsCodeConst;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 验证码相关Utils
 */
public class VerificationUtils {

    private VerificationUtils() {
    }

    /**
     * 文本是否包含中文
     */
    private static boolean containsChinese(String text) {
        String regex = "[\u4e00-\u9fa5]|。";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }

    /**
     * 是否包含验证码短信关键字
     *
     * @param context context
     * @param content sms message content
     */
    public static boolean containsVerificationKeywords(Context context, String content) {
        String keywordsRegex = loadVerificationKeywords(context);
        return containsVerificationKeywords(keywordsRegex, content);
    }

    /**
     * 是否包含短信验证码关键字
     *
     * @param keywordsRegex verification message keywords (regex expressions)
     * @param content       sms message content
     */
    private static boolean containsVerificationKeywords(String keywordsRegex, String content) {
        Pattern pattern = Pattern.compile(keywordsRegex);
        Matcher matcher = pattern.matcher(content);
        return matcher.find();
    }

    private static String loadVerificationKeywords(Context context) {
        RemotePreferences preferences = RemotePreferencesUtils.getDefaultRemotePreferences(context);
        return SPUtils.getSMSCodeKeywords(preferences);
    }

    /**
     * 解析文本中的验证码并返回，如果不存在返回空字符
     */
    public static String parseVerificationCodeIfExists(Context context, String content) {
        String result = "";
        String keywordsRegex = loadVerificationKeywords(context);
        if (containsVerificationKeywords(keywordsRegex, content)) {
            if (containsChinese(content)) {
                result = getVerificationCodeCN(keywordsRegex, content);
            } else {
                result = getVerificationCodeEN(keywordsRegex, content);
            }
        }
        return result;
    }

    /**
     * 是否是中文验证码短信
     */
    public static boolean isVerificationMsgCN(String content) {
        boolean result = false;
        for (String keyWord : SmsCodeConst.VERIFICATION_KEY_WORDS_CN) {
            if (content.contains(keyWord)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * 是否是英文验证码短信
     */
    public static boolean isVerificationMsgEN(String content) {
        boolean result = false;
        for (String keyWord : SmsCodeConst.VERIFICATION_KEY_WORDS_EN) {
            if (content.contains(keyWord)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * 获取中文短信中包含的验证码
     */
    private static String getVerificationCodeCN(String keywordsRegex, String content) {
        // 之前的正则表达式是 [a-zA-Z0-9]{4,8}
        // 现在的正则表达式是 [a-zA-Z0-9]+(\.[a-zA-Z0-9]+)? 匹配数字和字母之间最多一个.的字符串
        // 之前的不能识别和剔除小数，比如 123456.231，很容易就把 123456 作为验证码。
        String verificationRegex = "[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)?";
        return getVerificationCode(verificationRegex, keywordsRegex, content);
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

    private static int getMatchLevel(String possibleCode) {
        if (possibleCode.matches("^[0-9]{6}$"))
            return LEVEL_DIGITAL_6;
        if (possibleCode.matches("^[0-9]{4}$"))
            return LEVEL_DIGITAL_4;
        if (possibleCode.matches("^[0-9]*$"))
            return LEVEL_DIGITAL_OTHERS;
        if (possibleCode.matches("^[a-zA-Z]*$"))
            return LEVEL_CHARACTER;
        return LEVEL_TEXT;
    }

    private static boolean isNearToKeywords(String keywordsRegex, String possibleCode, String content) {
        int beginIndex = 0, endIndex = content.length() - 1;
        int curIndex = content.indexOf(possibleCode);
        int strLength = possibleCode.length();
        int magicNumber = 14;
        if (curIndex - magicNumber > 0) {
            beginIndex = curIndex - magicNumber;
        }
        if (curIndex + strLength + magicNumber < endIndex) {
            endIndex = curIndex + strLength + magicNumber;
        }
        return containsVerificationKeywords(keywordsRegex, content.substring(beginIndex, endIndex));
    }

    /**
     * 获取英文短信包含的验证码
     */
    private static String getVerificationCodeEN(String keywordsRegex, String content) {
        // 之前的正则表达式是 [0-9]{4,8} 匹配由数字组成的4到8长度的字符串
        // 现在的正则表达式是 [0-9]+(\\.[0-9]+)? 匹配数字之间最多一个.的字符串
        // 之前的不能识别和剔除小数，比如 123456.231，很容易就把 123456 作为验证码。
        String verificationRegex = "[0-9]+(\\.[0-9]+)?";
        return getVerificationCode(verificationRegex, keywordsRegex, content);
    }

    /*
     * Parse verification code
     *
     * @param verificationRegex verification code regular expression
     * @param keywordsRegex     verification code SMS keywords expression
     * @param content           SMS content
     * @return the verification code if it's found, otherwise return empty string ""
     */
    private static String getVerificationCode(String verificationRegex,
                                              String keywordsRegex, String content) {
        Pattern p = Pattern.compile(verificationRegex);
        Matcher m = p.matcher(content);
        List<String> possibleCodes = new ArrayList<>();
        while (m.find()) {
            final String matchedStr = m.group();
            if (matchedStr.length() >= 4 && matchedStr.length() <= 8 && !matchedStr.contains(".")) {
                possibleCodes.add(matchedStr);
            }
        }
        if (possibleCodes.isEmpty()) { // no possible code
            return "";
        }
        int maxMatchLevel = LEVEL_NONE;
        String verificationCode = "";
        for (String possibleCode : possibleCodes) {
            if (isNearToKeywords(keywordsRegex, possibleCode, content)) {
                final int curLevel = getMatchLevel(possibleCode);
                if (curLevel > maxMatchLevel) {
                    maxMatchLevel = curLevel;
                    verificationCode = possibleCode;
                }
            }
        }
        if (maxMatchLevel == LEVEL_NONE) { // no possible code near to keywords
            for (String possibleCode : possibleCodes) {
                final int curLevel = getMatchLevel(possibleCode);
                if (curLevel > maxMatchLevel) {
                    maxMatchLevel = curLevel;
                    verificationCode = possibleCode;
                }
            }
        }
        return verificationCode;
    }

    public static boolean isPossiblePhoneNumber(String text) {
        return text.matches("\\d{8,}");
    }

    public static boolean containsPhoneNumberKeywords(String content) {
        Pattern pattern = Pattern.compile(SmsCodeConst.PHONE_NUMBER_KEYWORDS);
        Matcher matcher = pattern.matcher(content);
        return matcher.find();
    }
}
