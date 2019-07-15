package com.tianma.xsmscode.ui.app;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        ApplicationModule.class,
        ActivityBindingModule.class,
})
public interface ApplicationComponent extends AndroidInjector<SmsCodeApplication> {

    @Component.Factory
    interface Factory extends AndroidInjector.Factory<SmsCodeApplication> {

    }

}
