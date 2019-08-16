package com.tianma.xsmscode.ui.app.base;

import android.os.Bundle;

import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;
import com.tianma.xsmscode.common.utils.SPUtils;
import com.tianma.xsmscode.ui.theme.ThemeItemContainer;

import androidx.annotation.Nullable;

public abstract class BaseActivity extends CyaneaAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initTheme();
    }

    private void initTheme() {
        int index = SPUtils.getCurrentThemeIndex(this);
        setTheme(ThemeItemContainer.get().getItemAt(index).getThemeRes());
    }
}
