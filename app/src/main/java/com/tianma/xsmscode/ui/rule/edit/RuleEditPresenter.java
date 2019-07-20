package com.tianma.xsmscode.ui.rule.edit;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.tianma.xsmscode.data.db.DBManager;
import com.tianma.xsmscode.data.db.entity.SmsCodeRule;
import com.tianma.xsmscode.data.eventbus.Event;
import com.tianma.xsmscode.data.eventbus.XEventBus;
import com.tianma.xsmscode.feature.store.EntityStoreManager;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.tianma.xsmscode.ui.rule.edit.RuleEditFragment.EDIT_TYPE_CREATE;
import static com.tianma.xsmscode.ui.rule.edit.RuleEditFragment.EDIT_TYPE_UPDATE;
import static com.tianma.xsmscode.ui.rule.edit.RuleEditFragment.KEY_CODE_RULE;
import static com.tianma.xsmscode.ui.rule.edit.RuleEditFragment.KEY_RULE_EDIT_TYPE;

public class RuleEditPresenter implements RuleEditContract.Presenter {

    private Context mContext;
    private RuleEditContract.View mView;

    private CompositeDisposable mCompositeDisposable;

    private int mRuleEditType;
    private SmsCodeRule mCodeRule;

    @Inject
    RuleEditPresenter() {
        mCompositeDisposable = new CompositeDisposable();
    }

    @Inject
    @Override
    public void onAttach(Context context, RuleEditContract.View view) {
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
    public void handleArguments(Bundle args) {
        if (args == null) {
            return;
        }

        mRuleEditType = args.getInt(KEY_RULE_EDIT_TYPE);
        mCodeRule = args.getParcelable(KEY_CODE_RULE);
        if (mRuleEditType == EDIT_TYPE_UPDATE && mCodeRule != null) {
            mView.displayCodeRule(mCodeRule);
        } else {
            loadTemplate();
        }
    }

    private void loadTemplate() {
        Disposable disposable = Observable
                .fromCallable(() -> {
                    SmsCodeRule template = EntityStoreManager.loadEntityFromFile(
                            EntityStoreManager.EntityType.CODE_RULE_TEMPLATE,
                            SmsCodeRule.class
                    );
                    if (template == null) {
                        template = new SmsCodeRule();
                    }
                    return template;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(codeRule -> {
                    mCodeRule = codeRule;
                    mView.displayCodeRule(mCodeRule);
                });

        mCompositeDisposable.add(disposable);
    }

    @Override
    public void saveAsTemplate(SmsCodeRule template) {
        // clear error info
        mView.clearAllErrorInfo();

        Disposable disposable = Observable
                .fromCallable(() -> EntityStoreManager.storeEntityToFile(
                        EntityStoreManager.EntityType.CODE_RULE_TEMPLATE,
                        template
                ))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> mView.onTemplateSaved(aBoolean));
        mCompositeDisposable.add(disposable);
    }

    @Override
    public void saveIfValid(SmsCodeRule codeRule) {
        if (!checkValid(codeRule)) {
            return;
        }
        mView.hideSoftInput();

        mCodeRule.setCompany(codeRule.getCompany());
        mCodeRule.setCodeKeyword(codeRule.getCodeKeyword());
        mCodeRule.setCodeRegex(codeRule.getCodeRegex());

        Disposable disposable = Observable
                .fromCallable(() -> {
                    DBManager dbManager = DBManager.get(mContext);
                    if (mRuleEditType == EDIT_TYPE_CREATE) {
                        boolean duplicate = dbManager.isExists(mCodeRule);
                        if (duplicate) {
                            return false;
                        } else {
                            long id = dbManager.addSmsCodeRule(mCodeRule);
                            mCodeRule.setId(id);
                        }
                    } else if (mRuleEditType == EDIT_TYPE_UPDATE) {
                        dbManager.updateSmsCodeRule(mCodeRule);
                    }
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    XEventBus.post(new Event.OnRuleCreateOrUpdate(mRuleEditType, mCodeRule));
                    mView.onCodeRuleSaved(success);
                });
        mCompositeDisposable.add(disposable);

    }

    private boolean checkValid(SmsCodeRule codeRule) {
        boolean companyValid = !TextUtils.isEmpty(codeRule.getCompany());
        boolean keywordValid = !TextUtils.isEmpty(codeRule.getCodeKeyword());
        boolean codeRegexValid = !TextUtils.isEmpty(codeRule.getCodeRegex());

        mView.showErrorInfo(companyValid, keywordValid, codeRegexValid);
        return companyValid && keywordValid && codeRegexValid;
    }
}
