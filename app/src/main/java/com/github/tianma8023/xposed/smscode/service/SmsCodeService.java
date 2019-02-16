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
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.Telephony;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.crossbowffs.remotepreferences.RemotePreferences;
import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.github.tianma8023.xposed.smscode.R;
import com.github.tianma8023.xposed.smscode.aidl.ISmsMsgListener;
import com.github.tianma8023.xposed.smscode.aidl.ISmsMsgManager;
import com.github.tianma8023.xposed.smscode.aidl.SmsMsg;
import com.github.tianma8023.xposed.smscode.constant.NotificationConst;
import com.github.tianma8023.xposed.smscode.constant.PrefConst;
import com.github.tianma8023.xposed.smscode.db.DBManager;
import com.github.tianma8023.xposed.smscode.service.accessibility.SmsCodeAutoInputService;
import com.github.tianma8023.xposed.smscode.utils.AccessibilityUtils;
import com.github.tianma8023.xposed.smscode.utils.ClipboardUtils;
import com.github.tianma8023.xposed.smscode.utils.RemotePreferencesUtils;
import com.github.tianma8023.xposed.smscode.utils.SPUtils;
import com.github.tianma8023.xposed.smscode.utils.ShellUtils;
import com.github.tianma8023.xposed.smscode.utils.SmsCodeUtils;
import com.github.tianma8023.xposed.smscode.utils.StringUtils;
import com.github.tianma8023.xposed.smscode.utils.XLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 处理验证码的Service
 */
public class SmsCodeService extends IntentService {

    private static final String SERVICE_NAME = "SmsCodeService";

    private static final int NOTIFY_ID_FOREGROUND_SVC = 0xff;

    public static final String EXTRA_KEY_SMS_INTENT = "key_sms_intent";

    private static final int MSG_SMSCODE_EXTRACTED = 0xff;
    private static final int MSG_MARK_AS_READ = 0xfe;
    private static final int MSG_DELETE_SMS = 0xfd;

    private static final int OP_DELETE = 0;
    private static final int OP_MARK_AS_READ = 1;
    @IntDef({OP_DELETE, OP_MARK_AS_READ})
    @interface SmsOp {}

    private RemotePreferences mPreferences;

    private boolean mAutoInputEnabled;
    private boolean mIsAutoInputRootMode;
    private String mFocusMode;

    private RemoteCallbackList<ISmsMsgListener> mListenerList = new RemoteCallbackList<>();

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

