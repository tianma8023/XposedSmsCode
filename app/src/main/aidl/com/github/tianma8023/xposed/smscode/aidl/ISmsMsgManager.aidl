// ISmsMsgManager.aidl
package com.github.tianma8023.xposed.smscode.aidl;

// Declare any non-default types here with import statements
import com.github.tianma8023.xposed.smscode.aidl.SmsMsg;
import com.github.tianma8023.xposed.smscode.aidl.ISmsMsgListener;

interface ISmsMsgManager {

    void registerListener(ISmsMsgListener listener);

    void unregisterListener(ISmsMsgListener listener);
}
