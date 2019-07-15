package com.tianma.xsmscode.ui.theme;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.tianma8023.xposed.smscode.R;
import com.tianma.xsmscode.common.adapter.ItemCallback;

import java.util.List;

public class ThemeItemAdapter extends RecyclerView.Adapter<ThemeItemAdapter.VH> {

    private ItemCallback<ThemeItem> mItemCallback;
    private List<ThemeItem> mThemeItemList;
    private Context mContext;

    public ThemeItemAdapter(Context context, List<ThemeItem> themeItemList) {
        mThemeItemList = themeItemList;
        mContext = context;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.theme_item, parent, false);
        return new VH(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ThemeItem themeItem = mThemeItemList.get(position);
        holder.bindData(themeItem);
        holder.bindListener(themeItem, position);
    }

    @Override
    public int getItemCount() {
        return mThemeItemList == null ? 0 : mThemeItemList.size();
    }

    public void setItemCallback(ItemCallback<ThemeItem> itemCallback) {
        mItemCallback = itemCallback;
    }

    class VH extends RecyclerView.ViewHolder {

        View mItemView;
        TextView mColorItemTextView;

        VH(View itemView) {
            super(itemView);
            mItemView = itemView;
            mColorItemTextView = itemView.findViewById(R.id.tv_color_item);
        }

        void bindData(ThemeItem themeItem) {
            if (themeItem == null)
                return;
            mColorItemTextView.setText(themeItem.getColorNameRes());
            mColorItemTextView.setBackgroundColor(
                    ContextCompat.getColor(mContext, themeItem.getColorValueRes()));
        }

        void bindListener(final ThemeItem themeItem, final int position) {
            if (mItemCallback != null) {
                mItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mItemCallback.onItemClicked(itemView, themeItem, position);
                    }
                });
            }
        }
    }
}
