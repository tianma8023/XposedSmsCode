package com.github.tianma8023.xposed.smscode.app;

import android.preference.PreferenceFragment;

import com.umeng.analytics.MobclickAgent;

public abstract class BasePreferenceFragment extends PreferenceFragment {

    @Override
    public void onResume() {
        super.onResume();

        MobclickAgent.onPageStart(getPageName());
    }

    @Override
    public void onPause() {
        super.onPause();

        MobclickAgent.onPageEnd(getPageName());
    }

    protected abstract String getPageName();
}
