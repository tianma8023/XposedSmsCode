package com.tianma.xsmscode.ui.block;

import com.tianma.xsmscode.common.mvp.BasePresenter;
import com.tianma.xsmscode.common.mvp.BaseView;
import com.tianma.xsmscode.data.db.entity.AppInfo;

import java.util.List;

interface AppBlockContract {

    interface View extends BaseView {

        void showData(List<AppInfo> appInfoList);

        void showError(Throwable t);

        void showProgress();

        void cancelProgress();

        void onSaveSuccess();

        void onSaveFailed();
    }

    interface Presenter extends BasePresenter<View> {

        void refreshData();

        void doFilter(String filter);

        void doSort(SortType sortType);

        void doItemClicked(AppInfo item);

        void saveData();
    }

}
