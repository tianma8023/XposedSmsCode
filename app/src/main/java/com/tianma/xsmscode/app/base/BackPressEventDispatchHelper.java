package com.tianma.xsmscode.app.base;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import java.util.List;

public class BackPressEventDispatchHelper {

    public static boolean dispatchBackPressedEvent(FragmentManager fragmentManager) {
        List<Fragment> fragments = fragmentManager.getFragments();

        if (fragments == null)
            return false;

        for (int i = fragments.size() - 1; i >= 0; i--) {
            Fragment fragment = fragments.get(i);

            if (isBackPressedIntercepted(fragment)) {
                ((BackPressedListener) fragment).onBackPressed();
                return true;
            }
        }

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            return true;
        }
        return false;
    }

    public static boolean dispatchBackPressedEvent(Fragment fragment) {
        return dispatchBackPressedEvent(fragment.getChildFragmentManager());
    }

    public static boolean dispatchBackPressedEvent(FragmentActivity fragmentActivity) {
        return dispatchBackPressedEvent(fragmentActivity.getSupportFragmentManager());
    }

    private static boolean isBackPressedIntercepted(Fragment fragment) {
        return fragment != null &&
                fragment.isVisible() &&
                fragment.getUserVisibleHint() &&
                fragment instanceof BackPressedListener &&
                ((BackPressedListener) fragment).onInterceptBackPressed();
    }

}
