package com.github.tianma8023.xposed.smscode.utils;

import android.content.Context;
import android.text.TextUtils;

import com.crossbowffs.remotepreferences.RemotePreferences;
import com.github.tianma8023.xposed.smscode.constant.SmsCodeConst;
import com.github.tianma8023.xposed.smscode.db.DBManager;
import com.github.tianma8023.xposed.smscode.entity.SmsCodeRule;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 验证码相关Utils
 */
public class SmsCodeUtils {

    private SmsCodeUtils() {
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
    public static boolean containsCodeKeywords(Context context, String content) {
        String keywordsRegex = loadVerificationKeywords(context);
        return containsCodeKeywords(keywordsRegex, content);
    }

    /**
     * 是否包含短信验证码关键字
     *
     * @param keywordsRegex SMS code message keywords (regex expressions)
     * @param content       sms message content
     */
    private static boolean containsCodeKeywords(String keywordsRegex, String content) {
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
    public static String parseSmsCodeIfExists(Context context, String content) {
        String result = parseByCustomRules(context, content);
        if (TextUtils.isEmpty(result)) {
            result = parseByDefaultRule(context, content);
        }
        return result;
    }

    /**
     * Parse SMS code by default rule
     *
     * @param context context
     * @param content message body
     * @return the SMS code if matches, otherwise return empty string
     */
    private static String parseByDefaultRule(Context context, String content) {
        String result = "";
        String keywordsRegex = loadVerificationKeywords(context);
        if (containsCodeKeywords(keywordsRegex, content)) {
            if (containsChinese(content)) {
                result = getSmsCodeCN(keywordsRegex, content);
            } else {
                result = getSmsCodeEN(keywordsRegex, content);
            }
        }
        return result;
    }

    /**
     * 获取中文短信中包含的验证码
     */
    private static String getSmsCodeCN(String keywordsRegex, String content) {
        // 之前的正则表达式是 [a-zA-Z0-9]{4,8}
        // 现在的正则表达式是 [a-zA-Z0-9]+(\.[a-zA-Z0-9]+)? 匹配数字和字母之间最多一个.的字符串
        // 之前的不能识别和剔除小数，比如 123456.231，很容易就把 123456 作为验证码。
        String codeRegex = "[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)?";
        return getSmsCode(codeRegex, keywordsRegex, content);
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
        return containsCodeKeywords(keywordsRegex, content.substring(beginIndex, endIndex));
    }

    /**
     * 获取英文短信包含的验证码
     */
    private static String getSmsCodeEN(String keywordsRegex, String content) {
        // 之前的正则表达式是 [0-9]{4,8} 匹配由数字组成的4到8长度的字符串
        // 现在的正则表达式是 [0-9]+(\\.[0-9]+)? 匹配数字之间最多一个.的字符串
        // 之前的不能识别和剔除小数，比如 123456.231，很容易就把 123456 作为验证码。
        String codeRegex = "[0-9]+(\\.[0-9]+)?";
        return getSmsCode(codeRegex, keywordsRegex, content);
    }

    /*
     * Parse SMS code
     *
     * @param codeRegex SMS code regular expression
     * @param keywordsRegex     SMS code SMS keywords expression
     * @param content           SMS content
     * @return the SMS code if it's found, otherwise return empty string ""
     */
    private static String getSmsCode(String codeRegex, String keywordsRegex, String content) {
        Pattern p = Pattern.compile(codeRegex);
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
        String smsCode = "";
        for (String possibleCode : possibleCodes) {
            if (isNearToKeywords(keywordsRegex, possibleCode, content)) {
                final int curLevel = getMatchLevel(possibleCode);
                if (curLevel > maxMatchLevel) {
                    maxMatchLevel = curLevel;
                    smsCode = possibleCode;
                }
            }
        }
        if (maxMatchLevel == LEVEL_NONE) { // no possible code near to keywords
            for (String possibleCode : possibleCodes) {
                final int curLevel = getMatchLevel(possibleCode);
                if (curLevel > maxMatchLevel) {
                    maxMatchLevel = curLevel;
                    smsCode = possibleCode;
                }
            }
        }
        return smsCode;
    }

    public static boolean isPossiblePhoneNumber(String text) {
        return text.matches("\\d{8,}");
    }

    public static boolean containsPhoneNumberKeywords(String content) {
        Pattern pattern = Pattern.compile(SmsCodeConst.PHONE_NUMBER_KEYWORDS);
        Matcher matcher = pattern.matcher(content);
        return matcher.find();
    }

    /**
     * Parse SMS code by custom rules
     *
     * @param context context
     * @param content message body
     * @return the SMS code if matches, otherwise return empty string
     */
    private static String parseByCustomRules(Context context, String content) {
        List<SmsCodeRule> rules = DBManager.get(context).queryAllSmsCodeRules();
        String lowerContent = content.toLowerCase();
        for (SmsCodeRule rule : rules) {
            if (lowerContent.contains(rule.getCompany().toLowerCase())
                    && content.contains(rule.getCodeKeyword().toLowerCase())) { // case insensitive
                Pattern pattern = Pattern.compile(rule.getCodeRegex());
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    return matcher.group();
                }
            }
        }
        return "";
    }

    /**
     * Parse company info from message content if it exists
     *
     * @param content message content
     * @return company info if it exists, otherwise return empty string
     */
    public static String parseCompany(String content) {
        String regex = "((?<=【)(.*)(?=】))|((?<=\\[)(.*)(?=\\]))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        List<String> possibleCompanies = new ArrayList<>();
        while (matcher.find()) {
            possibleCompanies.add(matcher.group());
        }
        StringBuilder sb = new StringBuilder();
        boolean needComma = false; // 是否需要逗号分隔
        for (String company : possibleCompanies) {
            if (needComma) {
                sb.append(", ");
            } else {
                needComma = true;
            }
            sb.append(company);
        }
        return sb.toString();
    }
}
