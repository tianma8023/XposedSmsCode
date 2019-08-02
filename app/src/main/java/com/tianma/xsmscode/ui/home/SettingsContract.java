package com.tianma.xsmscode.ui.home;

import android.os.Bundle;

import com.tianma.xsmscode.common.mvp.BasePresenter;
import com.tianma.xsmscode.common.mvp.BaseView;
import com.tianma.xsmscode.data.db.entity.ApkVersion;

public interface SettingsContract {

    interface View extends BaseView {

        void showGetAlipayPacketDialog();

        void showSmsCodeTestResult(String code);

        void showCheckError(Throwable t);

        void showUpdateDialog(ApkVersion latestVersion);

        void showEnableModuleDialog();

        void showAppAlreadyNewest();
    }

    interface Presenter extends BasePresenter<View> {

        void handleArguments(Bundle args);

        void setPreferenceWorldWritable(String preferencesName);

        void hideOrShowLauncherIcon(boolean hide);

        void performSmsCodeTest(String msgBody);

        void joinQQGroup();

        void showSourceProject();

        void setInternalFilesWritable();

        void checkUpdate();

        void updateFromGithub();

        void updateFromCoolApk();
    }

}
