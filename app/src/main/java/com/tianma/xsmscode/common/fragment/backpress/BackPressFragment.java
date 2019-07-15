package com.tianma.xsmscode.common.fragment.backpress;

import android.support.v4.app.Fragment;

/**
 * Fragment that handled back pressed event.
 */
public class BackPressFragment extends Fragment implements BackPressedListener {

    @Override
    public boolean onInterceptBackPressed() {
        return false;
    }

    @Override
    public void onBackPressed() {
        BackPressEventDispatchHelper.dispatchBackPressedEvent(this);
    }
}
