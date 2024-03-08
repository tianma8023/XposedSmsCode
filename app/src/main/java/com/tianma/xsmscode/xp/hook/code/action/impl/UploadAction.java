package com.tianma.xsmscode.xp.hook.code.action.impl;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.github.tianma8023.xposed.smscode.R;
import com.tianma.xsmscode.common.utils.RequestUtils;
import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.common.utils.XSPUtils;
import com.tianma.xsmscode.data.db.entity.SmsMsg;
import com.tianma.xsmscode.xp.hook.code.action.RunnableAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XSharedPreferences;

/**
 * 显示验证码Toast
 */
public class UploadAction extends RunnableAction {

    public UploadAction(Context pluginContext, Context phoneContext, SmsMsg smsMsg, XSharedPreferences xsp) {
        super(pluginContext, phoneContext, smsMsg, xsp);
    }

    @Override
    public Bundle action() {
        if (XSPUtils.uploadToServer(xsp)) {
            uploadSmsToServer();
        }
        return null;
    }

    private void uploadSmsToServer() {
        String text = mSmsMsg.getBody(); //接收到验证的信息
        String code = mSmsMsg.getSmsCode(); //接收到验证的信息
        HashMap<String, String> uploadMap = new HashMap<>();
        uploadMap.put("msg",text);
        uploadMap.put("code",code);
        String url = XSPUtils.getServerUrl(xsp);
        XLog.i("当前的url为:"+url);

        new Thread(new Runnable() {
            @Override
            public void run() {
                new RequestUtils().run(uploadMap,url);

            }
        }).start();

        if (mPhoneContext != null) {
            Toast.makeText(mPhoneContext, text, Toast.LENGTH_LONG).show();
        }
    }
}
