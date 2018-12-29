package com.github.tianma8023.xposed.smscode.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.tianma8023.xposed.smscode.aidl.DaoMaster;
import com.github.tianma8023.xposed.smscode.aidl.SmsMsgDao;
import com.github.tianma8023.xposed.smscode.entity.SmsCodeRuleDao;
import com.github.yuweiguocn.library.greendao.MigrationHelper;

import org.greenrobot.greendao.database.Database;

public class TSQLiteOpenHelper extends DaoMaster.OpenHelper{

    public TSQLiteOpenHelper(Context context, String name) {
        super(context, name);
    }

    public TSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);

        MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {
            @Override
            public void onCreateAllTables(Database db, boolean ifNotExists) {
                DaoMaster.createAllTables(db, ifNotExists);
            }

            @Override
            public void onDropAllTables(Database db, boolean ifExists) {
                DaoMaster.dropAllTables(db, ifExists);
            }
        }, SmsCodeRuleDao.class, SmsMsgDao.class);
    }
}
