package com.tianma.xsmscode.ui.block;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.tianma8023.xposed.smscode.R;
import com.google.android.material.snackbar.Snackbar;
import com.tianma.xsmscode.common.adapter.ItemCallback;
import com.tianma.xsmscode.data.db.entity.AppInfo;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerFragment;

public class AppBlockFragment extends DaggerFragment implements AppBlockContract.View {

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mRefreshLayout;
    @BindView(R.id.app_block_recycler_view)
    RecyclerView mRecyclerView;

    private AppInfoAdapter mAppInfoAdapter;

    private Activity mActivity;

    @Inject
    AppBlockContract.Presenter mPresenter;

    static AppBlockFragment newInstance() {
        return new AppBlockFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_app_block, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = getActivity();

        mAppInfoAdapter = new AppInfoAdapter(mActivity, new ArrayList<>());
        mAppInfoAdapter.setItemCallback(new ItemCallback<AppInfo>() {
            @Override
            public void onItemClicked(View itemView, AppInfo item, int position) {
                itemClicked(item, position);
            }

            @Override
            public boolean onItemLongClicked(View itemView, AppInfo item, int position) {
                itemClicked(item, position);
                return true;
            }

            @Override
            public void onCreateItemContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, AppInfo item, int position) {

            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRecyclerView.setAdapter(mAppInfoAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));

        mRefreshLayout.setOnRefreshListener(() -> mPresenter.refreshData());
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.refreshData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onDetach();
        cancelProgress();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_app_block, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                doFilter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                doFilter(newText);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort_by_label_asc:
                mPresenter.doSort(SortType.LABEL_ASC);
                break;
            case R.id.action_sort_by_pkg_asc:
                mPresenter.doSort(SortType.PACKAGE_ASC);
                break;
            case R.id.action_sort_by_label_desc:
                mPresenter.doSort(SortType.LABEL_DESC);
                break;
            case R.id.action_sort_by_pkg_desc:
                mPresenter.doSort(SortType.PACKAGE_DESC);
                break;
            case R.id.action_tick:
                mPresenter.saveData();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void doFilter(String filter) {
        mPresenter.doFilter(filter);
    }

    private void itemClicked(AppInfo appInfo, int position) {
        mPresenter.doItemClicked(appInfo);
        mAppInfoAdapter.setItemSelected(position);
    }

    @Override
    public void showData(List<AppInfo> appInfoList) {
        mAppInfoAdapter.setItemList(appInfoList);
        mRefreshLayout.setEnabled(false);
    }

    @Override
    public void showError(Throwable t) {
        mRefreshLayout.setEnabled(true);
        Snackbar.make(mRecyclerView, R.string.load_failed, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showProgress() {
        if (!mRefreshLayout.isRefreshing()) {
            mRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public void cancelProgress() {
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onSaveSuccess() {
        mActivity.onBackPressed();
    }

    @Override
    public void onSaveFailed() {
        Snackbar.make(mRecyclerView, R.string.save_failed, Snackbar.LENGTH_SHORT).show();
    }
}
