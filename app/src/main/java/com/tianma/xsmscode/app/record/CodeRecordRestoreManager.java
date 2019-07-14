package com.tianma.xsmscode.app.record;

import android.annotation.SuppressLint;
import android.content.Context;

import com.tianma.xsmscode.constant.PrefConst;
import com.tianma.xsmscode.entity.SmsMsg;
import com.tianma.xsmscode.db.DBManager;
import com.tianma.xsmscode.utils.StorageUtils;
import com.tianma.xsmscode.utils.XLog;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CodeRecordRestoreManager {

    private static final String RECORD_FILE_PREFIX = "CodeRecord_";

    /**
     * Export code record to file
     */
    @SuppressLint({"SetWorldWritable", "SetWorldReadable"})
    public static boolean exportToFile(SmsMsg smsMsg) {
        OutputStreamWriter osw = null;
        try {
            String filename = RECORD_FILE_PREFIX + smsMsg.getDate();
            File recordFile = new File(StorageUtils.getFilesDir(), filename);
            osw = new OutputStreamWriter(
                    new FileOutputStream(recordFile), StandardCharsets.UTF_8);

            new Gson().toJson(smsMsg, osw);

            // set file world writable
            StorageUtils.setFileWorldWritable(recordFile, 0);
            return true;
        } catch (Exception e) {
            XLog.e("Export code record to file failed", e);
            return false;
        } finally {
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException ioException) {
                    // ignore
                }
            }
        }
    }

    /**
     * Import code records to database
     */
    public static boolean importToDatabase(Context context) {
        try {
            File[] recordFiles = getRecordFiles();

            List<SmsMsg> smsMsgList = new ArrayList<>();
            for (File recordFile : recordFiles) {
                SmsMsg smsMsg = loadFromFile(recordFile);
                if (smsMsg != null) {
                    smsMsgList.add(smsMsg);
                    recordFile.delete();
                }
            }

            if (!smsMsgList.isEmpty()) {
                DBManager dbManager = DBManager.get(context);
                dbManager.addSmsMsgList(smsMsgList);
                XLog.d("Import code records to database succeed");

                List<SmsMsg> allMsgList = dbManager.queryAllSmsMsg();
                if(allMsgList.size() > PrefConst.MAX_SMS_RECORDS_COUNT_DEFAULT) {
                    List<SmsMsg> outdatedMsgList = new ArrayList<>();
                    for (int i = PrefConst.MAX_SMS_RECORDS_COUNT_DEFAULT; i < allMsgList.size(); i++) {
                        outdatedMsgList.add(allMsgList.get(i));
                    }
                    dbManager.removeSmsMsgList(outdatedMsgList);
                    XLog.d("Remove outdated code records succeed");
                }
            }
            return true;
        } catch (Throwable t) {
            XLog.e("Import code records to database failed.", t);
        }
        return false;
    }

    /**
     * Get code record files
     */
    public static File[] getRecordFiles() {
        File filesDir = StorageUtils.getFilesDir();
        return filesDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(RECORD_FILE_PREFIX);
            }
        });
    }

    private static SmsMsg loadFromFile(File recordFile) {
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(
                    new FileInputStream(recordFile), StandardCharsets.UTF_8);

            return new Gson().fromJson(isr, SmsMsg.class);
        } catch (FileNotFoundException e) {
            XLog.e("", e);
        } finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
