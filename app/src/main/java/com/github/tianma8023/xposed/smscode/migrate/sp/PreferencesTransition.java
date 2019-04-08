package com.github.tianma8023.xposed.smscode.migrate.sp;

import android.content.Context;

import com.github.tianma8023.xposed.smscode.migrate.ITransition;
import com.github.tianma8023.xposed.smscode.utils.SPUtils;

/**
 * SharedPreferences 相关数据迁移
 */
public class PreferencesTransition implements ITransition {

    private Context mContext;
    private int mLocalVersionCode;

    private static final int VERSION_CODE_16 = 16;

    public PreferencesTransition(Context context) {
        mContext = context;
        mLocalVersionCode = SPUtils.getLocalVersionCode(mContext);
    }

    @Override
    public boolean shouldTransit() {
        return false;
    }

    @Override
    public boolean doTransition() {
        return false;
    }
}
