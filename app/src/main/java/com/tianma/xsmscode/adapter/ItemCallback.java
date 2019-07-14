package com.tianma.xsmscode.adapter;

import android.view.ContextMenu;
import android.view.View;

public interface ItemCallback<E> {

    void onItemClicked(View itemView, E item, int position);

    boolean onItemLongClicked(View itemView, E item, int position);

    void onCreateItemContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, E item, int position);
}
