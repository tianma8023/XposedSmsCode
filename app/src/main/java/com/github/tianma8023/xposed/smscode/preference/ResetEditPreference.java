package com.github.tianma8023.xposed.smscode.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v7.preference.EditTextPreference;
import android.util.AttributeSet;

import com.github.tianma8023.xposed.smscode.R;

public class ResetEditPreference extends EditTextPreference {

    private String mDefaultValue;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ResetEditPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public ResetEditPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ResetEditPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ResetEditPreference(Context context) {
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
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        Object result = super.onGetDefaultValue(a, index);
        mDefaultValue = (String) result;
        return result;
    }

    public String getDefaultValue() {
        return mDefaultValue;
    }
}
