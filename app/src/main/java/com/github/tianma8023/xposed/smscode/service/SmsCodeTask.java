package com.github.tianma8023.xposed.smscode.service;

import android.Manifest;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Telephony;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.crossbowffs.remotepreferences.RemotePreferences;
import com.github.tianma8023.xposed.smscode.R;
import com.github.tianma8023.xposed.smscode.constant.IPrefConstants;
import com.github.tianma8023.xposed.smscode.entity.SmsMessageData;
import com.github.tianma8023.xposed.smscode.service.accessibility.SmsCodeAutoInputService;
import com.github.tianma8023.xposed.smscode.utils.AccessibilityUtils;
import com.github.tianma8023.xposed.smscode.utils.ClipboardUtils;
import com.github.tianma8023.xposed.smscode.utils.RemotePreferencesUtils;
import com.github.tianma8023.xposed.smscode.utils.ShellUtils;
import com.github.tianma8023.xposed.smscode.utils.StringUtils;
import com.github.tianma8023.xposed.smscode.utils.VerificationUtils;
import com.github.tianma8023.xposed.smscode.utils.XLog;

import java.util.concurrent.TimeUnit;

import static com.github.tianma8023.xposed.smscode.utils.RemotePreferencesUtils.getBooleanPref;

/**
 * 短信验证码相关的Task
 */
public class SmsCodeTask implements Runnable {

    private Service mService;
    private RemotePreferences mPreferences;
    private Intent mSmsIntent;

    private static final int MSG_COPY_TO_CLIPBOARD = 0xff;
    private static final int MSG_MARK_AS_READ = 0xfe;

    public SmsCodeTask(Service service, Intent smsIntent) {
        mService = service;
        mSmsIntent = smsIntent;
        mPreferences = RemotePreferencesUtils.getDefaultRemotePreferences(mService);
    }

    @Override
    public void run() {
        doWork();
    }

    private void doWork() {
        if (!getBooleanPref(mPreferences, IPrefConstants.KEY_ENABLE, IPrefConstants.KEY_ENABLE_DEFAULT)) {
            XLog.i("SmsCode disabled, exiting");
            return;
        }

        SmsMessageData smsMessageData = SmsMessageData.fromIntent(mSmsIntent);

        String sender = smsMessageData.getSender();
        String msgBody = smsMessageData.getBody();
        XLog.i("Received a new SMS message");
        XLog.i("Sender: %s", StringUtils.escape(sender));
        XLog.i("Body: %s", StringUtils.escape(msgBody));

        if (TextUtils.isEmpty(msgBody))
            return;
        String verificationCode = VerificationUtils.parseVerificationCodeIfExists(mService, msgBody);

        if (TextUtils.isEmpty(verificationCode)) { // Not verification code msg.
            return;
        }

        if (getBooleanPref(mPreferences, IPrefConstants.KEY_AUTO_INPUT_MODE_ROOT, IPrefConstants.KEY_AUTO_INPUT_MODE_ROOT_DEFAULT)) {
            // Root auto-input mode
            ShellUtils.enableAccessibilityService(AccessibilityUtils.getAccessibilityServiceName(SmsCodeAutoInputService.class));
        }

        XLog.i("Verification code: %s", verificationCode);
        Message copyMsg = new Message();
        copyMsg.obj = verificationCode;
        copyMsg.what = MSG_COPY_TO_CLIPBOARD;
        innerHandler.sendMessage(copyMsg);

        // mark sms as read or not.
        if (getBooleanPref(mPreferences, IPrefConstants.KEY_MARK_AS_READ, IPrefConstants.KEY_MARK_AS_READ_DEFAULT)) {
            try {
                TimeUnit.SECONDS.sleep(8);
                Message markMsg = new Message();
                markMsg.obj = smsMessageData;
                markMsg.what = MSG_MARK_AS_READ;
                innerHandler.sendMessage(markMsg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Handler innerHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_COPY_TO_CLIPBOARD:
                    copyToClipboardOnMainThread((String) msg.obj);
                    break;
                case MSG_MARK_AS_READ:
                    SmsMessageData smsMessageData = (SmsMessageData) msg.obj;
                    String sender = smsMessageData.getSender();
                    String body = smsMessageData.getBody();
                    markSmsAsRead(sender, body);
                    mService.stopSelf(); // 关闭后台Service
                    break;
            }
        }
    };

    /**
     * 在主线程上执行copy操作
     */
    private void copyToClipboardOnMainThread(String verificationCode) {
        ClipboardUtils.copyToClipboard(mService, verificationCode);
        if (getBooleanPref(mPreferences, IPrefConstants.KEY_SHOW_TOAST, IPrefConstants.KEY_SHOW_TOAST_DEFAULT)) {
            String text = mService.getString(R.string.cur_verification_code, verificationCode);
            Toast.makeText(mService, text, Toast.LENGTH_LONG).show();
        }
    }

    private void markSmsAsRead(String sender, String body) {
        if (ContextCompat.checkSelfPermission(mService, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            XLog.e("Don't have permission read sms");
            return;
        }
        Uri uri = Telephony.Sms.Inbox.CONTENT_URI;
        Cursor cursor = mService.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null)
            return;
        try {
            while (cursor.moveToNext()) {
                if ((cursor.getString(cursor.getColumnIndex("address")).equals(sender))
                        && (cursor.getInt(cursor.getColumnIndex("read")) == 0)
                        && cursor.getString(cursor.getColumnIndex("body")).startsWith(body)) {
                    String smsMessageId = cursor.getString(cursor.getColumnIndex("_id"));
                    ContentValues values = new ContentValues();
                    values.put("read", true);
                    mService.getContentResolver().update(uri, values, "_id = ?", new String[]{smsMessageId});
                }
            }
            XLog.i("Mark as read succeed");
        } catch (Exception e) {
            XLog.e("Mark as read failed: ", e);
        } finally {
            cursor.close();
        }
    }
}
