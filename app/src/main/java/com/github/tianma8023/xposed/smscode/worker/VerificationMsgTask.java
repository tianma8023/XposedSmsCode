package com.github.tianma8023.xposed.smscode.worker;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.github.tianma8023.xposed.smscode.entity.SmsMessageData;
import com.github.tianma8023.xposed.smscode.utils.ClipboardUtils;
import com.github.tianma8023.xposed.smscode.utils.VerificationUtils;
import com.github.tianma8023.xposed.smscode.utils.XLog;

/**
 * 短信验证码相关的Task
 */
public class VerificationMsgTask implements Runnable {

    private SmsMessageData mSmsMessageData;
    private Context mContext;

    public VerificationMsgTask(Context context, SmsMessageData smsMessageData) {
        mContext = context;
        mSmsMessageData = smsMessageData;
    }

    @Override
    public void run() {
        doWork();
    }

    private void doWork() {

        String msgBody = mSmsMessageData.getBody();
        if (TextUtils.isEmpty(msgBody))
            return;
        // Check whether it's a verification message
        String verificationCode = "";
        if (VerificationUtils.containsChinese(msgBody)) {
            XLog.i("Message body contains Chinese character");
            if (VerificationUtils.isVerificationMsgCN(msgBody)) {
                XLog.i("Is Chinese verification code message");
                verificationCode = VerificationUtils.getVerificationCodeCN(msgBody);
            }
        } else {
            XLog.i("Message body does not contain Chinese character");
            if (VerificationUtils.isVerificationMsgEN(msgBody)) {
                XLog.i("Is English verification code message");
                verificationCode = VerificationUtils.getVerificationCodeEN(msgBody);
            }
        }
        if (!TextUtils.isEmpty(verificationCode)) {
            XLog.i("Verification code: %s", verificationCode);
            Message msg = new Message();
            msg.obj = verificationCode;
            msg.what = FLAG_COPY_TO_CLIPBOARD;
            copyHandler.sendMessage(msg);
        }
    }

    private static final int FLAG_COPY_TO_CLIPBOARD = 0xff;

    private Handler copyHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case FLAG_COPY_TO_CLIPBOARD:
                    copyToClipboardOnMainThread((String) msg.obj);
                    break;
            }
            return false;
        }
    });

    /**
     * 在主线程上执行copy操作
     */
    private void copyToClipboardOnMainThread(String verificationCode) {
        ClipboardUtils.copyToClipboard(mContext, verificationCode);
        Toast.makeText(mContext, "当前验证码：" + verificationCode, Toast.LENGTH_LONG).show();
    }

}
