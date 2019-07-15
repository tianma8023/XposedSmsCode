package com.tianma.xsmscode.common.adapter;

import android.view.View;

public interface ItemChildCallback<E> {

    void onItemChildClicked(View childView, E item, int position);

}
