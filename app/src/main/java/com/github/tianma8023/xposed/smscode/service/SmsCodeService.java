package com.github.tianma8023.xposed.smscode.service;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.crossbowffs.remotepreferences.RemotePreferences;
import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.github.tianma8023.xposed.smscode.R;
import com.github.tianma8023.xposed.smscode.constant.NotificationConst;
import com.github.tianma8023.xposed.smscode.constant.PrefConst;
import com.github.tianma8023.xposed.smscode.entity.SmsMessageData;
import com.github.tianma8023.xposed.smscode.service.accessibility.SmsCodeAutoInputService;
import com.github.tianma8023.xposed.smscode.utils.AccessibilityUtils;
import com.github.tianma8023.xposed.smscode.utils.ClipboardUtils;
import com.github.tianma8023.xposed.smscode.utils.RemotePreferencesUtils;
import com.github.tianma8023.xposed.smscode.utils.SPUtils;
import com.github.tianma8023.xposed.smscode.utils.ShellUtils;
import com.github.tianma8023.xposed.smscode.utils.StringUtils;
import com.github.tianma8023.xposed.smscode.utils.VerificationUtils;
import com.github.tianma8023.xposed.smscode.utils.XLog;

import java.util.concurrent.TimeUnit;

/**
 * 处理验证码的Service
 */
public class SmsCodeService extends IntentService {

    private static final String SERVICE_NAME = "SmsCodeService";

    private static final int NOTIFY_ID_FOREGROUND_SVC = 0xff;

    public static final String EXTRA_KEY_SMS_INTENT = "key_sms_intent";

    private static final int MSG_COPY_TO_CLIPBOARD = 0xff;
    private static final int MSG_MARK_AS_READ = 0xfe;

    private RemotePreferences mPreferences;

    private boolean mAutoInputEnabled;
    private boolean mIsAutoInputRootMode;
    private String mFocusMode;

