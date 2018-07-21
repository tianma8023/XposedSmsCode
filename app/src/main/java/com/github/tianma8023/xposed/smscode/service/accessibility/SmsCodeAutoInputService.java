package com.github.tianma8023.xposed.smscode.service.accessibility;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;

import com.github.tianma8023.xposed.smscode.utils.VerificationUtils;
import com.github.tianma8023.xposed.smscode.utils.XLog;

/**
 * An accessibility service that can input verification code automatically.
 */
public class SmsCodeAutoInputService extends BaseAccessibilityService {

    private ClipboardManager mClipboardManager;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        initClipboardManager();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    private void initClipboardManager() {
        if (mClipboardManager == null) {
            mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        }
        if (mClipboardManager != null) {
            mClipboardManager.addPrimaryClipChangedListener(mPrimaryClipChangedListener);
        }
    }

    private ClipboardManager.OnPrimaryClipChangedListener mPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onPrimaryClipChanged() {
            if (mClipboardManager.hasPrimaryClip()) {
                ClipData clipData = mClipboardManager.getPrimaryClip();
                if (clipData.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    ClipData.Item item = clipData.getItemAt(0);
                    onNewTextCopied(item.getText().toString());
                }
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void onNewTextCopied(String textCopied) {
//        if (VerificationUtils.maybeVerificationCode(textCopied)) {
            AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
            if (rootNodeInfo != null) {
                try {
                    traverse(rootNodeInfo, textCopied);
                } catch (Exception e) {
                    XLog.e("error occurs in traverse ", e);
                }
            }
//        }

//        boolean result = ShellUtils.disableAccessibilityService(
//                AccessibilityUtils.getAccessibilityServiceName(SmsCodeAutoInputService.class));
//        XLog.i("disable service = " + (result ? "succeed" : "failed"));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void traverse(AccessibilityNodeInfo nodeInfo, String textToPaste) {
        if (nodeInfo.getChildCount() == 0) {
            handleLeafNodeInfo(nodeInfo, textToPaste);
        } else {
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
                if (childNodeInfo != null) {
                    traverse(childNodeInfo, textToPaste);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleLeafNodeInfo(AccessibilityNodeInfo nodeInfo, String textToPaste) {
        try {
            Class<?> clz = Class.forName(nodeInfo.getClassName().toString());
            XLog.i("class=%s, text=%s, hint=%s, editable=%s, contentDescription=%s",
                    nodeInfo.getClassName().toString(),
                    nodeInfo.getText(),
                    nodeInfo.getHintText(),
                    String.valueOf(nodeInfo.isEditable()),
                    nodeInfo.getContentDescription());
            if (EditText.class.isAssignableFrom(clz)) { // is EditText
                if (nodeInfo.isFocusable()) {
                    CharSequence text = nodeInfo.getText();
                    if (text == null) {
                        return;
                    }
                    String hintOrText = text.toString();

                    boolean flag = VerificationUtils.containsVerificationKeywords(getApplicationContext(), hintOrText);
                    if (flag) {
                        // 模拟粘贴
                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            // ignore
        }
    }
}
