package com.github.tianma8023.xposed.smscode.migrate.sp;

import android.content.Context;

import com.crossbowffs.remotepreferences.RemotePreferences;
import com.github.tianma8023.xposed.smscode.migrate.ITransition;
import com.github.tianma8023.xposed.smscode.utils.RemotePreferencesUtils;
import com.github.tianma8023.xposed.smscode.utils.SPUtils;

/**
 * SharedPreferences 相关数据迁移
 */
public class PreferencesTransition implements ITransition {

    private Context mContext;
    private RemotePreferences mRemotePreferences;
    private int mLocalVersionCode;

    private static final int VERSION_CODE_16 = 16;

    public PreferencesTransition(Context context) {
        mContext = context;
        mRemotePreferences = RemotePreferencesUtils.getDefaultRemotePreferences(context);
        mLocalVersionCode = SPUtils.getLocalVersionCode(mRemotePreferences);
    }

    @Override
    public boolean shouldTransit() {
        if (mLocalVersionCode <= VERSION_CODE_16) {
            // v1.4.5 及以前版本，需要进行数据兼容
            return true;
        }
        return false;
    }

    @Override
    public boolean doTransition() {
//        try {
//            if (mLocalVersionCode <= VERSION_CODE_16) {
//                if (SPUtils.isAutoInputRootMode(mRemotePreferences)) {
//                    // auto-input 模式是 root模式
//                    SPUtils.setAutoInputMode(mRemotePreferences,
//                            mContext.getString(R.string.auto_input_mode_root));
//                } else if (SPUtils.isAutoInputAccessibilityMode(mRemotePreferences)) {
//                    // auto-input 模式是 accessibility模式
//                    SPUtils.setAutoInputMode(mRemotePreferences,
//                            mContext.getString(R.string.auto_input_mode_accessibility));
//                }
//            }
//            SPUtils.setLocalVersionCode(mRemotePreferences, BuildConfig.VERSION_CODE);
//            return true;
//        } catch (Exception e) {
//            XLog.e("Error occurs when do preferences transition.", e);
//        }
        return false;
    }
}
