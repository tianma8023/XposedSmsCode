package com.tianma.xsmscode.common.fragment.backpress;

/**
 * Listener for handling back-press event.
 */
public interface BackPressedListener {

    /**
     * Whether current fragment should intercept back-press event.
     *
     * @return true if back-press event should be intercepted, false if not.
     */
    boolean interceptBackPress();

    /**
     * Callback on back-pressed.
     */
    void onBackPressed();

}
