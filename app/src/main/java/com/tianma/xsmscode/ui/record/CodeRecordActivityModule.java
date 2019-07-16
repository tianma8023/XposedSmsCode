package com.tianma.xsmscode.ui.record;

import com.tianma.xsmscode.ui.app.FragmentScope;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class CodeRecordActivityModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = CodeRecordFragmentModule.class)
    abstract CodeRecordFragment contributeCodeRecordFragment();

}
