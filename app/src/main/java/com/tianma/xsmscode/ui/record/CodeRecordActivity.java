package com.tianma.xsmscode.ui.record;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.github.tianma8023.xposed.smscode.R;
import com.tianma.xsmscode.common.fragment.backpress.BackPressEventDispatchHelper;
import com.tianma.xsmscode.ui.app.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Sms Code Records
 */
public class CodeRecordActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    public static void startToMe(Context context) {
        Intent intent = new Intent(context, CodeRecordActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_records);
        ButterKnife.bind(this);

        setupToolbar();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.code_records_main_content, CodeRecordFragment.newInstance())
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!BackPressEventDispatchHelper.dispatchBackPressedEvent(this)) {
            super.onBackPressed();
        }
    }
}
