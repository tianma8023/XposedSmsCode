package com.github.tianma8023.xposed.smscode.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Floating Action Bar scroll behavior
 */
public class FabScrollBehavior extends FloatingActionButton.Behavior {

    public FabScrollBehavior() {
    }

    public FabScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull FloatingActionButton child,
                                       @NonNull View directTargetChild,
                                       @NonNull View target, int axes, int type) {
        // Ensure we react to vertical scrolling
        return type == ViewCompat.TYPE_TOUCH && axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public boolean onNestedFling(@NonNull CoordinatorLayout coordinatorLayout,
                                 @NonNull FloatingActionButton child,
                                 @NonNull View target, float velocityX,
                                 float velocityY, boolean consumed) {
        if (velocityY > 500) {
            animateOut(child);
            return true;
        } else if (velocityY < -500) {
            animateIn(child);
            return true;
        }
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    }

    private void animateOut(FloatingActionButton fab) {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        int bottomMargin = layoutParams.bottomMargin;
        fab.animate().translationY(fab.getHeight() + bottomMargin).setInterpolator(new LinearInterpolator()).start();
    }

    private void animateIn(FloatingActionButton fab) {
        fab.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
    }
}
