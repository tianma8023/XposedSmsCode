package com.tianma.xsmscode.ui.rule.list;

import com.tianma.xsmscode.ui.app.FragmentScope;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class RuleListModule {

    @FragmentScope
    @Binds
    abstract RuleListContract.View bindView(RuleListFragment view);

    @FragmentScope
    @Binds
    abstract RuleListContract.Presenter bindPresenter(RuleListPresenter presenter);

}
