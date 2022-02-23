package com.tianma.xsmscode.xp.hook.code.action.impl;

import android.content.Context;
import android.os.Bundle;

import com.tianma.xsmscode.common.utils.ClipboardUtils;
import com.tianma.xsmscode.common.utils.XSPUtils;
import com.tianma.xsmscode.data.db.entity.SmsMsg;
import com.tianma.xsmscode.xp.hook.code.action.RunnableAction;

import de.robv.android.xposed.XSharedPreferences;

/**
 * 将验证码复制到剪切板
 */
public class CopyToClipboardAction extends RunnableAction {

    public CopyToClipboardAction(Context pluginContext, Context phoneContext, SmsMsg smsMsg, XSharedPreferences xsp) {
        super(pluginContext, phoneContext, smsMsg, xsp);
    }

    @Override
    public Bundle action() {
        if (XSPUtils.copyToClipboardEnabled(xsp)) {
            copyToClipboard();
        }
        return null;
    }

    private void copyToClipboard() {
        ClipboardUtils.copyToClipboard(mPluginContext, mSmsMsg.getSmsCode());
    }
}
