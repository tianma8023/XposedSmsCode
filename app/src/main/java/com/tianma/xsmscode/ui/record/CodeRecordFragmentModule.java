package com.tianma.xsmscode.ui.record;

import com.tianma.xsmscode.ui.app.FragmentScope;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class CodeRecordFragmentModule {

    @FragmentScope
    @Binds
    public abstract CodeRecordContract.View bindView(CodeRecordFragment view);

    @FragmentScope
    @Binds
    public abstract CodeRecordContract.Presenter bindPresenter(CodeRecordPresenter presenter);

}
