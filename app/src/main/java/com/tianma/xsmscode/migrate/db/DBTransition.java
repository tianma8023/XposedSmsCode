package com.tianma.xsmscode.migrate.db;

import android.content.Context;

import com.tianma.xsmscode.app.record.CodeRecordRestoreManager;
import com.tianma.xsmscode.migrate.ITransition;

import java.io.File;

public class DBTransition implements ITransition {

    private Context mContext;

    public DBTransition(Context context) {
        mContext = context;
    }

    @Override
    public boolean shouldTransit() {
        File[] recordFiles = CodeRecordRestoreManager.getRecordFiles();
        return recordFiles != null && recordFiles.length > 0;
    }

    @Override
    public boolean doTransition() {
        return CodeRecordRestoreManager.importToDatabase(mContext);
    }
}
