package com.tianma.xsmscode.ui.record;

import android.content.Context;

import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.data.db.DBManager;
import com.tianma.xsmscode.data.db.entity.SmsMsg;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CodeRecordPresenter implements CodeRecordContract.Presenter {

    private CodeRecordContract.View mView;
    private Context mContext;
    private CompositeDisposable mCompositeDisposable;

    @Inject
    public CodeRecordPresenter() {
        mCompositeDisposable = new CompositeDisposable();
    }

    @Inject
    @Override
    public void onAttach(Context context, CodeRecordContract.View view) {
        mContext = context;
        mView = view;
    }

    @Override
    public void onDetach() {
        mView = null;
        if (mCompositeDisposable.size() > 0) {
            mCompositeDisposable.clear();
        }
    }

    @Override
    public void loadData() {
        Disposable disposable = DBManager.get(mContext)
                .queryAllSmsMsgRx()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(dis -> mView.showRefreshing())
                .subscribe(smsMsgList -> {
                    mView.displayData(smsMsgList);
                    mView.stopRefresh();
                }, throwable -> {
                    // ignore
                    XLog.e("", throwable);
                });
        mCompositeDisposable.add(disposable);
    }

    @Override
    public void removeSmsMsg(List<SmsMsg> smsMsgList) {
        Disposable disposable = DBManager.get(mContext)
                .removeSmsMsgListRx(smsMsgList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    // ignore
                }, throwable -> {
                    XLog.e("Error occurs when remove SMS records", throwable);
                });
        mCompositeDisposable.add(disposable);
    }
}
