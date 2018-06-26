package com.github.tianma8023.xposed.smscode.app;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.tianma8023.xposed.smscode.R;
import com.github.tianma8023.xposed.smscode.utils.ModuleUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 主界面
 */
public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar mToolbar;

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
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.home_content, new SettingsFragment())
                .commit();
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void showEnableModuleDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.enable_module_title)
                .content(R.string.enable_module_message)
                .positiveText(R.string.i_know)
                .show();
    }

}