    private Binder mBinder = new ISmsMsgManager.Stub() {
        @Override
        public void registerListener(ISmsMsgListener listener) throws RemoteException {
            mListenerList.register(listener);
        }

        @Override
        public void unregisterListener(ISmsMsgListener listener) throws RemoteException {
            mListenerList.unregister(listener);
        }
    };

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            Intent smsIntent = intent.getParcelableExtra(EXTRA_KEY_SMS_INTENT);
            doWork(smsIntent);
        }
    }

    /**
     *
     * @param smsIntent intent that contains SMS message data.
     */
    private void doWork(Intent smsIntent) {
        if (!SPUtils.isEnabled(mPreferences)) {
            XLog.i("SmsCode disabled, exiting");
            return;
        }

        SmsMsg smsMsg = SmsMsg.fromIntent(smsIntent);

        String sender = smsMsg.getSender();
        String msgBody = smsMsg.getBody();
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
        String smsCode = SmsCodeUtils.parseSmsCodeIfExists(this, msgBody);

        if (TextUtils.isEmpty(smsCode)) { // Not verification code msg.
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

        XLog.i("Verification code: %s", smsCode);

        Message copyMsg = new Message();
        copyMsg.obj = smsCode;
        copyMsg.what = MSG_SMSCODE_EXTRACTED;
        innerHandler.sendMessage(copyMsg);

        if (SPUtils.deleteSmsEnabled(mPreferences)) {
            XLog.d("Delete SMS enabled");
            // delete sms
            Message deleteMsg = new Message();
            deleteMsg.obj = smsMsg;
            deleteMsg.what = MSG_DELETE_SMS;
            innerHandler.sendMessageDelayed(deleteMsg, 6000);
        } else {
            // mark sms as read or not.
            if (SPUtils.markAsReadEnabled(mPreferences)) {
                XLog.d("Mark SMS as read enabled");
                Message markMsg = new Message();
                markMsg.obj = smsMsg;
                markMsg.what = MSG_MARK_AS_READ;
                innerHandler.sendMessageDelayed(markMsg, 6000);
            }
        }

        if (SPUtils.recordSmsCodeEnabled(mPreferences)) {
            smsMsg.setCompany(SmsCodeUtils.parseCompany(msgBody));
            smsMsg.setSmsCode(smsCode);
            smsMsg.setDate(System.currentTimeMillis());

            recordSmsMsg(smsMsg);
        }

        onNewSmsMsgParsed(smsMsg);
    }

    private void recordSmsMsg(SmsMsg smsMsg) {
        try {
            DBManager dm = DBManager.get(this);
            dm.addSmsMsg(smsMsg);
            XLog.d("add SMS message record succeed");

            List<SmsMsg> smsMsgList = dm.queryAllSmsMsg();
            if (smsMsgList.size() > PrefConst.MAX_SMS_RECORDS_COUNT_DEFAULT) {
                List<SmsMsg> outdatedMsgList = new ArrayList<>();
                for (int i = PrefConst.MAX_SMS_RECORDS_COUNT_DEFAULT; i < smsMsgList.size(); i++) {
                    outdatedMsgList.add(smsMsgList.get(i));
                }
                dm.removeSmsMsgList(outdatedMsgList);
                XLog.d("Remove outdated SMS message records succeed");
            }
        } catch (Exception e) {
            XLog.e("add SMS message record failed", e);
        }
    }

    private Handler innerHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SMSCODE_EXTRACTED:
                    onSmsCodeExtracted((String) msg.obj);
                    break;
                case MSG_MARK_AS_READ: {
                    SmsMsg smsMsg = (SmsMsg) msg.obj;
                    String sender = smsMsg.getSender();
                    String body = smsMsg.getBody();
                    markSmsAsRead(sender, body);
                    break;
                }
                case MSG_DELETE_SMS: {
                    SmsMsg smsMsg = (SmsMsg) msg.obj;
                    String sender = smsMsg.getSender();
                    String body = smsMsg.getBody();
                    deleteSms(sender, body);
                    break;
                }
            }
        }
    };

    private void onSmsCodeExtracted(final String smsCode) {
        boolean copyToClipboardEnabled = SPUtils.copyToClipboardEnabled(mPreferences);
        if (copyToClipboardEnabled) {
            ClipboardUtils.copyToClipboard(this, smsCode);
        }

        if (SPUtils.shouldShowToast(mPreferences)) {
            String text = this.getString(R.string.cur_verification_code, smsCode);
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }

        if (mAutoInputEnabled) {
            if (mIsAutoInputRootMode && PrefConst.FOCUS_MODE_MANUAL.equals(mFocusMode)) {
                // focus mode: manual focus
                // input mode: root mode
                boolean success = ShellUtils.inputText(smsCode);
                if (success) {
                    XLog.i("Auto input succeed");
                    if (copyToClipboardEnabled &&
                            SPUtils.shouldClearClipboard(mPreferences)) {
                        ClipboardUtils.clearClipboard(this);
                    }
                }
            } else {
                // start auto input
                Intent intent = new Intent(SmsCodeAutoInputService.ACTION_START_AUTO_INPUT);
                intent.putExtra(SmsCodeAutoInputService.EXTRA_KEY_SMS_CODE, smsCode);
                sendBroadcast(intent);
            }
        }
    }

    private void markSmsAsRead(String sender, String body) {
        XLog.d("Marking SMS...");
        boolean result = operateSms(sender, body, OP_MARK_AS_READ);
        if (result) {
            XLog.i("Mark SMS as read succeed");
        } else {
            XLog.i("Mark SMS as read failed");
        }
    }

    private void deleteSms(String sender, String body) {
        XLog.d("Deleting SMS...");
        boolean result = operateSms(sender, body, OP_DELETE);
        if (result) {
            XLog.i("Delete SMS succeed");
        } else {
            XLog.i("Delete SMS failed");
        }
    }

    /**
     * Handle sms according to its operation
     */
    private boolean operateSms(String sender, String body, @SmsOp int smsOp) {
        Cursor cursor = null;
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                XLog.e("Don't have permission to read/write sms");
                return false;
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
            Uri uri = Telephony.Sms.CONTENT_URI;
            cursor = this.getContentResolver().query(uri, projection, null, null, sortOrder);
            if (cursor == null) {
                XLog.i("Cursor is null");
                return false;
            }
            while (cursor.moveToNext()) {
                String curAddress = cursor.getString(cursor.getColumnIndex("address"));
                int curRead = cursor.getInt(cursor.getColumnIndex("read"));
                String curBody = cursor.getString(cursor.getColumnIndex("body"));
                if (curAddress.equals(sender) && curRead == 0 && curBody.startsWith(body)) {
                    String smsMessageId = cursor.getString(cursor.getColumnIndex("_id"));
                    String where = Telephony.Sms._ID + " = ?";
                    String[] selectionArgs = new String[]{smsMessageId};
                    if (smsOp == OP_DELETE) {
                        int rows = getContentResolver().delete(uri, where, selectionArgs);
                        if (rows > 0) {
                            return true;
                        }
                    } else if (smsOp == OP_MARK_AS_READ) {
                        ContentValues values = new ContentValues();
                        values.put(Telephony.Sms.READ, true);
                        int rows = this.getContentResolver().update(uri, values, where, selectionArgs);
                        if (rows > 0) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            XLog.e("Operate SMS failed: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    private void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    private void broadcastSmsBlock(boolean blockSmsBroadcast) {
//        Intent intent = new Intent(SmsHandlerHook.ACTION_HANDLE_SMS);
//        intent.putExtra(SmsHandlerHook.EXTRA_BLOCK_SMS_BROADCAST, blockSmsBroadcast);
//        sendBroadcast(intent);
//    }

    private void onNewSmsMsgParsed(SmsMsg smsMsg){
        XLog.d("onNewSmsMsgParsed: %s", smsMsg.getBody());
        XLog.d("new SmsMsg parsed, notify all listeners");

        final int N = mListenerList.beginBroadcast();
        for (int i = 0; i < N; i++) {
            ISmsMsgListener listener = mListenerList.getBroadcastItem(i);
            if (listener != null) {
                try {
                    listener.onNewSmsMsgParsed(smsMsg);
                } catch (RemoteException e) {
                    XLog.e("error occurs in ISmsMsgListener#onNewSmsMsgParsed()");
                }
            }
        }
        mListenerList.finishBroadcast();
    }
}
