package com.tianma.xsmscode.ui.app.base;

import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;

/**
 * base activity
 */
public abstract class BaseActivity extends CyaneaAppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
