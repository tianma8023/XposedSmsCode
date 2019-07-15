package com.tianma.xsmscode.ui.app;

import com.tianma.xsmscode.ui.rule.CodeRulesActivity;
import com.tianma.xsmscode.ui.rule.CodeRulesModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = CodeRulesModule.class)
    abstract CodeRulesActivity contributeCodeRulesActivity();

}
