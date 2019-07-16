package com.tianma.xsmscode.ui.app;

import com.tianma.xsmscode.ui.record.CodeRecordFragment;
import com.tianma.xsmscode.ui.record.CodeRecordFragmentModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Dagger Module for binding Fragment.
 */
@Module
public abstract class FragmentBindingModule {


    // CodeRecordActivity 其实并没有注入依赖。
    // 所以可以绕开 CodeRecordActivity 而直接对 CodeRecordFragment 进行注入
    @FragmentScope
    @ContributesAndroidInjector(modules = CodeRecordFragmentModule.class)
    abstract CodeRecordFragment contributeCodeRecordFragment();

}
