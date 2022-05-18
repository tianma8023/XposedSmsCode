package com.tianma.xsmscode.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import java.io.File;

import androidx.core.content.ContextCompat;

import com.github.tianma8023.xposed.smscode.BuildConfig;

/**
 * Utils for storage.
 */
public class StorageUtils {

    private StorageUtils() {

    }

    public static boolean isSDCardMounted() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * 获取日志路径
     */
    public static File getLogDir(Context context) {
        if (isSDCardMounted()) {
            return context.getExternalFilesDir("log");
        } else {
            return new File(context.getFilesDir(), "log");
        }
    }

    /**
     * 获取Crash日志路径
     */
    public static File getCrashLogDir(Context context) {
        if (isSDCardMounted()) {
            return context.getExternalFilesDir("crash");
        } else {
            return new File(context.getFilesDir(), "crash");
        }
    }

    /**
     * Get sdcard directory
     *
     * @return SD card directory
     */
    public static File getSDCardDir() {
        return Environment.getExternalStorageDirectory();
    }

    /**
     * get sdcard public documents directory
     *
     * @return SD card public documents directory
     */
    public static File getPublicDocumentsDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
    }

    public static File getSharedPreferencesFile(Context context, String preferencesName) {
        File dataDir = ContextCompat.getDataDir(context);
        File prefsDir = new File(dataDir, "shared_prefs");
        return new File(prefsDir, preferencesName + ".xml");
    }

    /**
     * Get internal data dir. /data/data/<package_name>/
     */
    public static File getInternalDataDir() {
        return new File(Environment.getDataDirectory(),
                "data/" + BuildConfig.APPLICATION_ID);
    }

    /**
     * Get internal files dir. /data/data/<package_name>/files/
     */
    public static File getInternalFilesDir() {
        return new File(getInternalDataDir(), "files");
    }

    /**
     * Get external files dir. /sdcard/Android/data/<package_name>/files/
     */
    public static File getExternalFilesDir() {
        return new File(Environment.getExternalStorageDirectory(),
                "Android/data/" + BuildConfig.APPLICATION_ID + "/files/");
    }

    /**
     * Get files dir
     * @see StorageUtils#getExternalFilesDir()
     * @see StorageUtils#getInternalFilesDir()
     */
    public static File getFilesDir() {
        if (isSDCardMounted()) {
            File externalFilesDir = getExternalFilesDir();
            if (!externalFilesDir.exists()) {
                externalFilesDir.mkdirs();
            }
            return externalFilesDir;
        } else {
            return getInternalFilesDir();
        }
    }

    /**
     * Set file world writable
     */
    @SuppressLint({"SetWorldWritable", "SetWorldReadable"})
    public static void setFileWorldWritable(File file, int parentDepth) {
        if (!file.exists()) {
            return;
        }
        parentDepth = parentDepth + 1;
        for (int i = 0; i < parentDepth; i++) {
            file.setExecutable(true, false);
            file.setWritable(true, false);
            file.setReadable(true, false);
            file = file.getParentFile();
            if (file == null) {
                break;
            }
        }
    }

    /**
     * Set file world readable
     */
    @SuppressLint("SetWorldReadable")
    public static void setFileWorldReadable(File file, int parentDepth) {
        if (!file.exists()) {
            return;
        }

        for (int i = 0; i < parentDepth; i++) {
            file.setReadable(true, false);
            file.setExecutable(true, false);
            file = file.getParentFile();
            if (file == null) {
                break;
            }
        }
    }
}
