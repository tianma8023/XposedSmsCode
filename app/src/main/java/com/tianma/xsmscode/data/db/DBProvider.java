package com.tianma.xsmscode.data.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.tianma.xsmscode.data.db.entity.AppInfoDao;
import com.tianma.xsmscode.data.db.entity.SmsCodeRuleDao;
import com.tianma.xsmscode.data.db.entity.SmsMsgDao;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DBProvider extends ContentProvider {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".db.provider";

    private static final String PATH_SMS_MSG = "sms_msg";
    private static final String PATH_SMS_CODE_RULE = "sms_code_rule";
    private static final String PATH_APP_INFO = "app_info";

    public static final Uri SMS_MSG_CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + PATH_SMS_MSG);
    public static final Uri SMS_CODE_RULE_URI =
            Uri.parse("content://" + AUTHORITY + "/" + PATH_SMS_CODE_RULE);
    public static final Uri APP_INFO_URI =
            Uri.parse("content://" + AUTHORITY + "/" + PATH_APP_INFO);

    private static final int SMS_MSG_DIR = 0;
    private static final int SMS_MSG_ID = 1;
    private static final int SMS_CODE_RULE_DIR = 2;
    private static final int SMS_CODE_RULE_ID = 3;
    private static final int APP_INFO_DIR = 4;
    private static final int APP_INFO_ID = 5;


    private static final String TABLE_SMS_MSG = SmsMsgDao.TABLENAME;
    private static final String TABLE_SMS_CODE_RULE = SmsCodeRuleDao.TABLENAME;
    private static final String TABLE_APP_INFO = AppInfoDao.TABLENAME;

    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, PATH_SMS_MSG, SMS_MSG_DIR);
        sUriMatcher.addURI(AUTHORITY, PATH_SMS_MSG + "/#", SMS_MSG_ID);

        sUriMatcher.addURI(AUTHORITY, PATH_SMS_CODE_RULE, SMS_CODE_RULE_DIR);
        sUriMatcher.addURI(AUTHORITY, PATH_SMS_CODE_RULE + "/#", SMS_CODE_RULE_ID);

        sUriMatcher.addURI(AUTHORITY, PATH_APP_INFO, APP_INFO_DIR);
        sUriMatcher.addURI(AUTHORITY, PATH_APP_INFO + "/#", APP_INFO_ID);
    }

    private SQLiteDatabase mDatabase;
    private Context mContext;

    @Override
    public boolean onCreate() {
        mContext = getContext();
        mDatabase = DBManager.get(mContext).getSQLiteDatabase();
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int uriType = sUriMatcher.match(uri);
        long id;
        String path;
        switch (uriType) {
            case SMS_MSG_DIR:
                id = mDatabase.insert(TABLE_SMS_MSG, null, values);
                path = PATH_SMS_MSG + "/" + id;
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        if (mContext != null) {
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return Uri.parse(path);
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int uriType = sUriMatcher.match(uri);
        String tableName;
        switch (uriType) {
            case SMS_CODE_RULE_DIR:
                tableName = TABLE_SMS_CODE_RULE;
                break;
            case SMS_MSG_DIR:
                tableName = TABLE_SMS_MSG;
                break;
            case APP_INFO_DIR:
                tableName = TABLE_APP_INFO;
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return mDatabase.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (uriType) {
            case SMS_MSG_DIR:
                rowsDeleted = mDatabase.delete(TABLE_SMS_MSG, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        if (rowsDeleted > 0) {
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
