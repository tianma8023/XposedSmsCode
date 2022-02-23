package com.tianma.xsmscode.ui.home;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.tianma.xsmscode.common.constant.Const;
import com.tianma.xsmscode.common.utils.ModuleUtils;
import com.tianma.xsmscode.common.utils.PackageUtils;
import com.tianma.xsmscode.common.utils.SmsCodeUtils;
import com.tianma.xsmscode.common.utils.StorageUtils;
import com.tianma.xsmscode.common.utils.Utils;
import com.tianma.xsmscode.data.db.entity.ApkVersion;
import com.tianma.xsmscode.data.repository.DataRepository;

import java.io.File;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.tianma.xsmscode.ui.home.SettingsFragment.ACTION_DONATE_BY_ALIPAY;
import static com.tianma.xsmscode.ui.home.SettingsFragment.EXTRA_ACTION;

public class SettingsPresenter implements SettingsContract.Presenter {

    private SettingsContract.View mView;
    private Context mContext;

    private CompositeDisposable mCompositeDisposable;

    @Inject
    SettingsPresenter() {
        mCompositeDisposable = new CompositeDisposable();
    }

    @Inject
    @Override
    public void onAttach(Context context, SettingsContract.View view) {
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
        String extraAction = args.getString(EXTRA_ACTION);
        if (ACTION_DONATE_BY_ALIPAY.equals(extraAction)) {
            args.remove(EXTRA_ACTION);
            mView.showGetAlipayPacketDialog();
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                mView.updateUIByModuleStatus(ModuleUtils.isModuleEnabled());
            }, 50L);
        }
    }

    @Override
    public void setPreferenceWorldWritable(String preferencesName) {
        // dataDir: /data/data/<package_name>/
        // spDir: /data/data/<package_name>/shared_prefs/
        // spFile: /data/data/<package_name>/shared_prefs/<preferences_name>.xml
        File prefsFile = StorageUtils.getSharedPreferencesFile(mContext, preferencesName);
        StorageUtils.setFileWorldWritable(prefsFile, 2);
    }

    @Override
    public void hideOrShowLauncherIcon(boolean hide) {
        PackageManager pm = mContext.getPackageManager();
        ComponentName launcherCN = new ComponentName(mContext, Const.HOME_ACTIVITY_ALIAS);
        int state = hide ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        if (pm.getComponentEnabledSetting(launcherCN) != state) {
            pm.setComponentEnabledSetting(launcherCN, state, PackageManager.DONT_KILL_APP);
        }
    }

    @Override
    public void performSmsCodeTest(String msgBody) {
        Disposable disposable = Observable
                .create((ObservableOnSubscribe<String>) emitter -> {
                    String code = TextUtils.isEmpty(msgBody) ? "" :
                            SmsCodeUtils.parseSmsCodeIfExists(mContext, msgBody, false);
                    emitter.onNext(code);
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(code -> mView.showSmsCodeTestResult(code),
                        throwable -> mView.showSmsCodeTestResult(""));
        mCompositeDisposable.add(disposable);
    }

    @Override
    public void joinQQGroup() {
        PackageUtils.joinQQGroup(mContext);
    }

    @Override
    public void showSourceProject() {
        Utils.showWebPage(mContext, Const.PROJECT_SOURCE_CODE_URL);
    }

    @Override
    public void setInternalFilesWritable() {
        // dataDir or external dataDir
        // filesDir or external filesDir
        StorageUtils.setFileWorldWritable(StorageUtils.getFilesDir(), 1);
    }

    @Override
    public void checkUpdate() {
        Disposable disposable = DataRepository.getLatestVersion()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(latestVersion -> {
                            ApkVersion currentVersion = new ApkVersion(BuildConfig.VERSION_NAME, "");
                            if (currentVersion.getVersionValue() < latestVersion.getVersionValue()) {
                                mView.showUpdateDialog(latestVersion);
                            } else {
                                mView.showAppAlreadyNewest();
                            }
                        },
                        throwable -> mView.showCheckError(throwable)
                );
        mCompositeDisposable.add(disposable);
    }

    @Override
    public void updateFromGithub() {
        Utils.showWebPage(mContext, Const.PROJECT_GITHUB_LATEST_RELEASE_URL);
    }

    @Override
    public void updateFromCoolApk() {
        PackageUtils.showAppDetailsInCoolApk(mContext);
    }
}
