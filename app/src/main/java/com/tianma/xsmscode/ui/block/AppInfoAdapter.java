package com.tianma.xsmscode.ui.block;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.tianma8023.xposed.smscode.R;
import com.tianma.xsmscode.common.adapter.ItemCallback;
import com.tianma.xsmscode.data.db.entity.AppInfo;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

class AppInfoAdapter extends RecyclerView.Adapter<AppInfoAdapter.VH> {

    private Context mContext;
    private List<AppInfo> mDataList;
    private PackageManager mPackageManager;

    private ItemCallback<AppInfo> mItemCallback;

    AppInfoAdapter(Context context, List<AppInfo> appInfoList) {
        mContext = context;
        mDataList = appInfoList;
        mPackageManager = mContext.getPackageManager();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_app_info, parent, false);
        return new VH(rootView);
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final AppInfo data = getItemAt(position);
        holder.bindData(data, position);
        holder.bindListener(data, position);
    }

    void setItemCallback(ItemCallback<AppInfo> callback) {
        mItemCallback = callback;
    }

    class VH extends RecyclerView.ViewHolder {

        @BindView(R.id.app_icon_view)
        ImageView mAppIconView;

        @BindView(R.id.app_label_view)
        TextView mAppLabelView;

        @BindView(R.id.pkg_name_view)
        TextView mPkgNameView;

        VH(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @SuppressLint("CheckResult")
        void bindData(AppInfo data, int position) {
            Observable
                    .fromCallable(() -> mPackageManager.getApplicationIcon(data.getPackageName()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(drawable -> {
                        if (drawable != null) {
                            mAppIconView.setImageDrawable(drawable);
                        }
                    }, throwable -> {
                        // ignore
                    });
            mAppLabelView.setText(data.getLabel());
            mPkgNameView.setText(data.getPackageName());
            itemView.setSelected(data.isBlocked());
        }

        void bindListener(final AppInfo data, final int position) {
            if (mItemCallback != null) {
                itemView.setOnClickListener(v -> mItemCallback.onItemClicked(itemView, data, position));
                itemView.setOnLongClickListener(v -> mItemCallback.onItemLongClicked(itemView, data, position));
            }
        }
    }

    private AppInfo getItemAt(int position) {
        return mDataList.get(position);
    }

    void setItemList(List<AppInfo> appInfoList) {
        mDataList.clear();
        mDataList.addAll(appInfoList);
        notifyDataSetChanged();
    }

    void setItemSelected(int position) {
        notifyItemChanged(position);
    }

}
