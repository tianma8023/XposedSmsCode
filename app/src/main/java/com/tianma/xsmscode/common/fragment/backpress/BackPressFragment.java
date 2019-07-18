package com.tianma.xsmscode.common.fragment.backpress;

import androidx.fragment.app.Fragment;

/**
 * Fragment that handled back pressed event.
 */
public class BackPressFragment extends Fragment implements BackPressedListener {

    @Override
    public boolean interceptBackPress() {
        return false;
    }

    @Override
    public void onBackPressed() {
        BackPressEventDispatchHelper.dispatchBackPressedEvent(this);
    }
}
