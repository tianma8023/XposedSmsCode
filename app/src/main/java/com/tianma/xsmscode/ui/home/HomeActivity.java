package com.tianma.xsmscode.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.tianma8023.xposed.smscode.R;
import com.tianma.xsmscode.common.adapter.BaseItemCallback;
import com.tianma.xsmscode.common.adapter.ItemCallback;
import com.tianma.xsmscode.common.constant.PrefConst;
import com.tianma.xsmscode.common.utils.PackageUtils;
import com.tianma.xsmscode.common.utils.SPUtils;
import com.tianma.xsmscode.ui.app.base.BaseActivity;
import com.tianma.xsmscode.ui.faq.FaqFragment;
import com.tianma.xsmscode.ui.theme.ThemeItem;
import com.tianma.xsmscode.ui.theme.ThemeItemAdapter;
import com.tianma.xsmscode.ui.theme.ThemeItemContainer;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

        handleIntent(getIntent());

        // setup toolbar
        setupToolbar();
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);

        refreshActionBar(getString(R.string.app_name));
    }

    private void handleIntent(Intent intent) {
        int themeIdx = SPUtils.getCurrentThemeIndex(this);
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
        if (PrefConst.KEY_CHOOSE_THEME.equals(key)) {
            onChooseThemePreferenceClicked();
        }
    }

    private ItemCallback<ThemeItem> mThemeItemCallback = new BaseItemCallback<ThemeItem>() {
        @Override
        public void onItemClicked(ThemeItem item, int position) {
            if (mThemeChooseDialog != null && mThemeChooseDialog.isShowing()) {
                mThemeChooseDialog.dismiss();
            }

            if (SPUtils.getCurrentThemeIndex(HomeActivity.this) == position) {
                return;
            }

            SPUtils.setCurrentThemeIndex(HomeActivity.this, position);

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

    void onTaichiUsersNoticeSelected() {
        new MaterialDialog.Builder(this)
                .title(R.string.taichi_users_notice)
                .content(R.string.taichi_users_notice_content)
                .negativeText(R.string.add_apps_in_taichi)
                .onNegative((dialog, which) -> PackageUtils.startAddAppsInTaiChi(HomeActivity.this))
                .positiveText(R.string.check_module_in_taichi)
                .onPositive((dialog, which) -> PackageUtils.startCheckModuleInTaiChi(HomeActivity.this))
                .show();
    }
}
