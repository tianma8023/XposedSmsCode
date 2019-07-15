package com.tianma.xsmscode.common.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.afollestad.materialdialogs.MaterialDialog;

public abstract class DialogAsyncTask<Param, Progress, Result> extends AsyncTask<Param, Progress, Result> implements DialogInterface.OnCancelListener {

    private boolean mCancelable;

    private MaterialDialog mProgressDialog;

    public DialogAsyncTask(Context context, String progressMsg, boolean cancelable) {
        mProgressDialog = new MaterialDialog.Builder(context)
                .content(progressMsg)
                .progress(true, 100)
                .cancelable(mCancelable)
                .build();
        mCancelable = cancelable;
    }

    @Override
    protected void onPreExecute() {
        if (mCancelable) {
            mProgressDialog.setOnCancelListener(this);
        }
        mProgressDialog.show();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        cancel(true);
    }

    @Override
    protected void onPostExecute(Result result) {
        mProgressDialog.dismiss();
    }

    @Override
    protected void onCancelled() {
       mProgressDialog.dismiss();
    }
}
