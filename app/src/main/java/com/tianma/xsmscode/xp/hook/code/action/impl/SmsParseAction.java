package com.tianma.xsmscode.xp.hook.code.action.impl;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.tianma.xsmscode.common.utils.SmsCodeUtils;
import com.tianma.xsmscode.common.utils.StringUtils;
import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.common.utils.XSPUtils;
import com.tianma.xsmscode.data.db.entity.SmsMsg;
import com.tianma.xsmscode.feature.store.EntityStoreManager;
import com.tianma.xsmscode.feature.store.EntityType;
import com.tianma.xsmscode.xp.hook.code.action.CallableAction;

import de.robv.android.xposed.XSharedPreferences;

/**
 * 解析短信中的验证码
 */
public class SmsParseAction extends CallableAction {

    public static final String SMS_MSG = "sms_msg";
    public static final String SMS_DUPLICATED = "sms_duplicated";

    private Intent mSmsIntent;

    public SmsParseAction(Context pluginContext, Context phoneContext, SmsMsg smsMsg, XSharedPreferences xsp) {
        super(pluginContext, phoneContext, smsMsg, xsp);
    }

    public void setSmsIntent(Intent smsIntent) {
        mSmsIntent = smsIntent;
    }

    @Override
    public Bundle action() {
        return parseSmsMsg();
    }

    private Bundle parseSmsMsg() {
        mSmsMsg = SmsMsg.fromIntent(mSmsIntent);

        String sender = mSmsMsg.getSender();
        String msgBody = mSmsMsg.getBody();
        if (BuildConfig.DEBUG) {
            XLog.d("Sender: %s", sender);
            XLog.d("Body: %s", msgBody);
        } else {
            XLog.d("Sender: %s", StringUtils.escape(sender));
            XLog.d("Body: %s", StringUtils.escape(msgBody));
        }

        if (TextUtils.isEmpty(sender) || TextUtils.isEmpty(msgBody)) {
            return null;
        }

        String smsCode = SmsCodeUtils.parseSmsCodeIfExists(mPluginContext, msgBody, true);
        if (TextUtils.isEmpty(smsCode)) { // isn't code message
            return null;
        }

        mSmsMsg.setSmsCode(smsCode);
        mSmsMsg.setCompany(SmsCodeUtils.parseCompany(msgBody));
        long timestamp = System.currentTimeMillis();
        mSmsMsg.setDate(timestamp);

        Bundle bundle = new Bundle();
        bundle.putParcelable(SMS_MSG, mSmsMsg);

        // 去除重复短信
        boolean duplicated = false;
        if (XSPUtils.deduplicateSms(xsp)) {
            SmsMsg prevSmsMsg = EntityStoreManager.loadEntityFromFile(EntityType.PREV_SMS_MSG, SmsMsg.class);
            if (prevSmsMsg != null) {
                if (Math.abs(timestamp - prevSmsMsg.getDate()) <= 15000) {
                    if ((sender.equals(prevSmsMsg.getSender()) && smsCode.equals(prevSmsMsg.getSmsCode()))
                            || msgBody.equals(prevSmsMsg.getBody())) {
                        duplicated = true;
                        XLog.d("Duplicated message, ignore");
                    }
                }
            }
            // 保存当前验证码记录 Action
            EntityStoreManager.storeEntityToFile(EntityType.PREV_SMS_MSG, mSmsMsg);
        }

        bundle.putBoolean(SMS_DUPLICATED, duplicated);
        return bundle;
    }


}
