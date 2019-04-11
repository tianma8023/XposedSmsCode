package com.github.tianma8023.xposed.smscode.app.record;

import android.annotation.SuppressLint;
import android.content.Context;

import com.github.tianma8023.xposed.smscode.entity.SmsMsg;
import com.github.tianma8023.xposed.smscode.db.DBManager;
import com.github.tianma8023.xposed.smscode.utils.StorageUtils;
import com.github.tianma8023.xposed.smscode.utils.XLog;
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
            XLog.e("export code record to file failed", e);
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
                DBManager.get(context).addSmsMsgList(smsMsgList);
            }
            return true;
        } catch (Throwable t) {
            XLog.e("import code records to database failed.", t);
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
