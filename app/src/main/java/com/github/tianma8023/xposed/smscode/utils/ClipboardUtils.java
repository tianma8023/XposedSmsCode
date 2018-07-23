package com.github.tianma8023.xposed.smscode.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardUtils {

    private ClipboardUtils() {
    }

    public static void copyToClipboard(Context context, String text) {
        try {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Copy text", text);
            clipboardManager.setPrimaryClip(clipData);
            XLog.i("Copy to clipboard succeed");
        } catch (Throwable e) {
            XLog.e("Copy to clipboard failed.", e);
        }
    }

}
