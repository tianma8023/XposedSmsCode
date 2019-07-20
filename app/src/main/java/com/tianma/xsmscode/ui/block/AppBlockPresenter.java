package com.tianma.xsmscode.ui.block;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;

import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.data.db.DBManager;
import com.tianma.xsmscode.data.db.entity.AppInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

class AppBlockPresenter implements AppBlockContract.Presenter {

    private AppBlockContract.View mView;
    private Context mContext;

    private CompositeDisposable mCompositeDisposable;

    private boolean mLoadSucceed = false;

    private List<AppInfo> mOriginalBlockedApps;
    private List<AppInfo> mApps;

    private String mFilter = "";
    private SortType mSortType = SortType.LABEL_ASC;

    @Inject
    public AppBlockPresenter() {
        mCompositeDisposable = new CompositeDisposable();
        mOriginalBlockedApps = new ArrayList<>();
        mApps = new ArrayList<>();
    }

    @Inject
    @Override
    public void onAttach(Context context, AppBlockContract.View view) {
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
    public void refreshData() {
        if (mLoadSucceed) {
            return;
        }

        PackageManager pm = mContext.getPackageManager();
        Disposable disposable = Observable
                .fromIterable(() -> {
                    mOriginalBlockedApps = DBManager.get(mContext).queryAllBlockedApps();
                    return pm.getInstalledApplications(0).iterator();
                })
                .map(applicationInfo -> {
                    AppInfo appInfo = AppInfoUtils.getAppInfo(pm, applicationInfo);
                    if (mOriginalBlockedApps.contains(appInfo)) {
                        appInfo.setBlocked(true);
                    }
                    return appInfo;
                })
                .sorted(mComparator)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable1 -> mView.showProgress())
                .subscribe(appInfoList -> {
                    mApps = appInfoList;
                    mView.cancelProgress();
                    mView.showData(new ArrayList<>(appInfoList));
                    mLoadSucceed = true;
                }, throwable -> {
                    XLog.e("", throwable);
                    mView.cancelProgress();
                    mView.showError(throwable);
                    mLoadSucceed = false;
                });
        mCompositeDisposable.add(disposable);
    }

    @SuppressLint("CheckResult")
    @Override
    public void doFilter(String filter) {
        sortWithFilter(mSortType, filter);
    }

    @Override
    public void doSort(SortType sortType) {
        sortWithFilter(sortType, mFilter);
    }

    private void sortWithFilter(SortType sortType, String filter) {
        filter = filter.toLowerCase();
        if (mSortType == sortType && mFilter.equals(filter)) {
            return;
        }
        mSortType = sortType;
        mFilter = filter;
        Disposable disposable = Observable.fromIterable(mApps)
                .filter(appInfo -> {
                    if (appInfo != null) { // case insensitive
                        return appInfo.getLabel().toLowerCase().contains(mFilter) ||
                                appInfo.getPackageName().toLowerCase().contains(mFilter);
                    }
                    return false;
                })
                .sorted(mComparator)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(appInfoList -> {
                    mView.showData(new ArrayList<>(appInfoList));
                }, throwable -> {
                    // ignore
                    XLog.e("", throwable);
                });
        mCompositeDisposable.add(disposable);
    }

    @SuppressLint("CheckResult")
    @Override
    public void doItemClicked(AppInfo item) {
        for (AppInfo appInfo : mApps) {
            if (appInfo != null && appInfo.getPackageName().equals(item.getPackageName())) {
                appInfo.setBlocked(!appInfo.isBlocked());
                break;
            }
        }
    }

    @Override
    public void saveData() {
        if (mApps.isEmpty()) {
            // 处理快速按下保存的问题
            mView.onSaveSuccess();
            return;
        }

        List<AppInfo> blockedApps = new ArrayList<>();
        for (AppInfo appInfo : mApps) {
            if (appInfo != null && appInfo.isBlocked()) {
                blockedApps.add(appInfo);
            }
        }

        if (blockedApps.equals(mOriginalBlockedApps)) { // 啥都没干
            mView.onSaveSuccess();
            return;
        }

        Disposable disposable = Observable
                .fromCallable(() -> {
                    // save to database
                    DBManager dbManager = DBManager.get(mContext);
                    dbManager.deleteAll(AppInfo.class);
                    dbManager.insertOrReplaceInTx(AppInfo.class, blockedApps);
                    // save to file
                    BlockedAppStoreManager.storeEntitiesToFile(
                            BlockedAppStoreManager.EntityType.BLOCKED_APP, blockedApps);
                    return blockedApps;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(appInfoList -> {
                    mView.onSaveSuccess();
                }, throwable -> mView.onSaveFailed());
        mCompositeDisposable.add(disposable);

    }

    private Comparator<AppInfo> mComparator = new Comparator<AppInfo>() {
        @Override
        public int compare(AppInfo o1, AppInfo o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            int result = Boolean.compare(o2.isBlocked(), o1.isBlocked());
            if (result == 0) {
                switch (mSortType) {
                    case LABEL_ASC:
                        result = compareString(o1.getLabel(), o2.getLabel());
                        break;
                    case PACKAGE_ASC:
                        result = compareString(o1.getPackageName(), o2.getPackageName());
                        break;
                    case LABEL_DESC:
                        result = compareString(o2.getLabel(), o1.getLabel());
                        break;
                    case PACKAGE_DESC:
                        result = compareString(o2.getPackageName(), o1.getPackageName());
                        break;
                }
            }
            return result;
        }

        private int compareString(String s1, String s2) {
            if (s1 == null && s2 == null) {
                return 0;
            }
            if (s1 == null) {
                return -1;
            }
            if (s2 == null) {
                return 1;
            }
            return s1.compareToIgnoreCase(s2);
        }
    };


}
