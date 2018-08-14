package com.github.tianma8023.xposed.smscode.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.github.tianma8023.xposed.smscode.R;
import com.github.tianma8023.xposed.smscode.app.theme.ThemeItem;
import com.github.tianma8023.xposed.smscode.constant.IConstants;
import com.github.tianma8023.xposed.smscode.constant.IPrefConstants;
import com.github.tianma8023.xposed.smscode.utils.ModuleUtils;
import com.github.tianma8023.xposed.smscode.utils.PackageUtils;
import com.github.tianma8023.xposed.smscode.utils.VerificationUtils;
import com.github.tianma8023.xposed.smscode.utils.XLog;

/**
 * 首选项Fragment
 */
public class SettingsFragment extends BasePreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    public static final String EXTRA_KEY_CURRENT_THEME = "extra_key_current_theme";

    private HomeActivity mHomeActivity;

    public interface OnPreferenceClickCallback {
        void onPreferenceClicked(String key, String title, boolean nestedPreference);
    }

    private OnPreferenceClickCallback mPreferenceClickCallback;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance(ThemeItem curThemeItem) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_KEY_CURRENT_THEME, curThemeItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
        if (!ModuleUtils.isModuleEnabled()) {
            Preference enablePref = findPreference(IPrefConstants.KEY_ENABLE);
            enablePref.setEnabled(false);
            enablePref.setSummary(R.string.pref_enable_summary_alt);
        }

        findPreference(IPrefConstants.KEY_HIDE_LAUNCHER_ICON).setOnPreferenceChangeListener(this);
        findPreference(IPrefConstants.KEY_VERBOSE_LOG_MODE).setOnPreferenceChangeListener(this);

        findPreference(IPrefConstants.KEY_SOURCE_CODE).setOnPreferenceClickListener(this);
        findPreference(IPrefConstants.KEY_DONATE_BY_ALIPAY).setOnPreferenceClickListener(this);
        findPreference(IPrefConstants.KEY_DONATE_BY_WECHAT).setOnPreferenceClickListener(this);
        findPreference(IPrefConstants.KEY_SMSCODE_TEST).setOnPreferenceClickListener(this);
        findPreference(IPrefConstants.KEY_ENTRY_AUTO_INPUT_CODE).setOnPreferenceClickListener(this);
        Preference chooseThemePref = findPreference(IPrefConstants.KEY_CHOOSE_THEME);
        chooseThemePref.setOnPreferenceClickListener(this);
        initChooseThemePreference(chooseThemePref);

        // Hide mark as read preference item.
        Preference markAsReadPref = findPreference(IPrefConstants.KEY_MARK_AS_READ);
        PreferenceGroup experimentalGroup = (PreferenceGroup) findPreference(IPrefConstants.KEY_EXPERIMENTAL);
        experimentalGroup.removePreference(markAsReadPref);
    }

    private void initChooseThemePreference(Preference chooseThemePref) {
        Bundle args = getArguments();
        ThemeItem themeItem = args.getParcelable(EXTRA_KEY_CURRENT_THEME);
        if (themeItem != null) {
            chooseThemePref.setSummary(themeItem.getColorNameRes());
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHomeActivity = (HomeActivity) getActivity();
    }

    public void setOnPreferenceClickCallback(OnPreferenceClickCallback preferenceClickCallback) {
        mPreferenceClickCallback = preferenceClickCallback;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (IPrefConstants.KEY_SOURCE_CODE.equals(key)) {
            aboutProject();
        } else if (IPrefConstants.KEY_DONATE_BY_ALIPAY.equals(key)) {
            donateByAlipay();
        } else if (IPrefConstants.KEY_DONATE_BY_WECHAT.equals(key)) {
            donateByWechat();
        } else if (IPrefConstants.KEY_SMSCODE_TEST.equals(key)) {
            showSmsCodeTestDialog();
        } else if (IPrefConstants.KEY_ENTRY_AUTO_INPUT_CODE.equals(key)) {
            if (mPreferenceClickCallback != null) {
                mPreferenceClickCallback.onPreferenceClicked(key, preference.getTitle().toString(), true);
            }
        } else if (IPrefConstants.KEY_CHOOSE_THEME.equals(key)) {
            if (mPreferenceClickCallback != null) {
                mPreferenceClickCallback.onPreferenceClicked(key, preference.getTitle().toString(), false);
            }
        } else {
            return false;
        }
        return true;
    }

    private void aboutProject() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(IConstants.PROJECT_SOURCE_CODE_URL));
        startActivity(intent);
    }

    private void donateByAlipay() {
        if (PackageUtils.isAlipayInstalled(mHomeActivity)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(IConstants.ALIPAY_QRCODE_URI_PREFIX
                    + IConstants.ALIPAY_QRCODE_URL));
            startActivity(intent);
        } else {
            Toast.makeText(mHomeActivity, R.string.alipay_install_prompt, Toast.LENGTH_SHORT).show();
        }
    }

    private void donateByWechat() {
        if (PackageUtils.isWeChatInstalled(mHomeActivity)) {
            if (ModuleUtils.isModuleEnabled()) {
                Intent intent = new Intent();
                intent.setClassName(IConstants.WECHAT_PACKAGE_NAME, IConstants.WECHAT_LAUNCHER_UI);
                intent.putExtra(IConstants.WECHAT_KEY_EXTRA_DONATE, true);
                startActivity(intent);
            }
        } else {
            Toast.makeText(mHomeActivity, R.string.wechat_install_prompt, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (IPrefConstants.KEY_HIDE_LAUNCHER_ICON.equals(key)) {
            hideOrShowLauncherIcon((Boolean) newValue);
        } else if (IPrefConstants.KEY_VERBOSE_LOG_MODE.equals(key)) {
            onVerboseLogModeSwitched((Boolean) newValue);
        } else {
            return false;
        }
        return true;
    }

    private void hideOrShowLauncherIcon(boolean hide) {
        PackageManager pm = mHomeActivity.getPackageManager();
        ComponentName launcherCN = new ComponentName(mHomeActivity, IConstants.HOME_ACTIVITY_ALIAS);
        int state = hide ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        pm.setComponentEnabledSetting(launcherCN, state, PackageManager.DONT_KILL_APP);
    }

    private void onVerboseLogModeSwitched(boolean on) {
        if (on) {
            XLog.setLogLevel(Log.VERBOSE);
        } else {
            XLog.setLogLevel(BuildConfig.LOG_LEVEL);
        }
    }

    private void showSmsCodeTestDialog() {
        new MaterialDialog.Builder(mHomeActivity)
                .title(R.string.pref_smscode_test)
                .input(R.string.sms_content_hint, 0, true, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        new Thread(new SmsCodeTestTask(mHomeActivity, input.toString())).start();
                    }
                })
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .negativeText(R.string.cancel)
                .show();
    }

    private class SmsCodeTestTask implements Runnable {

        private String mMsgBody;
        private Context mContext;

        SmsCodeTestTask(Context context, String msgBody) {
            mMsgBody = msgBody;
            mContext = context;
        }

        @Override
        public void run() {
            Message msg = new Message();
            msg.what = MSG_SMSCODE_TEST;
            if (TextUtils.isEmpty(mMsgBody)) {
                msg.obj = "";
            } else {
                msg.obj = VerificationUtils.parseVerificationCodeIfExists(mContext, mMsgBody);
            }
            mHandler.sendMessage(msg);
        }
    }

    private static final int MSG_SMSCODE_TEST = 0xff;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SMSCODE_TEST:
                    handleSmsCode((String) msg.obj);
                    return true;
            }
            return false;
        }
    });

    private void handleSmsCode(String verificationCode) {
        String text;
        if (TextUtils.isEmpty(verificationCode)) {
            text = getString(R.string.cannot_parse_smscode);
        } else {
            text = getString(R.string.cur_verification_code, verificationCode);
        }
        Toast.makeText(mHomeActivity, text, Toast.LENGTH_LONG).show();
    }
}
