package com.github.tianma8023.xposed.smscode.app;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.tianma8023.xposed.smscode.R;
import com.github.tianma8023.xposed.smscode.constant.IPrefConstants;
import com.github.tianma8023.xposed.smscode.utils.ModuleUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 主界面
 */
public class HomeActivity extends BaseActivity implements SettingsFragment.OnNestedPreferenceClickListener {

    @BindView(R.id.toolbar) Toolbar mToolbar;

    private static final String TAG_NESTED = "tag_nested";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        // setup toolbar
        setupToolbar();

        if (!ModuleUtils.isModuleEnabled()) {
            showEnableModuleDialog();
        }
        // init main fragment
        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.registerOnNestedPreferenceClickListener(this);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.home_content, settingsFragment)
                .commit();
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);

        refreshActionBar(getString(R.string.app_name), false);
    }

    private void showEnableModuleDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.enable_module_title)
                .content(R.string.enable_module_message)
                .positiveText(R.string.i_know)
                .show();
    }

    @Override
    public void onNestedPreferenceClicked(String key, String title) {
        Fragment newFragment = null;
        if (IPrefConstants.KEY_ENTRY_AUTO_INPUT_CODE.equals(key)) {
            newFragment = new AutoInputSettingsFragment();
        }
        if (newFragment == null)
            return;

        refreshActionBar(title, true);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.home_content, newFragment, TAG_NESTED)
                .addToBackStack(TAG_NESTED)
                .commit();
    }

    private void refreshActionBar(String title, boolean displayHomeAsUpEnabled) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(displayHomeAsUpEnabled);
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
            if (getFragmentManager().getBackStackEntryCount() == 1) {
                refreshActionBar(getString(R.string.app_name), false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
