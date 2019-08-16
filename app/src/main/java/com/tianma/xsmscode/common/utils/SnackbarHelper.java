package com.tianma.xsmscode.common.utils;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class SnackbarHelper {

    @NonNull
    public static Snackbar makeShort(@NonNull View view, @StringRes int resId) {
        return make(view, resId, Snackbar.LENGTH_SHORT);
    }

    @NonNull
    public static Snackbar makeShort(@NonNull View view, @NonNull CharSequence text) {
        return make(view, text, Snackbar.LENGTH_SHORT);
    }

    @NonNull
    public static Snackbar makeLong(@NonNull View view, @StringRes int resId) {
        return make(view, resId, Snackbar.LENGTH_LONG);
    }

    @NonNull
    public static Snackbar makeLong(@NonNull View view, @NonNull CharSequence text) {
        return make(view, text, Snackbar.LENGTH_LONG);
    }

    @NonNull
    private static Snackbar make(@NonNull View view, @StringRes int resId, int duration) {
        return make(view, view.getResources().getText(resId), duration);
    }

    @NonNull
    private static Snackbar make(@NonNull View view, @NonNull CharSequence text, int duration) {
        // View snackView = snackbar.getView();
        // TypedValue typedValue = new TypedValue();
        // Context context = view.getContext();
        // context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        // @ColorInt int colorPrimary = typedValue.data;
        // colorPrimary = ColorUtils.gradientColor(colorPrimary, 1.2f);
        // snackView.setBackgroundColor(colorPrimary);
        return Snackbar.make(view, text, duration);
    }

}
