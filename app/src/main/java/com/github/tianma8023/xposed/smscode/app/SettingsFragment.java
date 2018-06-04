package com.github.tianma8023.xposed.smscode.app;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.github.tianma8023.xposed.smscode.R;
import com.github.tianma8023.xposed.smscode.constant.IConstants;
import com.github.tianma8023.xposed.smscode.constant.IPrefConstants;
import com.github.tianma8023.xposed.smscode.utils.ModuleUtils;
import com.github.tianma8023.xposed.smscode.utils.PackageUtils;

/**
 * 首选项Fragment
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    HomeActivity mHomeActivity;

    public SettingsFragment() {
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

        findPreference(IPrefConstants.KEY_AUTHOR).setOnPreferenceClickListener(this);
        findPreference(IPrefConstants.KEY_DONATE_BY_ALIPAY).setOnPreferenceClickListener(this);
        findPreference(IPrefConstants.KEY_DONATE_BY_WECHAT).setOnPreferenceClickListener(this);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHomeActivity = (HomeActivity) getActivity();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (IPrefConstants.KEY_AUTHOR.equals(key)) {
            aboutAuthor();
        } else if (IPrefConstants.KEY_DONATE_BY_ALIPAY.equals(key)) {
            donateByAlipay();
        } else if (IPrefConstants.KEY_DONATE_BY_WECHAT.equals(key)) {
            donateByWechat();
        } else {
            return false;
        }
        return true;
    }

    private void aboutAuthor() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(IConstants.GITHUB_URL));
        startActivity(intent);
    }

    private void donateByAlipay() {
        if (PackageUtils.isAlipayInstalled(mHomeActivity)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("alipayqr://platformapi/startapp?saId=10000007&qrcode="
                    + IConstants.ALIPAY_QRCODE_URL));
            startActivity(intent);
        } else {
            Toast.makeText(mHomeActivity, R.string.alipay_install_prompt, Toast.LENGTH_SHORT).show();
        }
    }

    private void donateByWechat() {
        if (PackageUtils.isWeChatInstalled(mHomeActivity)) {
            Intent intent = new Intent();
            intent.setClassName(IConstants.WECHAT_PACKAGE_NAME, IConstants.WECHAT_LAUNCHER_UI);
            intent.putExtra(IConstants.WECHAT_KEY_EXTRA_DONATE, true);
            startActivity(intent);
        } else {
            Toast.makeText(mHomeActivity, R.string.wechat_install_prompt, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (IPrefConstants.KEY_HIDE_LAUNCHER_ICON.equals(key)) {
            hideOrShowLauncherIcon((Boolean) newValue);
        } else {
            return false;
        }
        return true;
    }

    private void hideOrShowLauncherIcon(boolean show) {
        PackageManager pm = mHomeActivity.getPackageManager();
        ComponentName launcherCN = new ComponentName(mHomeActivity, IConstants.HOME_ACTIVITY_ALIAS);
        int state = show ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        pm.setComponentEnabledSetting(launcherCN, state, PackageManager.DONT_KILL_APP);
    }
}
