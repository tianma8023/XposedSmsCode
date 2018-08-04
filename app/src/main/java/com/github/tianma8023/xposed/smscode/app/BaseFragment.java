package com.github.tianma8023.xposed.smscode.app;

import android.app.Fragment;

import com.umeng.analytics.MobclickAgent;

public abstract class BaseFragment extends Fragment implements IFragmentMobclick {

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

}
