package com.tianma.xsmscode.xp.hook.code.action.impl;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;

import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.common.utils.XSPUtils;
import com.tianma.xsmscode.data.db.entity.SmsMsg;
import com.tianma.xsmscode.xp.hook.code.action.CallableAction;

import androidx.annotation.IntDef;
import androidx.core.content.ContextCompat;
import de.robv.android.xposed.XSharedPreferences;

/**
 * 将验证码短信删除或者标记为已读
 */
public class OperateSmsAction extends CallableAction {

    private static final int OP_DELETE = 0;
    private static final int OP_MARK_AS_READ = 1;

    @IntDef({OP_DELETE, OP_MARK_AS_READ})
    private @interface SmsOp {
    }

    public OperateSmsAction(Context appContext, Context phoneContext, SmsMsg smsMsg, XSharedPreferences xsp) {
        super(appContext, phoneContext, smsMsg, xsp);
    }

    @Override
    public Bundle action() {
        String sender = mSmsMsg.getSender();
        String body = mSmsMsg.getBody();
        if (XSPUtils.deleteSmsEnabled(xsp)) {
            deleteSms(sender, body);
        } else if (XSPUtils.markAsReadEnabled(xsp)) {
            markSmsAsRead(sender, body);
        }
        return null;
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

}
