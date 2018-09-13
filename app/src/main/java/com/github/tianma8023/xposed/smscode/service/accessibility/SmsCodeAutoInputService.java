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
import com.github.tianma8023.xposed.smscode.constant.PrefConst;
import com.github.tianma8023.xposed.smscode.utils.AccessibilityUtils;
import com.github.tianma8023.xposed.smscode.utils.ClipboardUtils;
import com.github.tianma8023.xposed.smscode.utils.RemotePreferencesUtils;
import com.github.tianma8023.xposed.smscode.utils.SPUtils;
import com.github.tianma8023.xposed.smscode.utils.ShellUtils;
import com.github.tianma8023.xposed.smscode.utils.VerificationUtils;
import com.github.tianma8023.xposed.smscode.utils.XLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An accessibility service that can input SMS code automatically.
 */
public class SmsCodeAutoInputService extends BaseAccessibilityService {

    private RemotePreferences mPreferences;

    public static final String ACTION_START_AUTO_INPUT = "action_start_auto_input";
    public static final String ACTION_STOP_AUTO_INPUT_SERVICE = "action_stop_auto_input_service";

    public static final String EXTRA_KEY_SMS_CODE = "extra_key_sms_code";

    private static final int AUTO_INPUT_MAX_TRY_TIMES = 3;

    private class AutoInputControllerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            XLog.d("AutoInputControllerReceiver action=%s", action);
            if (ACTION_START_AUTO_INPUT.equals(action)) {
                String smsCode = intent.getStringExtra(EXTRA_KEY_SMS_CODE);
                autoInputSmsCode(smsCode);
            } else if (ACTION_STOP_AUTO_INPUT_SERVICE.equals(action)) {
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

    private AutoInputControllerReceiver mControllerReceiver;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        init();
    }

    private void init() {
        initPreferences();

        if (mControllerReceiver == null) {
            mControllerReceiver = new AutoInputControllerReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_START_AUTO_INPUT);
            intentFilter.addAction(ACTION_STOP_AUTO_INPUT_SERVICE);
            registerReceiver(mControllerReceiver, intentFilter);
        }
    }

    private void initPreferences() {
        if (mPreferences == null) {
            mPreferences = RemotePreferencesUtils.getDefaultRemotePreferences(this.getApplicationContext());
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
        boolean success = false;
        for (int i = 0; i < AUTO_INPUT_MAX_TRY_TIMES; i++) {
            XLog.d("try times %d", i+1);
            success = tryToAutoInputSMSCode(smsCode);
            if (success) {
                break;
            }
            sleep(100);
        }

        if (success) {
            XLog.i("Auto input succeed");
            if (SPUtils.shouldClearClipboard(mPreferences)) {
                // clear clipboard
                ClipboardUtils.clearClipboard(this);
            }
        }

        if (SPUtils.isAutoInputRootMode(mPreferences)) {
            Intent stopAutoInput = new Intent();
            stopAutoInput.setAction(ACTION_STOP_AUTO_INPUT_SERVICE);
            sendBroadcast(stopAutoInput);
        }
    }

    /**
     * 尝试自动输入短信验证码
     * @param smsCode SMS code
     * @return 成功输入则返回true，否则返回false
     */
    private boolean tryToAutoInputSMSCode(String smsCode) {
        String focusMode = SPUtils.getFocusMode(mPreferences);
        if (PrefConst.FOCUS_MODE_AUTO.equals(focusMode)) {
            // focus mode: auto focus
            return tryToAutoInputByAutoFocus(smsCode);
        } else {
            // focus mode: manual focus
            return tryToAutoInputByManualFocus(smsCode);
        }
    }

    /**
     * 手动对焦下的尝试自动输入
     * @param smsCode SMS code
     * @return 成功输入则返回true，否则返回false
     */
    private boolean tryToAutoInputByManualFocus(String smsCode) {
        AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
        AccessibilityNodeInfo focusedNodeInfo = rootNodeInfo.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (focusedNodeInfo != null && focusedNodeInfo.isEditable()) {
            inputText(focusedNodeInfo, smsCode);
            return true;
        }
        return false;
    }

    /**
     * 自动对焦下的尝试自动输入
     * @param smsCode SMS code
     * @return 成功输入则返回true，否则返回false
     */
    private boolean tryToAutoInputByAutoFocus(String smsCode) {
        AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
        if (rootNodeInfo == null) {
            XLog.d("rootNodeInfo is null");
            return false;
        }
        try {
            List<AccessibilityNodeInfo> editTextNodes = new ArrayList<>();
            traverse(rootNodeInfo, editTextNodes);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return autoInputSinceOreo(editTextNodes, smsCode);
            } else {
                return autoInputBeforeOreo(editTextNodes, smsCode);
            }
        } catch (Exception e) {
            XLog.e("error occurs in traverse()", e);
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean autoInputSinceOreo(List<AccessibilityNodeInfo> editTextNodes, String smsCode) {
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
                    return true;
                }
            }
        }

        if (editTextNodes.size() == 1) { // 只有一个EditText节点
            XLog.d("Have 1 EditText node");
            AccessibilityNodeInfo smsCodeNode = editTextNodes.get(0);
            inputText(smsCodeNode, smsCode);
            return true;
        } else if (editTextNodes.size() == 2) { // 有两个EditText (一个是电话号码,一个是验证码输入框)
            XLog.d("Have 2 EditText nodes");
            AccessibilityNodeInfo phoneNumberNode = editTextNodes.get(0);
            AccessibilityNodeInfo smsCodeNode = editTextNodes.get(1);
            CharSequence pnHintSequence = phoneNumberNode.getHintText();
            if (!TextUtils.isEmpty(pnHintSequence)) {
                if (VerificationUtils.containsPhoneNumberKeywords(pnHintSequence.toString())) {
                    inputText(smsCodeNode, smsCode);
                    return true;
                }
            }
            CharSequence pnTextSequence = phoneNumberNode.getText();
            if (!TextUtils.isEmpty(pnTextSequence)) {
                if (VerificationUtils.isPossiblePhoneNumber(pnTextSequence.toString())) {
                    inputText(smsCodeNode, smsCode);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean autoInputBeforeOreo(List<AccessibilityNodeInfo> editTextNodes, String smsCode) {
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
                    return true;
                }
            }
        }

        if (editTextNodes.size() == 1) { // 只有一个EditText节点
            AccessibilityNodeInfo smsCodeNode = editTextNodes.get(0);
            inputText(smsCodeNode, smsCode);
            return true;
        } else if (editTextNodes.size() == 2) { // 有两个EditText (一个是电话号码,一个是验证码输入框)
            AccessibilityNodeInfo phoneNumberNode = editTextNodes.get(0);
            AccessibilityNodeInfo smsCodeNode = editTextNodes.get(1);
            CharSequence pnTextSequence = phoneNumberNode.getText();
            if (!TextUtils.isEmpty(pnTextSequence)) {
                if (VerificationUtils.isPossiblePhoneNumber(pnTextSequence.toString())) {
                    inputText(smsCodeNode, smsCode);
                    return true;
                }
            }
        }
        return false;
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

    private void sleep(int milliSeconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliSeconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
