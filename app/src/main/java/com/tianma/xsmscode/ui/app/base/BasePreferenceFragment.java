package com.tianma.xsmscode.ui.app.base;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.tianma.xsmscode.common.constant.PrefConst;

import java.util.Objects;

/**
 * Base Preferences Fragment
 */
public abstract class BasePreferenceFragment extends PreferenceFragmentCompat {

   @Override
   public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      PreferenceManager manager = getPreferenceManager();
      manager.setSharedPreferencesName(PrefConst.PREF_NAME);

      doOnCreatePreferences(savedInstanceState, rootKey);
   }

   abstract protected void doOnCreatePreferences(Bundle savedInstanceState, String rootKey);

   @NonNull
   @Override
   public <T extends Preference> T findPreference(@NonNull CharSequence key) {
      return Objects.requireNonNull(super.findPreference(key));
   }
}
