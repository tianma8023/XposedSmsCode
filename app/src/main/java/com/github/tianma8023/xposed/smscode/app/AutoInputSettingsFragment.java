package com.github.tianma8023.xposed.smscode.app;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.tianma8023.xposed.smscode.R;
import com.github.tianma8023.xposed.smscode.constant.PrefConst;
import com.github.tianma8023.xposed.smscode.service.accessibility.SmsCodeAutoInputService;
import com.github.tianma8023.xposed.smscode.utils.AccessibilityUtils;
import com.github.tianma8023.xposed.smscode.utils.ShellUtils;

public class AutoInputSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private Context mContext;

    private ListPreference mAutoInputModePref;
    private String mCurAutoMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_auto_input_code);


        SwitchPreference autoInputPref = (SwitchPreference) findPreference(PrefConst.KEY_ENABLE_AUTO_INPUT_CODE);
        autoInputPref.setOnPreferenceChangeListener(this);

        mAutoInputModePref = (ListPreference) findPreference(PrefConst.KEY_AUTO_INPUT_MODE);
        mAutoInputModePref.setOnPreferenceChangeListener(this);
        mCurAutoMode = mAutoInputModePref.getValue();

        ListPreference focusModePref = (ListPreference) findPreference(PrefConst.KEY_FOCUS_MODE);
        focusModePref.setOnPreferenceChangeListener(this);

        refreshEnableAutoInputPreference(autoInputPref.isChecked());
        refreshAutoInputModePreference(mCurAutoMode);
        refreshFocusModePreference(focusModePref, focusModePref.getValue());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (PrefConst.KEY_ENABLE_AUTO_INPUT_CODE.equals(key)) {
            refreshEnableAutoInputPreference((Boolean) newValue);
        } else if (PrefConst.KEY_FOCUS_MODE.equals(key)) {
            refreshFocusModePreference((ListPreference) preference, (String) newValue);
        } else if (PrefConst.KEY_AUTO_INPUT_MODE.equals(key)) {
            if (!newValue.equals(mCurAutoMode)) {
                mCurAutoMode = (String) newValue;
                refreshAutoInputModePreference(mCurAutoMode);
                if (PrefConst.AUTO_INPUT_MODE_ROOT.equals(newValue)) {
                    showRootModePrompt();
                } else if (PrefConst.AUTO_INPUT_MODE_ACCESSIBILITY.equals(mCurAutoMode)) {
                    showAccessibilityModePrompt();
                }
            }
        } else {
            return false;
        }
        return true;
    }

    private void showAccessibilityModePrompt() {
        String serviceId = AccessibilityUtils.getServiceId(SmsCodeAutoInputService.class);
        boolean accessibilityEnabled = AccessibilityUtils.checkAccessibilityEnabled(mContext, serviceId);

        if (!accessibilityEnabled) {
            new MaterialDialog.Builder(mContext)
                    .title(R.string.open_auto_input_accessibility)
                    .content(R.string.open_auto_input_accessibility_prompt)
                    .positiveText(R.string.go_to_open)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            AccessibilityUtils.gotoAccessibility(mContext);
                        }
                    })
                    .show();
        }
    }

    private void showRootModePrompt() {
        new MaterialDialog.Builder(mContext)
                .title(R.string.acquire_root_permission)
                .content(R.string.acquire_root_permission_prompt)
                .positiveText(R.string.okay)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ShellUtils.checkRootPermission();
                    }
                })
                .show();
    }

    private void refreshEnableAutoInputPreference(boolean autoInputEnabled) {
        if (autoInputEnabled && TextUtils.isEmpty(mAutoInputModePref.getValue())) {
            Toast.makeText(getActivity(), R.string.pref_auto_input_mode_summary_default, Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshAutoInputModePreference(String newValue) {
        if (TextUtils.isEmpty(newValue)) {
            mAutoInputModePref.setSummary(R.string.pref_auto_input_mode_summary_default);
            return;
        }
        CharSequence[] entries = mAutoInputModePref.getEntries();
        int index = mAutoInputModePref.findIndexOfValue(newValue);
        try {
            mAutoInputModePref.setSummary(entries[index]);
        } catch (Exception e) {
            // ignore
        }
    }

    private void refreshFocusModePreference(ListPreference focusModePref, String newValue) {
        if (TextUtils.isEmpty(newValue))
            return;
        CharSequence[] entries = focusModePref.getEntries();
        int index = focusModePref.findIndexOfValue(newValue);
        try {
            focusModePref.setSummary(entries[index]);
        } catch (Exception e) {
            //ignore
        }
    }

}
