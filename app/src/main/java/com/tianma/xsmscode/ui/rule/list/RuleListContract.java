package com.tianma.xsmscode.ui.rule.list;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.tianma.xsmscode.common.mvp.BasePresenter;
import com.tianma.xsmscode.common.mvp.BaseView;
import com.tianma.xsmscode.data.db.entity.SmsCodeRule;
import com.tianma.xsmscode.feature.backup.ImportResult;

import java.io.File;
import java.util.List;

interface RuleListContract {

    interface View extends BaseView {

        void displayRules(List<SmsCodeRule> rules);

        void attemptImportRuleListDirectly(Uri importUri);

        void showImportDialogConfirm(Uri importUri);

        void showProgress(String progressMsg);

        void cancelProgress();

        void onExportCompletedBelowQ(boolean success, File file);

        void onExportCompletedAboveQ(boolean success);

        void onImportComplete(ImportResult importResult);
    }

    interface Presenter extends BasePresenter<View> {

        void loadAllRules();

        void handleArguments(Bundle args);

        void removeRule(SmsCodeRule codeRule);

        void exportRulesBelowQ(List<SmsCodeRule> rules, File file, String progressMsg);

        void exportRulesAboveQ(List<SmsCodeRule> rules, Context context, Uri uri, String progressMsg);

        void importRules(Uri uri, boolean retain, String progressMsg);

        void saveRulesToFile(List<SmsCodeRule> rules);
    }

}
