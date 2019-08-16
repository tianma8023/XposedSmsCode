package com.tianma.xsmscode.ui.app.base;

import android.os.Bundle;

import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;
import com.tianma.xsmscode.common.utils.SPUtils;
import com.tianma.xsmscode.ui.theme.ThemeItemContainer;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

public abstract class BaseDaggerActivity extends CyaneaAppCompatActivity implements HasAndroidInjector {

    @Inject
    DispatchingAndroidInjector<Object> androidInjector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        // initTheme();
    }

    private void initTheme() {
        int index = SPUtils.getCurrentThemeIndex(this);
        setTheme(ThemeItemContainer.get().getItemAt(index).getThemeRes());
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return androidInjector;
    }

}
