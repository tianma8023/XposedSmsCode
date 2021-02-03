package com.tianma.xsmscode.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.github.tianma8023.xposed.smscode.R;
import com.jaredrummler.cyanea.prefs.CyaneaSettingsActivity;
import com.tianma.xsmscode.common.constant.Const;
import com.tianma.xsmscode.common.constant.PrefConst;
import com.tianma.xsmscode.common.preference.ResetEditPreference;
import com.tianma.xsmscode.common.preference.ResetEditPreferenceDialogFragCompat;
import com.tianma.xsmscode.common.utils.ClipboardUtils;
import com.tianma.xsmscode.common.utils.ModuleUtils;
import com.tianma.xsmscode.common.utils.PackageUtils;
import com.tianma.xsmscode.common.utils.SnackbarHelper;
import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.data.db.entity.ApkVersion;
import com.tianma.xsmscode.ui.block.AppBlockActivity;
import com.tianma.xsmscode.ui.record.CodeRecordActivity;
import com.tianma.xsmscode.ui.rule.CodeRulesActivity;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import dagger.android.support.AndroidSupportInjection;

/**
 * 首选项Fragment
 */
public class SettingsFragment extends PreferenceFragmentCompat implements
        Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener,
        HasAndroidInjector,
        SettingsContract.View {

    static final String EXTRA_ACTION = "extra_action";
    static final String ACTION_GET_RED_PACKET = "get_red_packet";

    private HomeActivity mActivity;

    @Inject
    DispatchingAndroidInjector<Object> androidInjector;

    @Inject
    SettingsContract.Presenter mPresenter;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance() {
        return newInstance(null);
    }

    public static SettingsFragment newInstance(String extraAction) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_ACTION, extraAction);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return androidInjector;
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);

        // general group
        if (!ModuleUtils.isModuleEnabled()) {
            Preference enablePref = findPreference(PrefConst.KEY_ENABLE);
            enablePref.setSummary(R.string.pref_enable_summary_alt);
        }

        findPreference(PrefConst.KEY_HIDE_LAUNCHER_ICON).setOnPreferenceChangeListener(this);

        findPreference(PrefConst.KEY_CHOOSE_THEME).setOnPreferenceClickListener(this);

        findPreference(PrefConst.KEY_APP_BLOCK_ENTRY).setOnPreferenceClickListener(this);
        // general group end

        // experimental group
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
        versionPref.setOnPreferenceClickListener(this);
        showVersionInfo(versionPref);
        findPreference(PrefConst.KEY_JOIN_QQ_GROUP).setOnPreferenceClickListener(this);
        findPreference(PrefConst.KEY_SOURCE_CODE).setOnPreferenceClickListener(this);
        findPreference(PrefConst.KEY_GET_ALIPAY_PACKET).setOnPreferenceClickListener(this);
        findPreference(PrefConst.KEY_DONATE_BY_ALIPAY).setOnPreferenceClickListener(this);
        // about group end
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (HomeActivity) requireActivity();

        mPresenter.handleArguments(getArguments());
    }

    @Override
    public void onPause() {
        super.onPause();
        String preferencesName = getPreferenceManager().getSharedPreferencesName();
        mPresenter.setPreferenceWorldWritable(preferencesName);
        mPresenter.setInternalFilesWritable();
    }

    private void showEnableModuleDialog() {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.enable_module_title)
                .content(R.string.enable_module_message)
                .neutralText(R.string.ignore)
                .negativeText(R.string.taichi_users_notice)
                .onNegative((dialog, which) -> mActivity.onTaichiUsersNoticeSelected())
                .positiveText(R.string.open_xposed_installer)
                .onPositive((dialog, which) -> openXposedInstaller())
                .show();
    }

    @Override
    public void showAppAlreadyNewest() {
        SnackbarHelper.makeLong(getListView(), R.string.app_already_newest).show();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (PrefConst.KEY_CHOOSE_THEME.equals(key)) {
            Intent intent = new Intent(mActivity, CyaneaSettingsActivity.class);
            startActivity(intent);
        } else if (PrefConst.KEY_CODE_RULES.equals(key)) {
            CodeRulesActivity.startToMe(mActivity);
        } else if (PrefConst.KEY_SMSCODE_TEST.equals(key)) {
            showSmsCodeTestDialog();
        } else if (PrefConst.KEY_JOIN_QQ_GROUP.equals(key)) {
            mPresenter.joinQQGroup();
        } else if (PrefConst.KEY_SOURCE_CODE.equals(key)) {
            mPresenter.showSourceProject();
        } else if (PrefConst.KEY_DONATE_BY_ALIPAY.equals(key)) {
            donateByAlipay();
        } else if (PrefConst.KEY_ENTRY_CODE_RECORDS.equals(key)) {
            CodeRecordActivity.startToMe(mActivity);
        } else if (PrefConst.KEY_GET_ALIPAY_PACKET.equals(key)) {
            getAlipayPacket();
        } else if (PrefConst.KEY_APP_BLOCK_ENTRY.equals(key)) {
            AppBlockActivity.startMe(mActivity);
        } else if (PrefConst.KEY_VERSION.equals(key)) {
            mPresenter.checkUpdate();
        } else {
            return false;
        }
        return true;
    }

    private void showVersionInfo(Preference preference) {
        String summary = getString(R.string.pref_version_summary, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
        preference.setSummary(summary);
    }

    private void getAlipayPacket() {
        final String packetCode = Const.ALIPAY_RED_PACKET_CODE;
        new MaterialDialog.Builder(mActivity)
                .title(R.string.pref_get_alipay_packet_title)
                .content(getString(R.string.pref_get_alipay_packet_content, packetCode))
                .positiveText(R.string.copy_packet_code_open_alipay)
                .onPositive((dialog, which) -> {
                    ClipboardUtils.copyToClipboard(mActivity, packetCode);
                    String text = getString(R.string.alipay_red_packet_code_copied, Const.ALIPAY_RED_PACKET_CODE);
                    Toast.makeText(mActivity, text, Toast.LENGTH_SHORT).show();
                    PackageUtils.startAlipayActivity(mActivity);
                })
                .show();
    }

    private void donateByAlipay() {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.donation_tips_title)
                .content(R.string.donation_tips_content)
                .positiveText(R.string.donate_directly)
                .onPositive((dialog, which) -> PackageUtils.startAlipayDonatePage(mActivity))
                .negativeText(R.string.get_red_packet)
                .onNegative((dialog, which) -> getAlipayPacket())
                .show();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (PrefConst.KEY_HIDE_LAUNCHER_ICON.equals(key)) {
            mPresenter.hideOrShowLauncherIcon((Boolean) newValue);
        } else if (PrefConst.KEY_VERBOSE_LOG_MODE.equals(key)) {
            onVerboseLogModeSwitched((Boolean) newValue);
        } else {
            return false;
        }
        return true;
    }

    private void onVerboseLogModeSwitched(boolean on) {
        XLog.setLogLevel(on ? Log.VERBOSE : BuildConfig.LOG_LEVEL);
    }

    private void showSmsCodeTestDialog() {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.pref_smscode_test_title)
                .input(R.string.sms_content_hint, 0, true,
                        (dialog, input) -> mPresenter.performSmsCodeTest(input.toString()))
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .negativeText(R.string.cancel)
                .show();
    }

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

    @Override
    public void showGetAlipayPacketDialog() {
        scrollToPreference(PrefConst.KEY_GET_ALIPAY_PACKET);
        getAlipayPacket();
    }

    @Override
    public void showSmsCodeTestResult(String code) {
        String text = TextUtils.isEmpty(code) ? getString(R.string.cannot_parse_smscode)
                : getString(R.string.current_sms_code, code);
        SnackbarHelper.makeLong(getListView(), text).show();
    }

    @Override
    public void showCheckError(Throwable t) {
        SnackbarHelper.makeShort(getListView(), R.string.check_update_failed).show();
    }

    @Override
    public void showUpdateDialog(ApkVersion latestVersion) {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.new_version_found)
                .content(latestVersion.getVersionInfo())
                .positiveText(R.string.update_from_coolapk)
                .onPositive((dialog, which) -> mPresenter.updateFromCoolApk())
                .negativeText(R.string.update_from_github)
                .onNegative((dialog, which) -> mPresenter.updateFromGithub())
                .show();
    }

    @Override
    public void updateUIByModuleStatus(boolean moduleEnabled) {
        Preference enablePref = findPreference(PrefConst.KEY_ENABLE);
        if(moduleEnabled) {
            enablePref.setSummary(R.string.pref_enable_summary);
        } else {
            enablePref.setSummary(R.string.pref_enable_summary_alt);

            showEnableModuleDialog();
        }
    }
}
