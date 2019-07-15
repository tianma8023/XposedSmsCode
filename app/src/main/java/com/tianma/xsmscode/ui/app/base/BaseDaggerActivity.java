package com.tianma.xsmscode.ui.app.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tianma.xsmscode.common.utils.SPUtils;
import com.tianma.xsmscode.ui.theme.ThemeItemContainer;

import dagger.android.support.DaggerAppCompatActivity;

public abstract class BaseDaggerActivity extends DaggerAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTheme();
    }

    private void initTheme() {
        int index = SPUtils.getCurrentThemeIndex(this);
        setTheme(ThemeItemContainer.get().getItemAt(index).getThemeRes());
    }

}
