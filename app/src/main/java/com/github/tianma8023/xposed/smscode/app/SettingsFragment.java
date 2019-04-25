package com.github.tianma8023.xposed.smscode.app;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.github.tianma8023.xposed.smscode.R;
import com.github.tianma8023.xposed.smscode.app.record.CodeRecordsActivity;
import com.github.tianma8023.xposed.smscode.app.rule.CodeRulesActivity;
import com.github.tianma8023.xposed.smscode.app.theme.ThemeItem;
import com.github.tianma8023.xposed.smscode.constant.Const;
import com.github.tianma8023.xposed.smscode.constant.PrefConst;
import com.github.tianma8023.xposed.smscode.preference.ResetEditPreference;
import com.github.tianma8023.xposed.smscode.preference.ResetEditPreferenceDialogFragCompat;
import com.github.tianma8023.xposed.smscode.utils.ClipboardUtils;
import com.github.tianma8023.xposed.smscode.utils.ModuleUtils;
import com.github.tianma8023.xposed.smscode.utils.PackageUtils;
import com.github.tianma8023.xposed.smscode.utils.SmsCodeUtils;
import com.github.tianma8023.xposed.smscode.utils.StorageUtils;
import com.github.tianma8023.xposed.smscode.utils.Utils;
import com.github.tianma8023.xposed.smscode.utils.XLog;

import java.io.File;

