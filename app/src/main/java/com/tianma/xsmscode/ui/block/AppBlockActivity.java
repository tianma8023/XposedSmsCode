package com.tianma.xsmscode.ui.block;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.github.tianma8023.xposed.smscode.R;
import com.tianma.xsmscode.ui.app.base.BaseActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity for choosing apps where auto-input is banned.
 */
public class AppBlockActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    public static void startMe(Context context) {
        context.startActivity(new Intent(context, AppBlockActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_block);
        ButterKnife.bind(this);

        setupToolbar();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.app_block_main_content, AppBlockFragment.newInstance())
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
}
