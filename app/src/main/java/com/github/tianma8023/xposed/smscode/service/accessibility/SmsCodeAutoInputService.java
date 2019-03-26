//package com.github.tianma8023.xposed.smscode.service.accessibility;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Build;
//import android.os.Handler;
//import android.os.Looper;
//import android.support.annotation.RequiresApi;
//import android.text.TextUtils;
//import android.view.accessibility.AccessibilityEvent;
//import android.view.accessibility.AccessibilityNodeInfo;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import com.crossbowffs.remotepreferences.RemotePreferences;
//import com.github.tianma8023.xposed.smscode.R;
//import com.github.tianma8023.xposed.smscode.constant.PrefConst;
//import com.github.tianma8023.xposed.smscode.utils.AccessibilityUtils;
//import com.github.tianma8023.xposed.smscode.utils.ClipboardUtils;
//import com.github.tianma8023.xposed.smscode.utils.RemotePreferencesUtils;
//import com.github.tianma8023.xposed.smscode.utils.SPUtils;
//import com.github.tianma8023.xposed.smscode.utils.ShellUtils;
//import com.github.tianma8023.xposed.smscode.utils.SmsCodeUtils;
//import com.github.tianma8023.xposed.smscode.utils.XLog;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
///**
// * An accessibility service that can input SMS code automatically.
// */
//public class SmsCodeAutoInputService extends BaseAccessibilityService {
//
//    private RemotePreferences mPreferences;
//
//    public static final String ACTION_START_AUTO_INPUT = "action_start_auto_input";
//    public static final String ACTION_STOP_AUTO_INPUT_SERVICE = "action_stop_auto_input_service";
//
//    public static final String EXTRA_KEY_SMS_CODE = "extra_key_sms_code";
//
//    private static final int AUTO_INPUT_MAX_TRY_TIMES = 3;
//
//    private class AutoInputControllerReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            XLog.d("AutoInputControllerReceiver action=%s", action);
//            if (ACTION_START_AUTO_INPUT.equals(action)) {
//                String smsCode = intent.getStringExtra(EXTRA_KEY_SMS_CODE);
//                ExecutorService threadPool = Executors.newSingleThreadExecutor();
//                threadPool.execute(new AutoInputTask(smsCode));
//            } else if (ACTION_STOP_AUTO_INPUT_SERVICE.equals(action)) {
//                String accessSvcName = AccessibilityUtils.getServiceName(SmsCodeAutoInputService.class);
//                // 先尝试用无Root的方式关闭无障碍服务
//                boolean disabled = AccessibilityUtils.disableAccessibilityService(context, accessSvcName);
//                if (!disabled) {
//                    // 不成功,则用root的方式关闭无障碍服务
//                    disabled = ShellUtils.disableAccessibilityService(accessSvcName);
//                }
//                XLog.d("disable service = " + (disabled ? "succeed" : "failed"));
//            }
//        }
//    }
//
//    private AutoInputControllerReceiver mControllerReceiver;
//    private Handler mInnerHandler;
//
//    @Override
//    protected void onServiceConnected() {
//        super.onServiceConnected();
//        init();
//    }
//
//    private void init() {
//        initPreferences();
//
//        if (mControllerReceiver == null) {
//            mControllerReceiver = new AutoInputControllerReceiver();
//            IntentFilter intentFilter = new IntentFilter();
//            intentFilter.addAction(ACTION_START_AUTO_INPUT);
//            intentFilter.addAction(ACTION_STOP_AUTO_INPUT_SERVICE);
//            registerReceiver(mControllerReceiver, intentFilter);
//        }
//
//        if (mInnerHandler == null) {
//            mInnerHandler = new Handler(Looper.getMainLooper());
//        }
//    }
//
//    private void initPreferences() {
//        if (mPreferences == null) {
//            mPreferences = RemotePreferencesUtils.getDefaultRemotePreferences(this.getApplicationContext());
//        }
//    }
//
//    @Override
//    public void onAccessibilityEvent(AccessibilityEvent event) {
//    }
//
//    @Override
//    public void onDestroy() {
//        if (mControllerReceiver != null) {
//            unregisterReceiver(mControllerReceiver);
//        }
//        super.onDestroy();
//    }
//
//    private void autoInputSmsCode(String smsCode) {
//        boolean success = tryToAutoInputSMSCode(smsCode);
//
//        if (success) {
//            XLog.i("Auto input succeed");
//            if (SPUtils.copyToClipboardEnabled(mPreferences)
//                    && SPUtils.shouldClearClipboard(mPreferences)) {
//                // clear clipboard
//                ClipboardUtils.clearClipboard(this);
//            }
//        } else {
//            XLog.i("Auto input failed");
//        }
//
//        String autoInputMode = SPUtils.getAutoInputMode(mPreferences);
//        if (PrefConst.AUTO_INPUT_MODE_ROOT.equals(autoInputMode)) {
//            Intent stopAutoInput = new Intent();
//            stopAutoInput.setAction(ACTION_STOP_AUTO_INPUT_SERVICE);
//            sendBroadcast(stopAutoInput);
//        }
//    }
//
//    /**
//     * 尝试自动输入短信验证码
//     * @param smsCode SMS code
//     * @return 成功输入则返回true，否则返回false
//     */
//    private boolean tryToAutoInputSMSCode(String smsCode) {
//        boolean success = false;
//        String focusMode = SPUtils.getFocusMode(mPreferences);
//        boolean isRootAutoInputMode =
//                PrefConst.AUTO_INPUT_MODE_ROOT.equals(SPUtils.getAutoInputMode(mPreferences));
//        if (PrefConst.FOCUS_MODE_AUTO.equals(focusMode)) {
//            // focus mode: auto focus
//            for (int i = 0; i < AUTO_INPUT_MAX_TRY_TIMES; i++) {
//                XLog.d("try times %d", i+1);
//                success = tryToAutoInputByAutoFocus(smsCode);
//                if (success) {
//                    break;
//                }
//                sleep(100);
//            }
//
//            if (!success && SPUtils.manualFocusIfFailedEnabled(mPreferences)) {
//                XLog.d("auto focus failed, transfer to manual focus");
//                final int secs = 3;
//                mInnerHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        String text = getString(R.string.auto_focus_failed_prompt, secs);
//                        Toast.makeText(SmsCodeAutoInputService.this, text, Toast.LENGTH_LONG).show();
//                    }
//                });
//                sleep(secs * 1000);
//                success = tryToAutoInputByManualFocus(smsCode, isRootAutoInputMode);
//            }
//        } else {
//            // focus mode: manual focus
//            for (int i = 0; i < AUTO_INPUT_MAX_TRY_TIMES; i++) {
//                XLog.d("try times %d", i+1);
//                success = tryToAutoInputByManualFocus(smsCode, isRootAutoInputMode);
//                if (success) {
//                    break;
//                }
//                sleep(100);
//            }
//        }
//        return success;
//    }
//
//    /**
//     * 手动对焦下的尝试自动输入
//     * @param smsCode SMS code
//     * @return 成功输入则返回true，否则返回false
//     */
//    private boolean tryToAutoInputByManualFocus(String smsCode, boolean isRootAutoInputMode) {
//        if (isRootAutoInputMode){
//            return ShellUtils.inputText(smsCode);
//        } else {
//            AccessibilityNodeInfo focusedNodeInfo = findFocusNodeInfo();
//            if (focusedNodeInfo != null && focusedNodeInfo.isEditable()) {
//                inputText(focusedNodeInfo, smsCode);
//                return true;
//            }
//            return false;
//        }
//    }
//
//    /**
//     * 获取当前输入焦点控件
//     * @return 当前输入焦点,获取失败返回null
//     */
//    private AccessibilityNodeInfo findFocusNodeInfo() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            return findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
//        } else {
//            AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
//            if (rootNodeInfo == null) {
//                XLog.d("rootNodeInfo is null");
//                return null;
//            }
//            return rootNodeInfo.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
//        }
//    }
//
//    /**
//     * 自动对焦下的尝试自动输入
//     * @param smsCode SMS code
//     * @return 成功输入则返回true，否则返回false
//     */
//    private boolean tryToAutoInputByAutoFocus(String smsCode) {
//        AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
//        if (rootNodeInfo == null) {
//            XLog.d("rootNodeInfo is null");
//            return false;
//        }
//        try {
//            List<AccessibilityNodeInfo> editTextNodes = new ArrayList<>();
//            traverse(rootNodeInfo, editTextNodes);
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                return autoInputSinceOreo(editTextNodes, smsCode);
//            } else {
//                return autoInputBeforeOreo(editTextNodes, smsCode);
//            }
//        } catch (Exception e) {
//            XLog.e("error occurs in traverse()", e);
//        }
//        return false;
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private boolean autoInputSinceOreo(List<AccessibilityNodeInfo> editTextNodes, String smsCode) {
//        // 判断有没有验证码输入框
//        // Android O 起可以通过getHintText获取EditText的hint内容
//        for (AccessibilityNodeInfo nodeInfo : editTextNodes) {
//            if (nodeInfo.isFocusable()) {
//                CharSequence hintSequence = nodeInfo.getHintText();
//                if (hintSequence == null) {
//                    continue;
//                }
//                String hint = hintSequence.toString();
//
//                boolean flag = SmsCodeUtils.containsCodeKeywords(getApplicationContext(), hint);
//                if (flag) {
//                    // 模拟输入
//                    inputText(nodeInfo, smsCode);
//                    XLog.d("SMS code EditText found!");
//                    return true;
//                }
//            }
//        }
//
//        // 但是在Android O 上WebView中的EditText，没办法通过 getHintText 获取hint内容
//        // 所以需要通过 getText 进一步判断
//        for (AccessibilityNodeInfo nodeInfo : editTextNodes) {
//            if (nodeInfo.isFocusable()) {
//                CharSequence text = nodeInfo.getText();
//                if (text == null) {
//                    continue;
//                }
//                String hintOrText = text.toString();
//
//                boolean flag = SmsCodeUtils.containsCodeKeywords(getApplicationContext(), hintOrText);
//                if (flag) {
//                    // 模拟输入
//                    inputText(nodeInfo, smsCode);
//                    XLog.d("SMS code EditText found!");
//                    return true;
//                }
//            }
//        }
//
//        if (editTextNodes.size() == 1) { // 只有一个EditText节点
//            XLog.d("Have 1 EditText node");
//            AccessibilityNodeInfo smsCodeNode = editTextNodes.get(0);
//            inputText(smsCodeNode, smsCode);
//            return true;
//        } else if (editTextNodes.size() == 2) { // 有两个EditText (一个是电话号码,一个是验证码输入框)
//            XLog.d("Have 2 EditText nodes");
//            AccessibilityNodeInfo phoneNumberNode = editTextNodes.get(0);
//            AccessibilityNodeInfo smsCodeNode = editTextNodes.get(1);
//            CharSequence pnHintSequence = phoneNumberNode.getHintText();
//            if (!TextUtils.isEmpty(pnHintSequence)) {
//                if (SmsCodeUtils.containsPhoneNumberKeywords(pnHintSequence.toString())) {
//                    inputText(smsCodeNode, smsCode);
//                    return true;
//                }
//            }
//            CharSequence pnTextSequence = phoneNumberNode.getText();
//            if (!TextUtils.isEmpty(pnTextSequence)) {
//                if (SmsCodeUtils.isPossiblePhoneNumber(pnTextSequence.toString())) {
//                    inputText(smsCodeNode, smsCode);
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    private boolean autoInputBeforeOreo(List<AccessibilityNodeInfo> editTextNodes, String smsCode) {
//        // 判断有没有验证码输入框
//        for (AccessibilityNodeInfo nodeInfo : editTextNodes) {
//            if (nodeInfo.isFocusable()) {
//                CharSequence text = nodeInfo.getText();
//                if (text == null) {
//                    continue;
//                }
//                String hintOrText = text.toString();
//
//                boolean flag = SmsCodeUtils.containsCodeKeywords(getApplicationContext(), hintOrText);
//                if (flag) {
//                    // 模拟输入
//                    inputText(nodeInfo, smsCode);
//                    XLog.d("SMS code EditText found!");
//                    return true;
//                }
//            }
//        }
//
//        if (editTextNodes.size() == 1) { // 只有一个EditText节点
//            XLog.d("Have 1 EditText node");
//            AccessibilityNodeInfo smsCodeNode = editTextNodes.get(0);
//            inputText(smsCodeNode, smsCode);
//            return true;
//        } else if (editTextNodes.size() == 2) { // 有两个EditText (一个是电话号码,一个是验证码输入框)
//            XLog.d("Have 2 EditText nodes");
//            AccessibilityNodeInfo phoneNumberNode = editTextNodes.get(0);
//            AccessibilityNodeInfo smsCodeNode = editTextNodes.get(1);
//            CharSequence pnTextSequence = phoneNumberNode.getText();
//            if (!TextUtils.isEmpty(pnTextSequence)) {
//                if (SmsCodeUtils.isPossiblePhoneNumber(pnTextSequence.toString())) {
//                    inputText(smsCodeNode, smsCode);
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    /**
//     * 遍历以当前 nodeInfo 为根节点的节点树，将EditText节点存储在editTextNodes中
//     *
//     * @param nodeInfo      current root node
//     * @param editTextNodes store EditText nodes.
//     */
//    private void traverse(AccessibilityNodeInfo nodeInfo,
//                          List<AccessibilityNodeInfo> editTextNodes) {
//        if (nodeInfo.getChildCount() == 0) { // 叶子节点
//            handleLeafNodeInfo(nodeInfo, editTextNodes);
//        } else {
//            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
//                AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
//                if (childNodeInfo != null) {
//                    traverse(childNodeInfo, editTextNodes);
//                }
//            }
//        }
//    }
//
//    /**
//     * 处理叶节点，返回是否找到该输入验证码的EditText
//     *
//     * @param nodeInfo      nodeInfo
//     * @param editTextNodes editTextNodes
//     */
//    private void handleLeafNodeInfo(AccessibilityNodeInfo nodeInfo,
//                                    List<AccessibilityNodeInfo> editTextNodes) {
//        try {
//            Class<?> clz = Class.forName(nodeInfo.getClassName().toString());
//            XLog.d("class=%s, text=%s",
//                    nodeInfo.getClassName().toString(),
//                    nodeInfo.getText());
//            if (EditText.class.isAssignableFrom(clz)) { // is EditText
//                editTextNodes.add(nodeInfo);
//            }
//        } catch (ClassNotFoundException e) {
//            // ignore
//        }
//    }
//
//    private void sleep(int milliSeconds) {
//        try {
//            TimeUnit.MILLISECONDS.sleep(milliSeconds);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private class AutoInputTask implements Runnable {
//
//        private String mSmsCode;
//
//        AutoInputTask(String smsCode) {
//            mSmsCode = smsCode;
//        }
//
//        @Override
//        public void run() {
//            Looper.prepare();
//            autoInputSmsCode(mSmsCode);
//            Looper.loop();
//        }
//    }
//}
