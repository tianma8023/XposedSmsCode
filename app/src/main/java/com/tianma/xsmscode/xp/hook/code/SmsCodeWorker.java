package com.tianma.xsmscode.xp.hook.code;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.github.tianma8023.xposed.smscode.R;
import com.tianma.xsmscode.common.constant.NotificationConst;
import com.tianma.xsmscode.common.constant.PrefConst;
import com.tianma.xsmscode.common.utils.ClipboardUtils;
import com.tianma.xsmscode.common.utils.SmsCodeUtils;
import com.tianma.xsmscode.common.utils.StringUtils;
import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.common.utils.XSPUtils;
import com.tianma.xsmscode.data.db.DBProvider;
import com.tianma.xsmscode.data.db.entity.AppInfo;
import com.tianma.xsmscode.data.db.entity.AppInfoDao;
import com.tianma.xsmscode.data.db.entity.SmsMsg;
import com.tianma.xsmscode.data.db.entity.SmsMsgDao;
import com.tianma.xsmscode.feature.store.EntityStoreManager;
import com.tianma.xsmscode.feature.store.EntityType;
import com.tianma.xsmscode.ui.record.CodeRecordRestoreManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.IntDef;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import de.robv.android.xposed.XSharedPreferences;

public class SmsCodeWorker {

    static final String ACTION_PRE_QUIT_QUEUE = BuildConfig.APPLICATION_ID + "action.PRE_QUIT_QUEUE";

    private static final int MSG_QUIT_QUEUE = 0xff;
    private static final int MSG_MARK_AS_READ = 0xfe;
    private static final int MSG_DELETE_SMS = 0xfd;
    private static final int MSG_SHOW_TOAST = 0xfc;
    private static final int MSG_RECORD_SMS_MSG = 0xfb;
    private static final int MSG_KILL_ME = 0xfa;
    private static final int MSG_AUTO_INPUT_CODE = 0xf9;
    private static final int MSG_COPY_TO_CLIPBOARD = 0xf8;
    private static final int MSG_SHOW_CODE_NOTIFICATION = 0xf7;
    private static final int MSG_CANCEL_NOTIFICATION = 0xf6;

    private static final int OP_DELETE = 0;
    private static final int OP_MARK_AS_READ = 1;

    @IntDef({OP_DELETE, OP_MARK_AS_READ})
    @interface SmsOp {
    }

    private static final int DEFAULT_QUIT_COUNT = 0;

    private Context mPhoneContext;
    private Context mAppContext;
    private XSharedPreferences mPreferences;
    private Intent mSmsIntent;

    private AtomicInteger mPreQuitQueueCount;

    private Handler uiHandler;
    private Handler workerHandler;

    SmsCodeWorker(Context appContext, Context phoneContext, Intent smsIntent) {
        mAppContext = appContext;
        mPhoneContext = phoneContext;
        mPreferences = new XSharedPreferences(BuildConfig.APPLICATION_ID);
        mSmsIntent = smsIntent;

        HandlerThread workerThread = new HandlerThread("SmsCodeWorker");
        workerThread.start();
        workerHandler = new WorkerHandler(workerThread.getLooper());

        uiHandler = new WorkerHandler(Looper.getMainLooper());

        mPreQuitQueueCount = new AtomicInteger(DEFAULT_QUIT_COUNT);
    }

