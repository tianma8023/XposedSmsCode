package com.github.tianma8023.xposed.smscode.adapter;

import android.view.View;

public interface ItemChildCallback<E> {

    void onItemChildClicked(View childView, E item, int position);

}
