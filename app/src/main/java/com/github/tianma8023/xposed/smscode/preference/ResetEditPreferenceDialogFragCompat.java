package com.github.tianma8023.xposed.smscode.preference;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.EditText;

public class ResetEditPreferenceDialogFragCompat extends PreferenceDialogFragmentCompat {

    private static final String SAVE_STATE_TEXT = "ResetEditPreferenceDialogFragCompat.text";
    private EditText mEditText;
    private CharSequence mText;
    private int mWhichClicked;

    public ResetEditPreferenceDialogFragCompat() {
        mWhichClicked = DialogInterface.BUTTON_NEUTRAL;
    }

    public static ResetEditPreferenceDialogFragCompat newInstance(String key) {
        ResetEditPreferenceDialogFragCompat fragment = new ResetEditPreferenceDialogFragCompat();
        Bundle b = new Bundle(1);
        b.putString("key", key);
        fragment.setArguments(b);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            this.mText = this.getResetEditPreference().getText();
        } else {
            this.mText = savedInstanceState.getCharSequence(SAVE_STATE_TEXT);
        }

    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(SAVE_STATE_TEXT, this.mText);
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        this.mEditText = view.findViewById(android.R.id.edit);
        this.mEditText.requestFocus();
        if (this.mEditText == null) {
            throw new IllegalStateException("Dialog view must contain an EditText with id @android:id/edit");
        } else {
            this.mEditText.setText(this.mText);
            this.mEditText.setSelection(this.mEditText.getText().length());
        }
    }

    protected boolean needInputMethod() {
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        mWhichClicked = which;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        onDialogClosed();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        // do nothing
    }

    private ResetEditPreference getResetEditPreference() {
        return (ResetEditPreference) getPreference();
    }

    protected void onDialogClosed() {
        if (mWhichClicked == DialogInterface.BUTTON_NEUTRAL) {
            return;
        }
        ResetEditPreference resetEditPreference = getResetEditPreference();
        String value;
        if (mWhichClicked == DialogInterface.BUTTON_POSITIVE) {
            value = mEditText.getText().toString();
        } else {
            value = resetEditPreference.getDefaultValue();
        }

        if (resetEditPreference.callChangeListener(value)) {
            resetEditPreference.setText(value);
        }
    }
}
