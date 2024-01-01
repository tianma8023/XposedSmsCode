package com.tianma.xsmscode.ui.app;

import android.content.res.Resources;

import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.CyaneaResources;
import com.tianma.xsmscode.data.eventbus.MyEventBusIndex;
import com.tianma.xsmscode.feature.migrate.TransitionTask;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;

public class SmsCodeApplication extends DaggerApplication {

    private CyaneaResources mResources = null;

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerApplicationComponent.factory().create(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Cyanea.init(this, super.getResources());
        if (!Cyanea.getInstance().isThemeModified()) {
            Cyanea.getInstance().edit()
                    .baseTheme(Cyanea.BaseTheme.LIGHT)
                    .apply();
        }

        installDefaultEventBus();
        performTransitionTask();
    }

    @Override
    public Resources getResources() {
        if (Cyanea.isInitialized()) {
            if (mResources == null) {
                mResources = new CyaneaResources(super.getResources(), Cyanea.getInstance());
            }
            return mResources;
        }
        return super.getResources();
    }

    private void installDefaultEventBus() {
        EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();
    }

    // data transition task
    private void performTransitionTask() {
        Executor singlePool = Executors.newSingleThreadExecutor();
        singlePool.execute(new TransitionTask(this));
    }

}
