package com.github.tianma8023.xposed.smscode.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.tianma8023.xposed.smscode.R;
import com.github.tianma8023.xposed.smscode.app.faq.FaqFragment;
import com.github.tianma8023.xposed.smscode.constant.IPrefConstants;
import com.github.tianma8023.xposed.smscode.utils.ModuleUtils;
import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 主界面
 */
public class HomeActivity extends BaseActivity implements SettingsFragment.OnNestedPreferenceClickListener {

    @BindView(R.id.toolbar) Toolbar mToolbar;

    private static final String TAG_NESTED = "tag_nested";
    private static final String TAG_FAQ = "tag_faq";

    private Fragment mCurrentFragment;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        // init main fragment
        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.registerOnNestedPreferenceClickListener(this);
        mFragmentManager = getFragmentManager();
        mFragmentManager.beginTransaction()
                .replace(R.id.home_content, settingsFragment)
                .commit();
        mCurrentFragment = settingsFragment;

        // setup toolbar
        setupToolbar();

        if (!ModuleUtils.isModuleEnabled()) {
            showEnableModuleDialog();
        }

        initUmengAnalyze();
    }

    private void initUmengAnalyze() {
        MobclickAgent.openActivityDurationTrack(false);
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);

        refreshActionBar(getString(R.string.app_name));
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

        mFragmentManager
                .beginTransaction()
                .replace(R.id.home_content, newFragment, TAG_NESTED)
                .addToBackStack(TAG_NESTED)
                .commit();
        mCurrentFragment = newFragment;
        refreshActionBar(title);
    }

    private void refreshActionBar(String title) {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setHomeButtonEnabled(true);
            if (mCurrentFragment instanceof SettingsFragment) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mFragmentManager.getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            mFragmentManager.popBackStackImmediate();
            mCurrentFragment = mFragmentManager.findFragmentById(R.id.home_content);
            refreshActionBar(getString(R.string.app_name));
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_home_faq:
                onFAQSelected();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        MenuItem faqItem = menu.findItem(R.id.action_home_faq);
        if (mCurrentFragment instanceof FaqFragment) {
            faqItem.setVisible(false);
        } else {
            faqItem.setVisible(true);
        }
        return true;
    }

    private void onFAQSelected() {
        FaqFragment faqFragment = FaqFragment.newInstance();
        mFragmentManager
                .beginTransaction()
                .replace(R.id.home_content, faqFragment, TAG_FAQ)
                .addToBackStack(TAG_FAQ)
                .commit();
        mCurrentFragment = faqFragment;
        refreshActionBar(getString(R.string.action_home_faq_title));
        invalidateOptionsMenu();
    }
}
