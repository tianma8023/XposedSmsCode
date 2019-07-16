package com.tianma.xsmscode.ui.record;

import com.tianma.xsmscode.common.mvp.BasePresenter;
import com.tianma.xsmscode.common.mvp.BaseView;
import com.tianma.xsmscode.data.db.entity.SmsMsg;

import java.util.List;

interface CodeRecordContract {

    interface View extends BaseView {

        void showRefreshing();

        void stopRefresh();

        void displayData(List<SmsMsg> smsMsgList);

    }

    interface Presenter extends BasePresenter<View> {

        void loadData();

        void removeSmsMsg(List<SmsMsg> smsMsgList);
    }

}
