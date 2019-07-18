package com.tianma.xsmscode.common.fragment.backpress;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

/**
 * Helper for dispatching back-press event.
 */
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
                ((BackPressedListener) fragment).interceptBackPress();
    }

}
