package com.tianma.xsmscode.common.adapter;

import android.view.ContextMenu;
import android.view.View;

public class BaseItemCallback<E> implements ItemCallback<E> {
    @Override
    public void onItemClicked(View itemView, E item, int position) {
        onItemClicked(item, position);
    }

    protected void onItemClicked(E item, int position) {

    }

    @Override
    public boolean onItemLongClicked(View itemView, E item, int position) {
        return onItemLongClicked(item, position);
    }


    protected boolean onItemLongClicked(E item, int position) {
        return false;
    }

    @Override
    public void onCreateItemContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo,
                                        E item, int position) {
    }
}
