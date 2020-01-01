package com.tianma.xsmscode.ui.rule;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.github.tianma8023.xposed.smscode.R;
import com.tianma.xsmscode.data.eventbus.Event;
import com.tianma.xsmscode.data.eventbus.XEventBus;
import com.tianma.xsmscode.ui.app.base.BaseDaggerActivity;
import com.tianma.xsmscode.ui.rule.edit.RuleEditFragment;
import com.tianma.xsmscode.ui.rule.list.RuleListFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * User custom smscode codeRule list
 */
public class CodeRulesActivity extends BaseDaggerActivity {

    private static final String TAG_RULE_EDIT = "tag_rule_edit";
    private static final String TAG_RULE_LIST = "tag_rule_list";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private FragmentManager mFragmentManager;

    public static void startToMe(Context context) {
        Intent intent = new Intent(context, CodeRulesActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_code_rules);
        ButterKnife.bind(this);

        // set up toolbar
        setupToolbar();

        handleIntent(getIntent());
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        refreshActionBar(getString(R.string.rule_list));
    }

    private void refreshActionBar(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        RuleListFragment ruleListFragment = null;

        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            if (uri != null) {
                // Import rules by back file URI
                ruleListFragment = RuleListFragment.newInstance(uri);
            }
        }

        if (ruleListFragment == null) {
            ruleListFragment = RuleListFragment.newInstance();
        }

        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
                .replace(R.id.code_rules_main_content, ruleListFragment, TAG_RULE_LIST)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        XEventBus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        XEventBus.unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onStartRuleEdit(Event.StartRuleEditEvent event) {
        RuleEditFragment ruleEditFragment = RuleEditFragment.newInstance(event.type, event.codeRule);
        mFragmentManager.beginTransaction()
                .replace(R.id.code_rules_main_content, ruleEditFragment, TAG_RULE_EDIT)
                .addToBackStack(TAG_RULE_EDIT)
                .commit();
        if (event.type == RuleEditFragment.EDIT_TYPE_CREATE) {
            refreshActionBar(getString(R.string.create_rule));
        } else {
            refreshActionBar(getString(R.string.edit_rule));
        }
    }

    @Override
    public void onBackPressed() {
        if (mFragmentManager.getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            mFragmentManager.popBackStackImmediate();
            refreshActionBar(getString(R.string.rule_list));
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
}
