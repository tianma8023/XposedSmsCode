package com.github.tianma8023.xposed.smscode.xp.hook.code;

public class ParseResult {

    private boolean blockSms;

    public ParseResult() {

    }

    public boolean isBlockSms() {
        return blockSms;
    }

    public void setBlockSms(boolean blockSms) {
        this.blockSms = blockSms;
    }
}
