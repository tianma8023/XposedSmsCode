package com.github.tianma8023.xposed.smscode.service.accessibility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;

import com.crossbowffs.remotepreferences.RemotePreferences;
import com.github.tianma8023.xposed.smscode.constant.IPrefConstants;
import com.github.tianma8023.xposed.smscode.utils.AccessibilityUtils;
import com.github.tianma8023.xposed.smscode.utils.RemotePreferencesUtils;
import com.github.tianma8023.xposed.smscode.utils.ShellUtils;
import com.github.tianma8023.xposed.smscode.utils.VerificationUtils;
import com.github.tianma8023.xposed.smscode.utils.XLog;

import java.util.ArrayList;
import java.util.List;

/**
 * An accessibility service that can input SMS code automatically.
 */
public class SmsCodeAutoInputService extends BaseAccessibilityService {

    private RemotePreferences mPreferences;

    public static final String ACTION_START_AUTO_INPUT = "action_start_auto_input";
    public static final String ACTION_STOP_AUTO_INPUT_SERVICE = "action_stop_auto_input_service";

    public static final String EXTRA_KEY_SMS_CODE = "extra_key_sms_code";

    private class AutoInputControllerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            XLog.d("AutoInputControllerReceiver action=%s", action);
            if (ACTION_START_AUTO_INPUT.equals(action)) {
                String smsCode = intent.getStringExtra(EXTRA_KEY_SMS_CODE);
                autoInputSmsCode(smsCode);
            } else if (ACTION_STOP_AUTO_INPUT_SERVICE.equals(action)) {
                if (RemotePreferencesUtils.getBooleanPref(mPreferences, IPrefConstants.KEY_AUTO_INPUT_MODE_ROOT, IPrefConstants.KEY_AUTO_INPUT_MODE_ROOT_DEFAULT)) {
                    String accessSvcName = AccessibilityUtils.getServiceName(SmsCodeAutoInputService.class);
                    // 先尝试用无Root的方式关闭无障碍服务
                    boolean disabled = AccessibilityUtils.disableAccessibilityService(context, accessSvcName);
                    if (!disabled) {
                        // 不成功,则用root的方式关闭无障碍服务
                        disabled = ShellUtils.disableAccessibilityService(accessSvcName);
                    }
                    XLog.d("disable service = " + (disabled ? "succeed" : "failed"));
                }
            }
        }
    }

    private AutoInputControllerReceiver mControllerReceiver;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        init();
    }

    private void init() {
        if (mPreferences == null) {
            mPreferences = RemotePreferencesUtils.getDefaultRemotePreferences(this.getApplicationContext());
        }

        if (mControllerReceiver == null) {
            mControllerReceiver = new AutoInputControllerReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_START_AUTO_INPUT);
            intentFilter.addAction(ACTION_STOP_AUTO_INPUT_SERVICE);
            registerReceiver(mControllerReceiver, intentFilter);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onDestroy() {
        if (mControllerReceiver != null) {
            unregisterReceiver(mControllerReceiver);
        }
        super.onDestroy();
    }

    private void autoInputSmsCode(String smsCode) {
        tryToAutoInputSMSCode(smsCode);

        Intent stopAutoInput = new Intent();
        stopAutoInput.setAction(ACTION_STOP_AUTO_INPUT_SERVICE);
        sendBroadcast(stopAutoInput);
    }

    private void tryToAutoInputSMSCode(String smsCode) {
        AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
        if (rootNodeInfo == null) {
            return;
        }
        try {
            List<AccessibilityNodeInfo> editTextNodes = new ArrayList<>();
            traverse(rootNodeInfo, editTextNodes);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                autoInputSinceOreo(editTextNodes, smsCode);
            } else {
                autoInputBeforeOreo(editTextNodes, smsCode);
            }
        } catch (Exception e) {
            XLog.e("error occurs in traverse()", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void autoInputSinceOreo(List<AccessibilityNodeInfo> editTextNodes, String smsCode) {
        // 判断有没有验证码输入框
        for (AccessibilityNodeInfo nodeInfo : editTextNodes) {
            if (nodeInfo.isFocusable()) {
                CharSequence hintSequence = nodeInfo.getHintText();
                if (hintSequence == null) {
                    continue;
                }
                String hint = hintSequence.toString();

                boolean flag = VerificationUtils.containsVerificationKeywords(getApplicationContext(), hint);
                if (flag) {
                    // 模拟输入
                    inputText(nodeInfo, smsCode);
                    XLog.d("SMS code EditText found!");
                    return;
                }
            }
        }

        if (editTextNodes.size() == 1) { // 只有一个EditText节点
            XLog.d("Have 1 EditText node");
            AccessibilityNodeInfo smsCodeNode = editTextNodes.get(0);
            inputText(smsCodeNode, smsCode);
        } else if (editTextNodes.size() == 2) { // 有两个EditText (一个是电话号码,一个是验证码输入框)
            XLog.d("Have 2 EditText nodes");
            AccessibilityNodeInfo phoneNumberNode = editTextNodes.get(0);
            AccessibilityNodeInfo smsCodeNode = editTextNodes.get(1);
            CharSequence pnHintSequence = phoneNumberNode.getHintText();
            if (!TextUtils.isEmpty(pnHintSequence)) {
                if (VerificationUtils.containsPhoneNumberKeywords(pnHintSequence.toString())) {
                    inputText(smsCodeNode, smsCode);
                    return;
                }
            }
            CharSequence pnTextSequence = phoneNumberNode.getText();
            if (!TextUtils.isEmpty(pnTextSequence)) {
                if (VerificationUtils.isPossiblePhoneNumber(pnTextSequence.toString())) {
                    inputText(smsCodeNode, smsCode);
                }
            }
        }
    }

    private void autoInputBeforeOreo(List<AccessibilityNodeInfo> editTextNodes, String smsCode) {
        // 判断有没有验证码输入框
        for (AccessibilityNodeInfo nodeInfo : editTextNodes) {
            if (nodeInfo.isFocusable()) {
                CharSequence text = nodeInfo.getText();
                if (text == null) {
                    continue;
                }
                String hintOrText = text.toString();

                boolean flag = VerificationUtils.containsVerificationKeywords(getApplicationContext(), hintOrText);
                if (flag) {
                    // 模拟输入
                    inputText(nodeInfo, smsCode);
                    XLog.d("SMS code EditText found!");
                    return;
                }
            }
        }

        if (editTextNodes.size() == 1) { // 只有一个EditText节点
            AccessibilityNodeInfo smsCodeNode = editTextNodes.get(0);
            inputText(smsCodeNode, smsCode);
        } else if (editTextNodes.size() == 2) { // 有两个EditText (一个是电话号码,一个是验证码输入框)
            AccessibilityNodeInfo phoneNumberNode = editTextNodes.get(0);
            AccessibilityNodeInfo smsCodeNode = editTextNodes.get(1);
            CharSequence pnTextSequence = phoneNumberNode.getText();
            if (!TextUtils.isEmpty(pnTextSequence)) {
                if (VerificationUtils.isPossiblePhoneNumber(pnTextSequence.toString())) {
                    inputText(smsCodeNode, smsCode);
                }
            }
        }
    }

    /**
     * 遍历以当前 nodeInfo 为根节点的节点树，将EditText节点存储在editTextNodes中
     *
     * @param nodeInfo      current root node
     * @param editTextNodes store EditText nodes.
     */
    private void traverse(AccessibilityNodeInfo nodeInfo,
                          List<AccessibilityNodeInfo> editTextNodes) {
        if (nodeInfo.getChildCount() == 0) { // 叶子节点
            handleLeafNodeInfo(nodeInfo, editTextNodes);
        } else {
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
                if (childNodeInfo != null) {
                    traverse(childNodeInfo, editTextNodes);
                }
            }
        }
    }

    /**
     * 处理叶节点，返回是否找到该输入验证码的EditText
     *
     * @param nodeInfo      nodeInfo
     * @param editTextNodes editTextNodes
     */
    private void handleLeafNodeInfo(AccessibilityNodeInfo nodeInfo,
                                    List<AccessibilityNodeInfo> editTextNodes) {
        try {
            Class<?> clz = Class.forName(nodeInfo.getClassName().toString());
            XLog.d("class=%s, text=%s",
                    nodeInfo.getClassName().toString(),
                    nodeInfo.getText());
            if (EditText.class.isAssignableFrom(clz)) { // is EditText
                editTextNodes.add(nodeInfo);
            }
        } catch (ClassNotFoundException e) {
            // ignore
        }
    }
}
