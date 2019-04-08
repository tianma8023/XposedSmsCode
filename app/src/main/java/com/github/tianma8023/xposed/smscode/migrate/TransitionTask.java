package com.github.tianma8023.xposed.smscode.migrate;

import android.content.Context;

import com.github.tianma8023.xposed.smscode.migrate.db.DBTransition;

import java.util.ArrayList;
import java.util.List;

/**
 * 执行数据迁移过渡的Task
 */
public class TransitionTask implements Runnable {

    private List<ITransition> mTransitionList;

    public TransitionTask(Context context) {
        init(context);
    }

    private void init(Context context) {
        mTransitionList = new ArrayList<>();
//        mTransitionList.add(new PreferencesTransition(context));
        mTransitionList.add(new DBTransition(context));
    }

    @Override
    public void run() {
        for (ITransition transition : mTransitionList) {
            if (transition.shouldTransit()) {
                transition.doTransition();
            }
        }
    }

}
