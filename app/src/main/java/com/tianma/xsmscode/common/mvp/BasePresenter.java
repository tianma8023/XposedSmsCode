package com.tianma.xsmscode.common.mvp;

import android.content.Context;

/**
 * Base Presenter
 *
 * @param <T> View extends BaseView
 */
public interface BasePresenter<T extends BaseView> {

    /**
     * on view attach
     *
     * @param context context
     * @param view    view
     */
    void onAttach(Context context, T view);

    /**
     * on view detach
     */
    void onDetach();

}