/**
 * 首选项Fragment
 */
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    public static final String EXTRA_CURRENT_THEME = "extra_current_theme";
    public static final String EXTRA_ACTION = "extra_action";
    public static final String ACTION_GET_RED_PACKET = "get_red_packet";

    public interface OnPreferenceClickCallback {
        void onPreferenceClicked(String key, String title, boolean nestedPreference);
    }

    private OnPreferenceClickCallback mPreferenceClickCallback;

    private HomeActivity mActivity;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance(ThemeItem curThemeItem) {
        return newInstance(curThemeItem, null);
    }

    public static SettingsFragment newInstance(ThemeItem curThemeItem, String extraAction) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_CURRENT_THEME, curThemeItem);
        args.putString(EXTRA_ACTION, extraAction);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);

        // general group
        if (!ModuleUtils.isModuleEnabled()) {
            Preference enablePref = findPreference(PrefConst.KEY_ENABLE);
            enablePref.setEnabled(false);
            enablePref.setSummary(R.string.pref_enable_summary_alt);
        }

        findPreference(PrefConst.KEY_HIDE_LAUNCHER_ICON).setOnPreferenceChangeListener(this);

        // findPreference(PrefConst.KEY_ENTRY_AUTO_INPUT_CODE).setOnPreferenceClickListener(this);

        Preference chooseThemePref = findPreference(PrefConst.KEY_CHOOSE_THEME);
        chooseThemePref.setOnPreferenceClickListener(this);
        initChooseThemePreference(chooseThemePref);
        // general group end

        // experimental group
        // findPreference(PrefConst.KEY_MARK_AS_READ).setOnPreferenceChangeListener(this);
        // findPreference(PrefConst.KEY_DELETE_SMS).setOnPreferenceChangeListener(this);

        // hide block notification
        // PreferenceGroup experimentalGroup = (PreferenceGroup) findPreference(PrefConst.KEY_EXPERIMENTAL);
        // experimentalGroup.removePreference(findPreference(PrefConst.KEY_BLOCK_SMS));
        // experimental group end

        // code message group
        findPreference(PrefConst.KEY_CODE_RULES).setOnPreferenceClickListener(this);
        findPreference(PrefConst.KEY_SMSCODE_TEST).setOnPreferenceClickListener(this);
        // code message group end

        // code records group
        Preference recordsEntryPref = findPreference(PrefConst.KEY_ENTRY_CODE_RECORDS);
        recordsEntryPref.setOnPreferenceClickListener(this);
        initRecordEntryPreference(recordsEntryPref);
        // code records group end

        // others group
        findPreference(PrefConst.KEY_VERBOSE_LOG_MODE).setOnPreferenceChangeListener(this);
        // others group end

        // about group
        // version info preference
        Preference versionPref = findPreference(PrefConst.KEY_VERSION);
        showVersionInfo(versionPref);
        findPreference(PrefConst.KEY_JOIN_QQ_GROUP).setOnPreferenceClickListener(this);
        findPreference(PrefConst.KEY_SOURCE_CODE).setOnPreferenceClickListener(this);
        findPreference(PrefConst.KEY_GET_ALIPAY_PACKET).setOnPreferenceClickListener(this);
        findPreference(PrefConst.KEY_DONATE_BY_ALIPAY).setOnPreferenceClickListener(this);
        // about group end
    }

    private void initChooseThemePreference(Preference chooseThemePref) {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        ThemeItem themeItem = args.getParcelable(EXTRA_CURRENT_THEME);
        if (themeItem != null) {
            chooseThemePref.setSummary(themeItem.getColorNameRes());
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (HomeActivity) requireActivity();

        onHandleArguments(getArguments());
    }

    private void onHandleArguments(Bundle args) {
        if (args == null) {
            return;
        }
        String extraAction = args.getString(EXTRA_ACTION);
        if (ACTION_GET_RED_PACKET.equals(extraAction)) {
            args.remove(EXTRA_ACTION);
            scrollToPreference(PrefConst.KEY_GET_ALIPAY_PACKET);
            getAlipayPacket();
        } else {
            if (!ModuleUtils.isModuleEnabled()) {
                showEnableModuleDialog();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        setPreferenceWorldWritable();
        setInternalFilesWritable();
    }

    @SuppressLint({"SetWorldReadable", "SetWorldWritable"})
    private void setPreferenceWorldWritable() {
        // dataDir: /data/data/<package_name>/
        // spDir: /data/data/<package_name>/shared_prefs/
        // spFile: /data/data/<package_name>/shared_prefs/<preferences_name>.xml
        String preferencesName = getPreferenceManager().getSharedPreferencesName();
        File prefsFile = StorageUtils.getSharedPreferencesFile(mActivity, preferencesName);
        StorageUtils.setFileWorldWritable(prefsFile, 2);
    }

    private void setInternalFilesWritable() {
        // dataDir or external dataDir
        // filesDir or external filesDir
        StorageUtils.setFileWorldWritable(StorageUtils.getFilesDir(), 1);
    }

    private void showEnableModuleDialog() {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.enable_module_title)
                .content(R.string.enable_module_message)
                .neutralText(R.string.ignore)
                .negativeText(R.string.taichi_users_notice)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mActivity.onTaichiUsersNoticeSelected();
                    }
                })
                .positiveText(R.string.open_xposed_installer)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        openXposedInstaller();
                    }
                })
                .show();
    }

    public void setOnPreferenceClickCallback(OnPreferenceClickCallback preferenceClickCallback) {
        mPreferenceClickCallback = preferenceClickCallback;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (PrefConst.KEY_ENTRY_AUTO_INPUT_CODE.equals(key)) {
            if (mPreferenceClickCallback != null) {
                mPreferenceClickCallback.onPreferenceClicked(key, preference.getTitle().toString(), true);
            }
        } else if (PrefConst.KEY_CHOOSE_THEME.equals(key)) {
            if (mPreferenceClickCallback != null) {
                mPreferenceClickCallback.onPreferenceClicked(key, preference.getTitle().toString(), false);
            }
        } else if (PrefConst.KEY_CODE_RULES.equals(key)) {
            CodeRulesActivity.startToMe(mActivity);
        } else if (PrefConst.KEY_SMSCODE_TEST.equals(key)) {
            showSmsCodeTestDialog();
        } else if (PrefConst.KEY_JOIN_QQ_GROUP.equals(key)) {
            joinQQGroup();
        } else if (PrefConst.KEY_SOURCE_CODE.equals(key)) {
            aboutProject();
        } else if (PrefConst.KEY_DONATE_BY_ALIPAY.equals(key)) {
            donateByAlipay();
        } else if (PrefConst.KEY_ENTRY_CODE_RECORDS.equals(key)) {
            CodeRecordsActivity.startToMe(mActivity);
        } else if (PrefConst.KEY_GET_ALIPAY_PACKET.equals(key)) {
            getAlipayPacket();
        } else {
            return false;
        }
        return true;
    }

    private void showVersionInfo(Preference preference) {
        String summary = getString(R.string.pref_version_summary,
                BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
        preference.setSummary(summary);
    }

    private void joinQQGroup() {
        PackageUtils.joinQQGroup(mActivity);
    }

    private void aboutProject() {
        Utils.showWebPage(mActivity, Const.PROJECT_SOURCE_CODE_URL);
    }

    private void getAlipayPacket() {
        final String packetCode = Const.ALIPAY_RED_PACKET_CODE;
        new MaterialDialog.Builder(mActivity)
                .title(R.string.pref_get_alipay_packet_title)
                .content(getString(R.string.pref_get_alipay_packet_content, packetCode))
                .positiveText(R.string.copy_packet_code_open_alipay)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ClipboardUtils.copyToClipboard(mActivity, packetCode);
                        String text = getString(R.string.alipay_red_packet_code_copied, Const.ALIPAY_RED_PACKET_CODE);
                        Toast.makeText(mActivity, text, Toast.LENGTH_SHORT).show();

                        PackageUtils.startAlipayActivity(mActivity);
                    }
                })
                .show();
    }

    private void donateByAlipay() {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.donation_tips_title)
                .content(R.string.donation_tips_content)
                .positiveText(R.string.donate_directly)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PackageUtils.startAlipayDonatePage(mActivity);
                    }
                })
                .negativeText(R.string.get_red_packet)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        getAlipayPacket();
                    }
                })
                .show();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (PrefConst.KEY_HIDE_LAUNCHER_ICON.equals(key)) {
            hideOrShowLauncherIcon((Boolean) newValue);
        } else if (PrefConst.KEY_VERBOSE_LOG_MODE.equals(key)) {
            onVerboseLogModeSwitched((Boolean) newValue);
        } /* else if (PrefConst.KEY_MARK_AS_READ.equals(key)) {
            return checkAppOpsPermission((Boolean) newValue);
        } else if (PrefConst.KEY_DELETE_SMS.equals(key)) {
            return checkAppOpsPermission((Boolean) newValue);
        } */ else {
            return false;
        }
        return true;
    }

    private void hideOrShowLauncherIcon(boolean hide) {
        PackageManager pm = mActivity.getPackageManager();
        ComponentName launcherCN = new ComponentName(mActivity, Const.HOME_ACTIVITY_ALIAS);
        int state = hide ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        if (pm.getComponentEnabledSetting(launcherCN) != state) {
            pm.setComponentEnabledSetting(launcherCN, state, PackageManager.DONT_KILL_APP);
        }
    }

    private void onVerboseLogModeSwitched(boolean on) {
        if (on) {
            XLog.setLogLevel(Log.VERBOSE);
        } else {
            XLog.setLogLevel(BuildConfig.LOG_LEVEL);
        }
    }

    private void showSmsCodeTestDialog() {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.pref_smscode_test_title)
                .input(R.string.sms_content_hint, 0, true, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        new Thread(new SmsCodeTestTask(mActivity, input.toString())).start();
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
                msg.obj = SmsCodeUtils.parseSmsCodeIfExists(mContext, mMsgBody, false);
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
        Toast.makeText(mActivity, text, Toast.LENGTH_LONG).show();
    }

    //    private boolean checkAppOpsPermission(boolean on) {
    //        if (!on) {
    //            return true;
    //        }
    //
    //        String packageName = BuildConfig.APPLICATION_ID;
    //        int uid = Process.myUid();
    //        int opWriteSms = AppOpsUtils.OP_WRITE_SMS;
    //        if (!AppOpsUtils.checkOp(mActivity, opWriteSms, uid, packageName)) {
    //            // Don't have write sms AppOps permission
    //            try {
    //                AppOpsUtils.allowOp(mActivity, opWriteSms, uid, packageName);
    //                return true;
    //            } catch (Exception e) {
    //                Toast.makeText(mActivity, R.string.no_permission_prompt, Toast.LENGTH_LONG).show();
    //                return false;
    //            }
    //        } else {
    //            return true;
    //        }
    //    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        boolean handled = false;
        if (preference instanceof ResetEditPreference) {
            DialogFragment dialogFragment =
                    ResetEditPreferenceDialogFragCompat.newInstance(preference.getKey());

            FragmentManager fm = getFragmentManager();
            if (fm != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(fm, "android.support.v7.preference.PreferenceFragment.DIALOG");
                handled = true;
            }
        }
        if (!handled) {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    private void initRecordEntryPreference(Preference preference) {
        String summary = getString(R.string.pref_entry_code_records_summary, PrefConst.MAX_SMS_RECORDS_COUNT_DEFAULT);
        preference.setSummary(summary);
    }

    private void openXposedInstaller() {
        if (!PackageUtils.startXposedActivity(mActivity, PackageUtils.Section.MODULES)) {
            Toast.makeText(mActivity, R.string.xposed_not_installed, Toast.LENGTH_SHORT).show();
        }
    }
}
