package com.tianma.xsmscode.ui.app;

import com.tianma.xsmscode.ui.rule.CodeRulesActivity;
import com.tianma.xsmscode.ui.rule.CodeRulesModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Dagger Module for binding Activity
 */
@Module
public abstract class ActivityBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = CodeRulesModule.class)
    abstract CodeRulesActivity contributeCodeRulesActivity();

    // CodeRecordActivity 其实并没有注入依赖。
    // 所以可以绕开 CodeRecordActivity 而直接对 CodeRecordFragment 进行注入
    // @ActivityScope
    // @ContributesAndroidInjector(modules = CodeRecordActivityModule.class)
    // abstract CodeRecordActivity contributeCodeRecordActivity();
}
