// ISmsMsgManager.aidl
package com.github.tianma8023.xposed.smscode.aidl;

import com.github.tianma8023.xposed.smscode.aidl.SmsMsg;
// Declare any non-default types here with import statements

interface ISmsMsgListener {

    void onNewSmsMsgParsed(in SmsMsg smsMsg);

}
