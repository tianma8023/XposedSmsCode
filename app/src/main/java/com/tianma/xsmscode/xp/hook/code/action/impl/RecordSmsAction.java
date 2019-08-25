package com.tianma.xsmscode.xp.hook.code.action.impl;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.tianma.xsmscode.common.constant.PrefConst;
import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.common.utils.XSPUtils;
import com.tianma.xsmscode.data.db.DBProvider;
import com.tianma.xsmscode.data.db.entity.SmsMsg;
import com.tianma.xsmscode.data.db.entity.SmsMsgDao;
import com.tianma.xsmscode.ui.record.CodeRecordRestoreManager;
import com.tianma.xsmscode.xp.hook.code.action.CallableAction;

import java.util.ArrayList;

import de.robv.android.xposed.XSharedPreferences;

/**
 * 记录验证码短信
 */
public class RecordSmsAction extends CallableAction {

    public RecordSmsAction(Context appContext, Context phoneContext, SmsMsg smsMsg, XSharedPreferences xsp) {
        super(appContext, phoneContext, smsMsg, xsp);
    }

    @Override
    public Bundle action() {
        if (XSPUtils.recordSmsCodeEnabled(xsp)) {
            recordSmsMsg(mSmsMsg);
        }
        return null;
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
}
