package com.github.tianma8023.xposed.smscode.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crossbowffs.remotepreferences.RemotePreferences;
import com.github.tianma8023.xposed.smscode.R;
import com.github.tianma8023.xposed.smscode.adapter.BaseItemCallback;
import com.github.tianma8023.xposed.smscode.adapter.ItemCallback;
import com.github.tianma8023.xposed.smscode.app.faq.FaqFragment;
import com.github.tianma8023.xposed.smscode.app.theme.ThemeItem;
import com.github.tianma8023.xposed.smscode.app.theme.ThemeItemAdapter;
import com.github.tianma8023.xposed.smscode.app.theme.ThemeItemContainer;
import com.github.tianma8023.xposed.smscode.constant.PrefConst;
import com.github.tianma8023.xposed.smscode.utils.PackageUtils;
import com.github.tianma8023.xposed.smscode.utils.RemotePreferencesUtils;
import com.github.tianma8023.xposed.smscode.utils.SPUtils;
import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 主界面
 */
public class HomeActivity extends BaseActivity implements SettingsFragment.OnPreferenceClickCallback {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private static final String TAG_NESTED = "tag_nested";
    private static final String TAG_FAQ = "tag_faq";

    private Fragment mCurrentFragment;
    private FragmentManager mFragmentManager;

    private MaterialDialog mThemeChooseDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        initUmengAnalyze();

        handleIntent(getIntent());

        // setup toolbar
        setupToolbar();
    }

    private void initUmengAnalyze() {
        MobclickAgent.openActivityDurationTrack(false);
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);

        refreshActionBar(getString(R.string.app_name));
    }

    private void handleIntent(Intent intent) {
        RemotePreferences preferences =
                RemotePreferencesUtils.getDefaultRemotePreferences(this);
        int themeIdx = SPUtils.getCurrentThemeIndex(preferences);
        ThemeItem themeItem = ThemeItemContainer.get().getItemAt(themeIdx);

        String action = intent.getAction();
        SettingsFragment settingsFragment = null;
        if (Intent.ACTION_VIEW.equals(action)) {
            String extraAction = intent.getStringExtra(SettingsFragment.EXTRA_ACTION);
            if (SettingsFragment.ACTION_GET_RED_PACKET.equals(extraAction)) {
                settingsFragment = SettingsFragment.newInstance(themeItem, extraAction);
            }
        }

        if (settingsFragment == null) {
            settingsFragment = SettingsFragment.newInstance(themeItem);
        }

        settingsFragment.setOnPreferenceClickCallback(this);
        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
                .replace(R.id.home_content, settingsFragment)
                .commit();
        mCurrentFragment = settingsFragment;
    }

    @Override
    public void onPreferenceClicked(String key, String title, boolean nestedPreference) {
        if (nestedPreference) {
            onNestedPreferenceClicked(key, title);
            return;
        }
        if (PrefConst.KEY_CHOOSE_THEME.equals(key)) {
            onChooseThemePreferenceClicked();
        }
    }

    private void onNestedPreferenceClicked(String key, String title) {
//        Fragment newFragment = null;
//        if (PrefConst.KEY_ENTRY_AUTO_INPUT_CODE.equals(key)) {
//            newFragment = new AutoInputSettingsFragment();
//        }
//        if (newFragment == null)
//            return;
//
//        mFragmentManager
//                .beginTransaction()
//                .replace(R.id.home_content, newFragment, TAG_NESTED)
//                .addToBackStack(TAG_NESTED)
//                .commit();
//        mCurrentFragment = newFragment;
//        refreshActionBar(title);
    }

    private ItemCallback<ThemeItem> mThemeItemCallback = new BaseItemCallback<ThemeItem>() {
        @Override
        public void onItemClicked(ThemeItem item, int position) {
            if (mThemeChooseDialog != null && mThemeChooseDialog.isShowing()) {
                mThemeChooseDialog.dismiss();
            }

            RemotePreferences preferences =
                    RemotePreferencesUtils.getDefaultRemotePreferences(HomeActivity.this);

            if (SPUtils.getCurrentThemeIndex(preferences) == position) {
                return;
            }

            SPUtils.setCurrentThemeIndex(preferences, position);

            recreate();
        }
    };

    private void onChooseThemePreferenceClicked() {
        ThemeItemAdapter adapter = new ThemeItemAdapter(this,
                ThemeItemContainer.get().getThemeItemList());
        adapter.setItemCallback(mThemeItemCallback);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        mThemeChooseDialog = new MaterialDialog.Builder(this)
                .title(R.string.pref_choose_theme_title)
                .adapter(adapter, layoutManager)
                .negativeText(R.string.cancel)
                .build();

        RecyclerView recyclerView = mThemeChooseDialog.getRecyclerView();
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));

        mThemeChooseDialog.show();
    }

    private void refreshActionBar(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
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
//            case R.id.action_ignore_battery_optimization:
//                onIgnoreBatteryOptimizationSelected();
//                return true;
            case R.id.action_taichi_users_notice:
                onTaichiUsersNoticeSelected();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        MenuItem faqItem = menu.findItem(R.id.action_home_faq);
        if (mCurrentFragment instanceof FaqFragment) {
            faqItem.setVisible(false);
        } else {
            faqItem.setVisible(true);
        }

//        MenuItem ignoreOptimizeItem = menu.findItem(R.id.action_ignore_battery_optimization);
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            ignoreOptimizeItem.setVisible(false);
//        } else {
//            ignoreOptimizeItem.setVisible(true);
//        }
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

//    private void onIgnoreBatteryOptimizationSelected() {
//        new MaterialDialog.Builder(this)
//                .title(R.string.ignore_battery_optimization_statement)
//                .content(R.string.ignore_battery_optimization_content)
//                .positiveText(R.string.yes)
//                .onPositive(new MaterialDialog.SingleButtonCallback() {
//                    @Override
//                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        ignoreBatteryOptimization();
//                    }
//                })
//                .negativeText(R.string.no)
//                .show();
//    }

//    @SuppressLint("BatteryLife")
//    private void ignoreBatteryOptimization() {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            return;
//        }
//
//        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
//        if (pm == null) {
//            return;
//        }
//
//        if (pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)) {
//            Toast.makeText(this, R.string.battery_optimization_ignored, Toast.LENGTH_LONG).show();
//        } else {
//            try {
//                // 申请忽略电源优化
//                SettingsUtils.requestIgnoreBatteryOptimization(this);
//            } catch (Exception e) {
//                try {
//                    // 跳转至电源优化界面
//                    SettingsUtils.gotoIgnoreBatteryOptimizationSettings(this);
//                    Toast.makeText(this, R.string.ignore_battery_optimization_manually, Toast.LENGTH_LONG).show();
//                } catch (Exception e1) {
//                    Toast.makeText(this, R.string.ignore_battery_optimization_settings_failed, Toast.LENGTH_LONG).show();
//                }
//            }
//        }
//    }

    void onTaichiUsersNoticeSelected() {
        new MaterialDialog.Builder(this)
                .title(R.string.taichi_users_notice)
                .content(R.string.taichi_users_notice_content)
                .negativeText(R.string.add_apps_in_taichi)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PackageUtils.startAddAppsInTaiChi(HomeActivity.this);
                    }
                })
                .positiveText(R.string.check_module_in_taichi)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PackageUtils.startCheckModuleInTaiChi(HomeActivity.this);
                    }
                })
                .show();
    }
}
