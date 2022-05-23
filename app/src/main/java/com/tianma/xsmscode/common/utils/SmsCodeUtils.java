package com.tianma.xsmscode.common.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.tianma.xsmscode.common.constant.PrefConst;
import com.tianma.xsmscode.data.db.DBProvider;
import com.tianma.xsmscode.data.db.entity.SmsCodeRule;
import com.tianma.xsmscode.data.db.entity.SmsCodeRuleDao;
import com.tianma.xsmscode.feature.store.EntityStoreManager;
import com.tianma.xsmscode.feature.store.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XSharedPreferences;

/**
 * 验证码相关Utils
 */
public class SmsCodeUtils {

    private SmsCodeUtils() {
    }

    /**
     * 是否包含中文
     *
     * @param text text
     */
    private static boolean containsChinese(String text) {
        String regex = "[\u4e00-\u9fa5]|。";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }

    /**
     * 解析文本内容中的验证码关键字，如果有则返回第一个匹配到的关键字，否则返回 空字符串
     */
    private static String parseKeyword(String keywordsRegex, String content) {
        Pattern pattern = Pattern.compile(keywordsRegex);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    private static String loadCodeKeywordsBySP(Context context) {
        return SPUtils.getSMSCodeKeywords(context);
    }

    private static String loadCodeKeywordsByXSP() {
        XSharedPreferences preferences = new XSharedPreferences(BuildConfig.APPLICATION_ID, PrefConst.PREF_NAME);
        return XSPUtils.getSMSCodeKeywords(preferences);
    }

    /**
     * 解析文本中的验证码并返回，如果不存在返回空字符
     */
    public static String parseSmsCodeIfExists(Context context, String content, boolean useXSP) {
        String result = parseByCustomRules(context, content);
        if (TextUtils.isEmpty(result)) {
            result = parseByDefaultRule(context, content, useXSP);
        }
        return result;
    }

    /**
     * Parse SMS code by default rule
     *
     * @param context context
     * @param content message body
     * @param useXSP  whether use XSharedPreferences or not.
     * @return the SMS code if matches, otherwise return empty string
     */
    private static String parseByDefaultRule(Context context, String content, boolean useXSP) {
        String result = "";
        String keywordsRegex;
        if (useXSP) {
            keywordsRegex = loadCodeKeywordsByXSP();
        } else {
            keywordsRegex = loadCodeKeywordsBySP(context);
        }
        String keyword = parseKeyword(keywordsRegex, content);
        if (!TextUtils.isEmpty(keyword)) {
            if (containsChinese(content)) {
                result = getSmsCodeCN(keyword, content);
            } else {
                result = getSmsCodeEN(keyword, content);
            }
        }
        return result;
    }

    /**
     * 获取中文短信中包含的验证码
     */
    private static String getSmsCodeCN(String keyword, String content) {
        // 之前的正则表达式是 [a-zA-Z0-9]{4,8}
        // 现在的正则表达式是 [a-zA-Z0-9]+(\.[a-zA-Z0-9]+)? 匹配数字和字母之间最多一个.的字符串
        // 之前的不能识别和剔除小数，比如 123456.231，很容易就把 123456 作为验证码。
        String codeRegex = "(?<![a-zA-Z0-9])[a-zA-Z0-9]{4,8}(?![a-zA-Z0-9])";
        // 先去掉所有空白字符处理
        String handledContent = removeAllWhiteSpaces(content);
        String smsCode = getSmsCode(codeRegex, keyword, handledContent);
        if (TextUtils.isEmpty(smsCode)) {
            // 没解析出就按照原文本再处理一遍
            smsCode = getSmsCode(codeRegex, keyword, content);
        }
        return smsCode;
    }

    /**
     * 获取英文短信包含的验证码
     */
    private static String getSmsCodeEN(String keyword, String content) {
        // 之前的正则表达式是 [0-9]{4,8} 匹配由数字组成的4到8长度的字符串
        // 现在的正则表达式是 [0-9]+(\\.[0-9]+)? 匹配数字之间最多一个.的字符串
        // 之前的不能识别和剔除小数，比如 123456.231，很容易就把 123456 作为验证码。
        String codeRegex = "(?<![0-9])[0-9]{4,8}(?![0-9])";
        String smsCode = getSmsCode(codeRegex, keyword, content);
        if (TextUtils.isEmpty(smsCode)) {
            // 没解析出就去掉所有空白字符再处理
            content = removeAllWhiteSpaces(content);
            smsCode = getSmsCode(codeRegex, keyword, content);
        }
        return smsCode;
    }

    /**
     * Remove all white spaces.
     */
    private static String removeAllWhiteSpaces(String content) {
        return content.replaceAll("\\s*", "");
    }

    /**
     * Parse SMS code
     *
     * @param codeRegex SMS code regular expression
     * @param keyword   SMS code SMS keywords expression
     * @param content   SMS content
     * @return the SMS code if it's found, otherwise return empty string ""
     */
    private static String getSmsCode(String codeRegex, String keyword, String content) {
        Pattern p = Pattern.compile(codeRegex);
        Matcher m = p.matcher(content);
        List<String> possibleCodes = new ArrayList<>();
        while (m.find()) {
            final String matchedStr = m.group();
            possibleCodes.add(matchedStr);
        }
        if (possibleCodes.isEmpty()) { // no possible code
            return "";
        }

        List<String> filteredCodes = new ArrayList<>();
        for (String possibleCode : possibleCodes) {
            if (isNearToKeyword(keyword, possibleCode, content)) {
                filteredCodes.add(possibleCode);
            }
        }
        if (filteredCodes.isEmpty()) { // no possible code near to keywords
            filteredCodes = possibleCodes;
        }

        int maxMatchLevel = LEVEL_NONE;
        // minimum distance of possible code to keyword
        int minDistance = content.length();
        String smsCode = "";
        for (String filteredCode : filteredCodes) {
            final int curLevel = getMatchLevel(filteredCode);
            if (curLevel > maxMatchLevel) {
                maxMatchLevel = curLevel;
                // reset the minDistance
                minDistance = distanceToKeyword(keyword, filteredCode, content);
                smsCode = filteredCode;
            } else if (curLevel == maxMatchLevel) {
                int curDistance = distanceToKeyword(keyword, filteredCode, content);
                if (curDistance < minDistance) {
                    minDistance = curDistance;
                    smsCode = filteredCode;
                }
            }
        }
        return smsCode;
    }

    /* 匹配度：6位纯数字，匹配度最高 */
    private static final int LEVEL_DIGITAL_6 = 4;
    /* 匹配度：4位纯数字，匹配度次之 */
    private static final int LEVEL_DIGITAL_4 = 3;
    /* 匹配度：纯数字, 匹配度次之 */
    private static final int LEVEL_DIGITAL_OTHERS = 2;
    /* 匹配度：数字+字母 混合, 匹配度次之 */
    private static final int LEVEL_TEXT = 1;
    /* 匹配度：纯字母, 匹配度最低 */
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

    /**
     * 可能的验证码是否靠近关键字；
     * @return 可能的验证码前后30个字符内是否包含验证码关键字，如果包含则返回true；否则返回false
     */
    private static boolean isNearToKeyword(String keyword, String possibleCode, String content) {
        return distanceToKeyword(keyword, possibleCode, content) <= 30;
    }

    /**
     * 计算可能的验证码与关键字的距离
     */
    private static int distanceToKeyword(String keyword, String possibleCode, String content) {
        int keywordIdx = content.indexOf(keyword);
        int possibleCodeIdx = content.indexOf(possibleCode);
        return Math.abs(keywordIdx - possibleCodeIdx);
    }

    /**
     * Parse SMS code by custom rules
     *
     * @param context context
     * @param content message body
     * @return the SMS code if matches, otherwise return empty string
     */
    private static String parseByCustomRules(Context context, String content) {
        List<SmsCodeRule> rules = queryAllSmsCodeRules(context);
        String lowerContent = content.toLowerCase();
        for (SmsCodeRule rule : rules) {
            if (lowerContent.contains(rule.getCompany().toLowerCase())
                    && lowerContent.contains(rule.getCodeKeyword().toLowerCase())) {
                Pattern pattern = Pattern.compile(rule.getCodeRegex());
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    return matcher.group();
                }
            }
        }
        return "";
    }

