package com.tianma.xsmscode.ui.rule.list;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.data.db.DBManager;
import com.tianma.xsmscode.data.db.entity.SmsCodeRule;
import com.tianma.xsmscode.feature.backup.BackupManager;
import com.tianma.xsmscode.feature.backup.ExportResult;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.tianma.xsmscode.ui.rule.list.RuleListFragment.EXTRA_IMPORT_URI;

class RuleListPresenter implements RuleListContract.Presenter {
    private RuleListContract.View mView;
    private Context mContext;

    private CompositeDisposable mCompositeDisposable;

    @Inject
    public RuleListPresenter() {
        mCompositeDisposable = new CompositeDisposable();
    }

    @Inject
    @Override
    public void onAttach(Context context, RuleListContract.View view) {
        mView = view;
        mContext = context;

        XLog.d("View: %s", mView == null ? "null" : mView.toString());
        XLog.d("Context: %s", context == null ? "null" : mContext.toString());
    }

    @Override
    public void onDetach() {
        mView = null;
        if (mCompositeDisposable.size() > 0) {
            mCompositeDisposable.clear();
        }
    }

    @Override
    public void loadAllRules() {
        Disposable disposable = DBManager.get(mContext)
                .queryAllSmsCodeRulesRx()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(smsCodeRules -> mView.displayRules(smsCodeRules));
        mCompositeDisposable.add(disposable);
    }

    @Override
    public void handleArguments(Bundle args) {
        if (args == null) {
            return;
        }

        final Uri importUri = args.getParcelable(EXTRA_IMPORT_URI);
        if (importUri != null) {
            args.remove(EXTRA_IMPORT_URI);

            if (ContentResolver.SCHEME_FILE.equals(importUri.getScheme())) {
                // file:// URI need storage permission
                mView.attemptImportRuleListDirectly(importUri);
            } else {
                // content:// URI don't need storage permission
                mView.showImportDialogConfirm(importUri);
            }
        }
    }

    @Override
    public void removeRule(SmsCodeRule codeRule) {
        Disposable disposable = DBManager.get(mContext)
                .removeSmsCodeRuleRx(codeRule)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                }, throwable -> XLog.e("Remove " + codeRule.toString() + " failed", throwable));
        mCompositeDisposable.add(disposable);
    }

    @Override
    public void exportRules(List<SmsCodeRule> rules, File file, String progressMsg) {
        Disposable disposable = Observable
                .fromCallable(() -> BackupManager.exportRuleList(file, rules))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable1 -> mView.showProgress(progressMsg))
                .subscribe(exportResult -> {
                    mView.onExportCompleted(exportResult == ExportResult.SUCCESS, file);
                    mView.cancelProgress();
                });
        mCompositeDisposable.add(disposable);
    }

    @Override
    public void importRules(Uri uri, boolean retain, String progressMsg) {
        Disposable disposable = Observable
                .fromCallable(() -> BackupManager.importRuleList(mContext, uri, retain))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(d -> mView.showProgress(progressMsg))
                .subscribe(importResult -> {
                    mView.onImportComplete(importResult);
                    mView.cancelProgress();
                });
        mCompositeDisposable.add(disposable);
    }


}
