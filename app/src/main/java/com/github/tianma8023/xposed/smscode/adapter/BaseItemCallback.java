package com.github.tianma8023.xposed.smscode.adapter;

import android.view.ContextMenu;
import android.view.View;

public class BaseItemCallback<E> implements ItemCallback<E> {
    @Override
    public void onItemClicked(E item, int position) {
    }

    @Override
    public boolean onItemLongClicked(E item, int position) {
        return false;
    }

    @Override
    public void onCreateItemContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo,
                                        E item, int position) {
    }
}
