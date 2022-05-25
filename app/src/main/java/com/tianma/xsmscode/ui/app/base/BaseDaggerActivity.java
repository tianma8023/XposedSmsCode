package com.tianma.xsmscode.ui.app.base;

import android.os.Bundle;

import androidx.annotation.Nullable;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

/**
 * Base Dagger Activity
 */
public abstract class BaseDaggerActivity extends BaseActivity implements HasAndroidInjector {

    @Inject
    DispatchingAndroidInjector<Object> androidInjector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return androidInjector;
    }

}
