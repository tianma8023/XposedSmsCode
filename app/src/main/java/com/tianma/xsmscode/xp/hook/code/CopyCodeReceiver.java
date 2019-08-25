package com.tianma.xsmscode.xp.hook.code;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.github.tianma8023.xposed.smscode.R;
import com.tianma.xsmscode.common.utils.ClipboardUtils;

/**
 * Receiver for copy code when notification clicked
 */
public class CopyCodeReceiver extends BroadcastReceiver {

    private static final String ACTION_COPY_CODE = BuildConfig.APPLICATION_ID + ".ACTION_COPY_CODE";
    private static final String EXTRA_KEY_CODE = "extra_key_code";

    public static Intent createIntent(String smsCode) {
        Intent intent = new Intent(ACTION_COPY_CODE);
        intent.putExtra(EXTRA_KEY_CODE, smsCode);
        return intent;
    }

    public static void registerMe(Context context) {
        CopyCodeReceiver receiver = new CopyCodeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_COPY_CODE);
        context.registerReceiver(receiver, filter);
    }

    private Context mAppContext;

    @Override
    public void onReceive(Context phoneContext, Intent intent) {
        String action = intent.getAction();
        if (ACTION_COPY_CODE.equals(action)) {
            String smsCode = intent.getStringExtra(EXTRA_KEY_CODE);
            // copy to clipboard
            ClipboardUtils.copyToClipboard(phoneContext, smsCode);

            // show toast
            mAppContext = createSmsCodeAppContext(phoneContext);
            showToast(smsCode);
        }
    }

    private Context createSmsCodeAppContext(Context phoneContext) {
        if (mAppContext == null) {
            try {
                mAppContext = phoneContext.createPackageContext(BuildConfig.APPLICATION_ID,
                        Context.CONTEXT_IGNORE_SECURITY);
            } catch (Exception e) {
                // ignore
            }
        }
        return mAppContext;
    }

    private void showToast(String smsCode) {
        if (mAppContext != null) {
            String text = mAppContext.getString(R.string.prompt_sms_code_copied, smsCode);
            Toast.makeText(mAppContext, text, Toast.LENGTH_LONG).show();
        }
    }
}
