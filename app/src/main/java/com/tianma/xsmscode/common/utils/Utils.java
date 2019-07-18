package com.tianma.xsmscode.common.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.github.tianma8023.xposed.smscode.R;

import java.util.Locale;

import androidx.browser.customtabs.CustomTabsIntent;

/**
 * Other Utils
 */
public class Utils {

    private Utils() {
    }

    public static void showWebPage(Context context, String url) {
        try {
            CustomTabsIntent cti = new CustomTabsIntent.Builder().build();
            cti.launchUrl(context, Uri.parse(url));
        } catch (Exception e) {
            Toast.makeText(context, R.string.browser_install_or_enable_prompt, Toast.LENGTH_SHORT).show();
        }
    }

    private static String getLanguagePath() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String result = "en";
        if ("zh".equals(language)) {
            if ("CN".equalsIgnoreCase(country)) {
                result = "zh-CN";
            } else if ("HK".equalsIgnoreCase(country) || "TW".equalsIgnoreCase(country)) {
                result = "zh-TW";
            }
        }
        return result;
    }

    public static String getProjectDocUrl(String docBaseUrl, String docPath) {
        return docBaseUrl + "/" + getLanguagePath() + "/" + docPath;
    }

    public static boolean isValidFilename(String filename) {
        if (filename == null || filename.trim().length() == 0) {
            return false;
        }

        if (".".equals(filename.trim()) || "..".equals(filename.trim())) {
            return false;
        }

        for(int i = 0; i < filename.length(); i++) {
            char c = filename.charAt(i);
            if (!isValidFilenameChar(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidFilenameChar(char c) {
        // check control characters
        if (c <= 1f || c == 7f) {
            return false;
        }

        // check special characters
        switch (c) {
            case '"':
            case '*':
            case '/':
            case '\\':
            case '<':
            case '>':
            case '|':
            case '?':
            case ',':
            case ';':
            case ':':
                return false;
        }

        return true;
    }
}
