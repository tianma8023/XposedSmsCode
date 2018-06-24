package com.github.tianma8023.xposed.smscode.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.EditTextPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.github.tianma8023.xposed.smscode.R;

public class ResettableEditPreference extends EditTextPreference {

    private String mDefaultValue;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ResettableEditPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public ResettableEditPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ResettableEditPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ResettableEditPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        setNegativeButtonText(R.string.reset);
    }

    @Override
    public void setDefaultValue(Object value) {
        super.setDefaultValue(value);
        mDefaultValue = (String) value;
        setText(mDefaultValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        Object result = super.onGetDefaultValue(a, index);
        mDefaultValue = (String) result;
        return result;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        String text = getText();
        if (!TextUtils.isEmpty(text)) {
            getEditText().setSelection(text.length());
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_NEGATIVE) {
            onDialogClosed(false);
        } else if (which == DialogInterface.BUTTON_POSITIVE) {
            onDialogClosed(true);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (!positiveResult) {
            if (callChangeListener(mDefaultValue)) {
                setText(mDefaultValue);
            }
        }
    }
}
