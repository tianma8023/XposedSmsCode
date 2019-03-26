package com.github.tianma8023.xposed.smscode.xp.hook;

import com.github.tianma8023.xposed.smscode.aidl.SmsMsg;

public class ParseResult {

    private SmsMsg mSmsMsg;

    private boolean blockSms;

    private boolean autoInput;

    public ParseResult() {

    }

    public SmsMsg getSmsMsg() {
        return mSmsMsg;
    }

    public void setSmsMsg(SmsMsg smsMsg) {
        mSmsMsg = smsMsg;
    }

    public boolean isBlockSms() {
        return blockSms;
    }

    public void setBlockSms(boolean blockSms) {
        this.blockSms = blockSms;
    }

    public boolean isAutoInput() {
        return autoInput;
    }

    public void setAutoInput(boolean autoInput) {
        this.autoInput = autoInput;
    }
}