    private static List<SmsCodeRule> queryAllSmsCodeRules(Context context) {
        List<SmsCodeRule> rules = new ArrayList<>();
        try {
            Uri smsCodeRuleUri = DBProvider.SMS_CODE_RULE_URI;
            ContentResolver resolver = context.getContentResolver();

            final String companyColumn = SmsCodeRuleDao.Properties.Company.columnName;
            final String keywordColumn = SmsCodeRuleDao.Properties.CodeKeyword.columnName;
            final String regexColumn = SmsCodeRuleDao.Properties.CodeRegex.columnName;

            String[] projection = {
                    companyColumn,
                    keywordColumn,
                    regexColumn,
            };

            Cursor cursor = resolver.query(smsCodeRuleUri, projection, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    SmsCodeRule rule = new SmsCodeRule();
                    rule.setCompany(cursor.getString(cursor.getColumnIndexOrThrow(companyColumn)));
                    rule.setCodeKeyword(cursor.getString(cursor.getColumnIndexOrThrow(keywordColumn)));
                    rule.setCodeRegex(cursor.getString(cursor.getColumnIndexOrThrow(regexColumn)));
                    rules.add(rule);
                }
                cursor.close();
                XLog.d("Load SmsCode rules succeed by content provider");
            } else {
                throw new Exception("Cursor is null");
            }
        } catch (Throwable e) {
            rules = EntityStoreManager.loadEntitiesFromFile(
                    EntityType.CODE_RULES, SmsCodeRule.class
            );
            XLog.d("Load SmsCode rules by file");
        }
        return rules;
    }

    /**
     * Parse company info from message content if it exists
     *
     * @param content message content
     * @return company info if it exists, otherwise return empty string
     */
    public static String parseCompany(String content) {
        String regex = "((?<=【)(.*?)(?=】))|((?<=\\[)(.*?)(?=\\]))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        List<String> possibleCompanies = new ArrayList<>();
        while (matcher.find()) {
            possibleCompanies.add(matcher.group());
        }
        StringBuilder sb = new StringBuilder();
        boolean needSpace = false; // 是否需要空格分隔
        for (String company : possibleCompanies) {
            if (needSpace) {
                sb.append(' ');
            } else {
                needSpace = true;
            }
            sb.append(company);
        }
        return sb.toString();
    }
}
