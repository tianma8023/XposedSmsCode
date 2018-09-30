package com.github.tianma8023.xposed.smscode.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

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
     * @return SD card directory
     */
    public static File getSDCardDir() {
        return Environment.getExternalStorageDirectory();
    }

}