    public SmsCodeService() {
        this(SERVICE_NAME);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SmsCodeService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPreferences = RemotePreferencesUtils.getDefaultRemotePreferences(this.getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Show a notification for the foreground service.
            Notification notification = new NotificationCompat.Builder(this, NotificationConst.CHANNEL_ID_FOREGROUND_SERVICE)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification))
                    .setWhen(System.currentTimeMillis())
                    .setContentText(getString(R.string.sms_code_notification_title))
                    .setAutoCancel(true)
                    .setColor(getColor(R.color.ic_launcher_background))
                    .build();
            startForeground(NOTIFY_ID_FOREGROUND_SVC, notification);
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            Intent smsIntent = intent.getParcelableExtra(EXTRA_KEY_SMS_INTENT);
            doWork(smsIntent);
        }
    }

    private void doWork(Intent mSmsIntent) {
        if (!SPUtils.isEnabled(mPreferences)) {
            XLog.i("SmsCode disabled, exiting");
            return;
        }

        SmsMessageData smsMessageData = SmsMessageData.fromIntent(mSmsIntent);

        String sender = smsMessageData.getSender();
        String msgBody = smsMessageData.getBody();
        XLog.i("Received a new SMS message");
        if (BuildConfig.DEBUG) {
            XLog.i("Sender: %s", sender);
            XLog.i("Body: %s", msgBody);
        } else {
            XLog.i("Sender: %s", StringUtils.escape(sender));
            XLog.i("Body: %s", StringUtils.escape(msgBody));
        }

        if (TextUtils.isEmpty(msgBody))
            return;
        String verificationCode = VerificationUtils.parseVerificationCodeIfExists(this, msgBody);

        if (TextUtils.isEmpty(verificationCode)) { // Not verification code msg.
            return;
        }

        boolean verboseLog = SPUtils.isVerboseLogMode(mPreferences);
        if (verboseLog) {
            XLog.setLogLevel(Log.VERBOSE);
        } else {
            XLog.setLogLevel(BuildConfig.LOG_LEVEL);
        }

        mAutoInputEnabled = SPUtils.autoInputCodeEnabled(mPreferences);
        XLog.d("AutoInputEnabled: " + mAutoInputEnabled);
        if (mAutoInputEnabled) {
            mFocusMode = SPUtils.getFocusMode(mPreferences);
            mIsAutoInputRootMode = PrefConst.AUTO_INPUT_MODE_ROOT.
                    equals(SPUtils.getAutoInputMode(mPreferences));

            XLog.d("FocusMode: %s", mFocusMode);
            XLog.d("AutoInputRootMode: " + mIsAutoInputRootMode);
            if (mIsAutoInputRootMode && PrefConst.FOCUS_MODE_AUTO.equals(mFocusMode)) {
                // Root mode + auto-focus mode
                String accessSvcName = AccessibilityUtils.getServiceName(SmsCodeAutoInputService.class);
                // 先尝试用无Root的方式启动无障碍服务
                boolean enabled = AccessibilityUtils.enableAccessibilityService(this, accessSvcName);
                if (!enabled) {
                    // 不成功则用root的方式启动
                    enabled = ShellUtils.enableAccessibilityService(accessSvcName);
                }
                XLog.d("Accessibility enabled " + (enabled ? "true" : "false"));
                if (enabled) { // waiting for AutoInputService working on.
                    sleep(1);
                }
            }
        }

        XLog.i("Verification code: %s", verificationCode);
        Message copyMsg = new Message();
        copyMsg.obj = verificationCode;
        copyMsg.what = MSG_COPY_TO_CLIPBOARD;
        innerHandler.sendMessage(copyMsg);

        // mark sms as read or not.
        if (SPUtils.markAsReadEnabled(mPreferences)) {
            Message markMsg = new Message();
            markMsg.obj = smsMessageData;
            markMsg.what = MSG_MARK_AS_READ;
            innerHandler.sendMessageDelayed(markMsg, 8000);
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
                    break;
            }
        }
    };

    /**
     * 在主线程上执行copy操作
     */
    private void copyToClipboardOnMainThread(String verificationCode) {
        ClipboardUtils.copyToClipboard(this, verificationCode);
        if (SPUtils.shouldShowToast(mPreferences)) {
            String text = this.getString(R.string.cur_verification_code, verificationCode);
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }

        if (mAutoInputEnabled) {
            if (mIsAutoInputRootMode && PrefConst.FOCUS_MODE_MANUAL.equals(mFocusMode)) {
                // focus mode: manual focus
                // input mode: root mode
                boolean success = ShellUtils.inputText(verificationCode);
                if (success) {
                    XLog.i("Auto input succeed");
                    if (SPUtils.shouldClearClipboard(mPreferences)) {
                        ClipboardUtils.clearClipboard(this);
                    }
                }
            } else {
                // start auto input
                Intent intent = new Intent(SmsCodeAutoInputService.ACTION_START_AUTO_INPUT);
                intent.putExtra(SmsCodeAutoInputService.EXTRA_KEY_SMS_CODE, verificationCode);
                sendBroadcast(intent);
            }
        }
    }

    private void markSmsAsRead(String sender, String body) {
        Cursor cursor = null;
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                XLog.e("Don't have permission to read/write sms");
                return;
            }
            String[] projection = new String[]{
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.READ,
                    Telephony.Sms.DATE
            };
            // 查看最近5条短信
            String sortOrder = Telephony.Sms.DATE + " desc limit 5";
            Uri uri = Telephony.Sms.Inbox.CONTENT_URI;
            cursor = this.getContentResolver().query(uri, projection, null, null, sortOrder);
            if (cursor == null)
                return;
            while (cursor.moveToNext()) {
                String curAddress = cursor.getString(cursor.getColumnIndex("address"));
                int curRead = cursor.getInt(cursor.getColumnIndex("read"));
                String curBody = cursor.getString(cursor.getColumnIndex("body"));
                if (curAddress.equals(sender) && curRead == 0 && curBody.startsWith(body)) {
                    String smsMessageId = cursor.getString(cursor.getColumnIndex("_id"));
                    String where = Telephony.Sms._ID + " = ?";
                    String[] selectionArgs = new String[]{smsMessageId};
                    ContentValues values = new ContentValues();
                    values.put(Telephony.Sms.READ, true);
                    int rows = this.getContentResolver().update(uri, values, where, selectionArgs);
                    if (rows > 0) {
                        XLog.i("Mark as read succeed");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            XLog.e("Mark as read failed: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