    public ParseResult parse() {
        if (!XSPUtils.isEnabled(mPreferences)) {
            XLog.i("SmsCode disabled, exiting");
            return null;
        }

        boolean verboseLog = XSPUtils.isVerboseLogMode(mPreferences);
        if (verboseLog) {
            XLog.setLogLevel(Log.VERBOSE);
        } else {
            XLog.setLogLevel(BuildConfig.LOG_LEVEL);
        }

        SmsMsg smsMsg = SmsMsg.fromIntent(mSmsIntent);

        String sender = smsMsg.getSender();
        String msgBody = smsMsg.getBody();
        if (BuildConfig.DEBUG) {
            XLog.d("Sender: %s", sender);
            XLog.d("Body: %s", msgBody);
        } else {
            XLog.d("Sender: %s", StringUtils.escape(sender));
            XLog.d("Body: %s", StringUtils.escape(msgBody));
        }
        if (TextUtils.isEmpty(sender) || TextUtils.isEmpty(msgBody)) {
            return null;
        }

        String smsCode = SmsCodeUtils.parseSmsCodeIfExists(mAppContext, msgBody, true);
        if (TextUtils.isEmpty(smsCode)) { // isn't code message
            return null;
        }

        smsMsg.setSmsCode(smsCode);
        smsMsg.setCompany(SmsCodeUtils.parseCompany(msgBody));
        long timestamp = System.currentTimeMillis();
        smsMsg.setDate(timestamp);

        // 去除重复短信
        SmsMsg prevSmsMsg = EntityStoreManager.loadEntityFromFile(EntityType.PREV_SMS_MSG, SmsMsg.class);
        if (prevSmsMsg != null) {
            if (Math.abs(timestamp - prevSmsMsg.getDate()) <= 5000) {
                if ((sender.equals(prevSmsMsg.getSender()) && smsCode.equals(prevSmsMsg.getSmsCode()))
                        || msgBody.equals(prevSmsMsg.getBody())) {
                    return buildParseResult();
                }
            }
        }
        // 保存当前验证码记录
        EntityStoreManager.storeEntityToFile(EntityType.PREV_SMS_MSG, smsMsg);

        // 是否复制到剪切板
        if (XSPUtils.copyToClipboardEnabled(mPreferences)) {
            Message copyMsg = uiHandler.obtainMessage(MSG_COPY_TO_CLIPBOARD, smsCode);
            uiHandler.sendMessage(copyMsg);
        }

        // 是否显示Toast
        if (XSPUtils.shouldShowToast(mPreferences)) {
            Message toastMsg = uiHandler.obtainMessage(MSG_SHOW_TOAST, smsCode);
            uiHandler.sendMessage(toastMsg);
        }

        // 是否自动输入
        if (XSPUtils.autoInputCodeEnabled(mPreferences)) {
            Message autoInputMsg = workerHandler.obtainMessage(MSG_AUTO_INPUT_CODE, smsMsg);
            workerHandler.sendMessage(autoInputMsg);
        }

        // 是否显示通知
        if (XSPUtils.showCodeNotification(mPreferences)) {
            Message notificationMsg = workerHandler.obtainMessage(MSG_SHOW_CODE_NOTIFICATION, smsMsg);
            workerHandler.sendMessage(notificationMsg);
        }

        // 是否记录验证码短信
        if (XSPUtils.recordSmsCodeEnabled(mPreferences)) {
            Message recordMsg = workerHandler.obtainMessage(MSG_RECORD_SMS_MSG, smsMsg);
            workerHandler.sendMessage(recordMsg);
        }

        // 是否删除验证码短信
        if (XSPUtils.deleteSmsEnabled(mPreferences)) {
            Message deleteMsg = workerHandler.obtainMessage(MSG_DELETE_SMS, smsMsg);
            mPreQuitQueueCount.getAndIncrement();
            workerHandler.sendMessageDelayed(deleteMsg, 3000);
        } else {
            // 是否标记验证码短信为已读
            if (XSPUtils.markAsReadEnabled(mPreferences)) {
                Message markMsg = workerHandler.obtainMessage(MSG_MARK_AS_READ, smsMsg);
                mPreQuitQueueCount.getAndIncrement();
                workerHandler.sendMessageDelayed(markMsg, 3000);
            }
        }

        // 是否自杀
        if (XSPUtils.killMeEnabled(mPreferences)) {
            mPreQuitQueueCount.getAndIncrement();
            workerHandler.sendEmptyMessageDelayed(MSG_KILL_ME, 4000);
        }

        return buildParseResult();
    }

    private ParseResult buildParseResult() {
        ParseResult parseResult = new ParseResult();
        parseResult.setBlockSms(XSPUtils.blockSmsEnabled(mPreferences));
        return parseResult;
    }

    private class WorkerHandler extends Handler {

        WorkerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_COPY_TO_CLIPBOARD: {
                    copyToClipboard((String) msg.obj);
                    break;
                }
                case MSG_SHOW_TOAST: {
                    showToast((String) msg.obj);
                    break;
                }
                case MSG_DELETE_SMS: {
                    SmsMsg smsMsg = (SmsMsg) msg.obj;
                    deleteSms(smsMsg.getSender(), smsMsg.getBody());
                    handlePreQuitQueue();
                    break;
                }
                case MSG_MARK_AS_READ: {
                    SmsMsg smsMsg = (SmsMsg) msg.obj;
                    markSmsAsRead(smsMsg.getSender(), smsMsg.getBody());
                    handlePreQuitQueue();
                    break;
                }
                case MSG_RECORD_SMS_MSG: {
                    recordSmsMsg((SmsMsg) msg.obj);
                    break;
                }
                case MSG_AUTO_INPUT_CODE: {
                    SmsMsg smsMsg = (SmsMsg) msg.obj;
                    prepareAutoInputCode(smsMsg.getSmsCode());
                    break;
                }
                case MSG_QUIT_QUEUE:
                    quit();
                    break;
                case MSG_KILL_ME: {
                    killBackgroundProcess(BuildConfig.APPLICATION_ID);
                    handlePreQuitQueue();
                    break;
                }
                case MSG_SHOW_CODE_NOTIFICATION: {
                    showCodeNotification((SmsMsg) msg.obj);
                    break;
                }
                case MSG_CANCEL_NOTIFICATION: {
                    cancelNotification((Integer) msg.obj);
                    handlePreQuitQueue();
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unsupported msg type");
            }
        }
    }

    private void copyToClipboard(String smsCode) {
        ClipboardUtils.copyToClipboard(mAppContext, smsCode);
    }

    private void showToast(String toast) {
        String text = mAppContext.getString(R.string.current_sms_code, toast);
        Toast.makeText(mAppContext, text, Toast.LENGTH_LONG).show();
    }

    private void markSmsAsRead(String sender, String body) {
        XLog.d("Marking SMS as read...");
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
            if (ContextCompat.checkSelfPermission(mAppContext, Manifest.permission.READ_SMS)
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
            ContentResolver resolver = mAppContext.getContentResolver();
            cursor = resolver.query(uri, projection, null, null, sortOrder);
            if (cursor == null) {
                XLog.d("Cursor is null");
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
                        int rows = resolver.delete(uri, where, selectionArgs);
                        if (rows > 0) {
                            return true;
                        }
                    } else if (smsOp == OP_MARK_AS_READ) {
                        ContentValues values = new ContentValues();
                        values.put(Telephony.Sms.READ, true);
                        int rows = resolver.update(uri, values, where, selectionArgs);
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

    private void recordSmsMsg(SmsMsg smsMsg) {
        try {
            Uri smsMsgUri = DBProvider.SMS_MSG_CONTENT_URI;

            ContentValues values = new ContentValues();
            values.put(SmsMsgDao.Properties.Body.columnName, smsMsg.getBody());
            values.put(SmsMsgDao.Properties.Company.columnName, smsMsg.getCompany());
            values.put(SmsMsgDao.Properties.Date.columnName, smsMsg.getDate());
            values.put(SmsMsgDao.Properties.Sender.columnName, smsMsg.getSender());
            values.put(SmsMsgDao.Properties.SmsCode.columnName, smsMsg.getSmsCode());

            ContentResolver resolver = mAppContext.getContentResolver();
            resolver.insert(smsMsgUri, values);
            XLog.d("Add code record succeed by content provider");

            String[] projections = {SmsMsgDao.Properties.Id.columnName};
            String order = SmsMsgDao.Properties.Date.columnName + " ASC";
            Cursor cursor = resolver.query(smsMsgUri, projections, null, null, order);
            if (cursor == null) {
                return;
            }
            int count = cursor.getCount();
            int maxRecordCount = PrefConst.MAX_SMS_RECORDS_COUNT_DEFAULT;
            if (cursor.getCount() > maxRecordCount) {
                // 删除最早的记录，直至剩余数目为 PrefConst.MAX_SMS_RECORDS_COUNT_DEFAULT
                ArrayList<ContentProviderOperation> operations = new ArrayList<>();
                String selection = SmsMsgDao.Properties.Id.columnName + " = ?";
                for (int i = 0; i < count - maxRecordCount; i++) {
                    cursor.moveToNext();
                    long id = cursor.getLong(cursor.getColumnIndex(SmsMsgDao.Properties.Id.columnName));
                    ContentProviderOperation operation = ContentProviderOperation.newDelete(smsMsgUri)
                            .withSelection(selection, new String[]{String.valueOf(id)})
                            .build();

                    operations.add(operation);
                }

                resolver.applyBatch(DBProvider.AUTHORITY, operations);
                XLog.d("Remove outdated code records succeed by content provider");
            }

            cursor.close();
        } catch (Exception e1) {
            // ContentProvider dead.
            // Write file to do data transition
            if (CodeRecordRestoreManager.exportToFile(smsMsg)) {
                XLog.d("Export code record to file succeed");
            }
        }
    }

    /**
     * android.app.ActivityManager#killBackgroundProcess()
     */
    @SuppressLint("MissingPermission")
    private void killBackgroundProcess(String packageName) {
        try {
            ActivityManager activityManager = (ActivityManager) mAppContext.getSystemService(Context.ACTIVITY_SERVICE);

            if (activityManager != null) {
                activityManager.killBackgroundProcesses(packageName);
                XLog.d("Kill %s background process succeed", packageName);
            }
        } catch (Throwable e) {
            XLog.e("Error occurs when kill background process %s", packageName, e);
        }
    }

    private void prepareAutoInputCode(String code) {
        if (!autoInputBlockedHere()) {
            autoInputCode(code);
        }
    }

    private boolean autoInputBlockedHere() {
        boolean result = false;
        try {
            List<String> blockedAppList = new ArrayList<>();
            try {
                Uri appInfoUri = DBProvider.APP_INFO_URI;
                ContentResolver resolver = mAppContext.getContentResolver();

                final String packageColumn = AppInfoDao.Properties.PackageName.columnName;
                final String blockedColumn = AppInfoDao.Properties.Blocked.columnName;

                String[] projection = {packageColumn,};
                String selection = blockedColumn + " = ?";
                String[] selectionArgs = {String.valueOf(1)};
                Cursor cursor = resolver.query(appInfoUri, projection, selection, selectionArgs, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        blockedAppList.add(cursor.getString(cursor.getColumnIndex(packageColumn)));
                    }
                    cursor.close();
                }
                XLog.d("Get blocked apps by content provider");
            } catch (Exception e) {
                List<AppInfo> appInfoList = EntityStoreManager
                        .loadEntitiesFromFile(EntityType.BLOCKED_APP, AppInfo.class);
                for (AppInfo appInfo : appInfoList) {
                    blockedAppList.add(appInfo.getPackageName());
                }
                XLog.d("Get blocked apps from file");
            }

            if (blockedAppList.isEmpty()) {
                return false;
            }

            List<ActivityManager.RunningTaskInfo> runningTasks = getRunningTasks(mPhoneContext);
            String topPkgPrimary = null;
            if (runningTasks != null && runningTasks.size() > 0) {
                topPkgPrimary = runningTasks.get(0).topActivity.getPackageName();
                XLog.d("topPackagePrimary: %s", topPkgPrimary);
            }

            if (topPkgPrimary != null && blockedAppList.contains(topPkgPrimary)) {
                return true;
            }

            // RunningAppProcess 判断当前的进程不是很准确，所以用作次要参考
            List<ActivityManager.RunningAppProcessInfo> appProcesses = getRunningAppProcesses(mPhoneContext);
            if (appProcesses == null) {
                return false;
            }

            String[] topPkgSecondary = appProcesses.get(0).pkgList;
            String topProcessSecondary = appProcesses.get(0).processName;
            XLog.d("topProcessSecondary: %s, topPackages: %s", topProcessSecondary, Arrays.toString(topPkgSecondary));

            if (blockedAppList.contains(topProcessSecondary)) {
                result = true;
            } else {
                for (String topPackage : topPkgSecondary) {
                    if (blockedAppList.contains(topPackage)) {
                        result = true;
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            XLog.e("", t);
        }
        return result;
    }

    // auto-input
    private void autoInputCode(String code) {
        try {
            InputHelper.sendText(code);
            XLog.d("Auto input code succeed");
        } catch (Throwable throwable) {
            XLog.e("Error occurs when auto input code", throwable);
        }
    }

    private void showCodeNotification(SmsMsg smsMsg) {
        NotificationManager manager = (NotificationManager) mPhoneContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }

        String company = smsMsg.getCompany();
        String smsCode = smsMsg.getSmsCode();
        String title = TextUtils.isEmpty(company) ? smsMsg.getSender() : company;
        String content = mAppContext.getString(R.string.code_notification_content, smsCode);

        int notificationId = smsMsg.hashCode();

        Intent copyCodeIntent = CopyCodeReceiver.createIntent(smsCode);
        PendingIntent contentIntent = PendingIntent.getBroadcast(mPhoneContext,
                0, copyCodeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent preQuitQueueIntent = new Intent(ACTION_PRE_QUIT_QUEUE);
        PendingIntent deleteIntent = PendingIntent.getBroadcast(mPhoneContext,
                0, preQuitQueueIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(mAppContext, NotificationConst.CHANNEL_ID_SMSCODE_NOTIFICATION)
                .setSmallIcon(R.drawable.ic_app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(mAppContext.getResources(), R.drawable.ic_app_icon))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(contentIntent)
                .setDeleteIntent(deleteIntent)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(mAppContext, R.color.ic_launcher_background))
                .setGroup(NotificationConst.GROUP_KEY_SMSCODE_NOTIFICATION)
                .build();

        manager.notify(notificationId, notification);
        XLog.d("Show notification succeed");

        if (XSPUtils.autoCancelCodeNotification(mPreferences)) {
            if (mCommandReceiver == null) {
                mCommandReceiver = new CommandReceiver();
                mCommandReceiver.register(mPhoneContext);
            }

            Message cancelNotifyMsg = workerHandler
                    .obtainMessage(MSG_CANCEL_NOTIFICATION, notificationId);
            int retentionTime = XSPUtils.getNotificationRetentionTime(mPreferences) * 1000;
            mPreQuitQueueCount.getAndIncrement();
            workerHandler.sendMessageDelayed(cancelNotifyMsg, retentionTime);
        }
    }

    private void cancelNotification(int notificationId) {
        NotificationManager manager = (NotificationManager) mPhoneContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        manager.cancel(notificationId);
        XLog.d("Notification auto cancelled");
    }

    private void handlePreQuitQueue() {
        mPreQuitQueueCount.decrementAndGet();
        if (mPreQuitQueueCount.get() <= DEFAULT_QUIT_COUNT) {
            // 结束Looper
            workerHandler.sendEmptyMessageDelayed(MSG_QUIT_QUEUE, 1000);
        }
    }

    private void quit() {
        if (workerHandler != null) {
            workerHandler.getLooper().quitSafely();
            XLog.d("Worker thread quit");
        }
        if (mCommandReceiver != null) {
            mCommandReceiver.unregister(mPhoneContext);
        }
    }

    private List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return am == null ? null : am.getRunningAppProcesses();
    }

    @SuppressWarnings("deprecation")
    private List<ActivityManager.RunningTaskInfo> getRunningTasks(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return am == null ? null : am.getRunningTasks(10);
    }

    private CommandReceiver mCommandReceiver = null;

    private class CommandReceiver extends BroadcastReceiver {

        void register(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_PRE_QUIT_QUEUE);
            context.registerReceiver(this, filter);
        }

        void unregister(Context context) {
            context.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            XLog.d("CommandReceiver received: %s", action);
            if (ACTION_PRE_QUIT_QUEUE.equals(action)) {
                handlePreQuitQueue();
            }
        }
    }

}
