package com.tianma.xsmscode.ui.record;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.tianma8023.xposed.smscode.R;
import com.google.android.material.snackbar.Snackbar;
import com.tianma.xsmscode.common.adapter.BaseItemCallback;
import com.tianma.xsmscode.common.utils.ClipboardUtils;
import com.tianma.xsmscode.common.utils.SnackbarHelper;
import com.tianma.xsmscode.data.db.entity.SmsMsg;
import com.tianma.xsmscode.ui.app.base.DaggerBackPressFragment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tianma.xsmscode.ui.record.CodeRecordAdapter.RECORD_MODE_EDIT;
import static com.tianma.xsmscode.ui.record.CodeRecordAdapter.RECORD_MODE_NORMAL;

/**
 * SMS code records fragment
 */
public class CodeRecordFragment extends DaggerBackPressFragment implements CodeRecordContract.View {

    private Activity mActivity;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.code_records_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.empty_view)
    View mEmptyView;

    private CodeRecordAdapter mAdapter;

    @Inject
    CodeRecordContract.Presenter mPresenter;

    public static CodeRecordFragment newInstance() {
        return new CodeRecordFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_code_records, container, false);
        ButterKnife.bind(this, rootView);
        mSwipeRefreshLayout.setOnRefreshListener(() -> mPresenter.loadData());
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = getActivity();

        List<RecordItem> records = new ArrayList<>();

        mAdapter = new CodeRecordAdapter(mActivity, records);
        mAdapter.setItemCallback(new BaseItemCallback<RecordItem>() {
            @Override
            public void onItemClicked(View itemView, RecordItem item, int position) {
                itemClicked(item, position);
            }

            @Override
            public boolean onItemLongClicked(View itemView, RecordItem item, int position) {
                return itemLongClicked(item, position);
            }
        });
        mAdapter.setItemChildCallback((childView, item, position) -> {
            int viewId = childView.getId();
            if (viewId == R.id.record_details_view) {
                showSmsDetails(item);
            } else if (viewId == R.id.checkbox) {
                selectRecordItem(position);
            }
        });
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                refreshEmptyView();
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        mPresenter.loadData();
    }

    private void refreshEmptyView() {
        if (mAdapter.getItemCount() > 0) {
            mEmptyView.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    private void itemClicked(RecordItem item, int position) {
        if (mAdapter.getMode() == RECORD_MODE_EDIT) {
            itemLongClicked(item, position);
        } else {
            copySmsCode(item);
        }
    }

    private boolean itemLongClicked(RecordItem item, int position) {
        selectRecordItem(position);
        return true;
    }

    private void showSmsDetails(final RecordItem recordItem) {
        SmsMsg smsMsg = recordItem.getSmsMsg();
        new MaterialDialog.Builder(mActivity)
                .title(R.string.message_details)
                .content(smsMsg.getBody())
                .positiveText(R.string.copy_smscode)
                .onPositive((dialog, which) -> copySmsCode(recordItem))
                .negativeText(R.string.cancel)
                .neutralText(R.string.copy_sms)
                .onNeutral((dialog, which) -> copySms(recordItem))
                .show();
    }

    private void copySms(RecordItem item) {
        String sms = item.getSmsMsg().getBody();
        ClipboardUtils.copyToClipboard(mActivity, sms);
        String prompt = getString(R.string.prompt_sms_copied);
        SnackbarHelper.makeShort(mRecyclerView, prompt).show();
    }

    private void copySmsCode(RecordItem item) {
        String smsCode = item.getSmsMsg().getSmsCode();
        ClipboardUtils.copyToClipboard(mActivity, smsCode);
        String prompt = getString(R.string.prompt_sms_code_copied, smsCode);
        SnackbarHelper.makeShort(mRecyclerView, prompt).show();
    }

    private void selectRecordItem(int position) {
        if (mAdapter.getMode() == RECORD_MODE_NORMAL) {
            mAdapter.setMode(RECORD_MODE_EDIT);
            refreshActionBarByMode();
        }
        boolean selected = mAdapter.isItemSelected(position);
        mAdapter.setItemSelected(position, !selected);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mAdapter.getMode() == RECORD_MODE_EDIT) {
            inflater.inflate(R.menu.menu_edit_code_record, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                removeSelectedItems();
                break;
            case R.id.action_select_all:
                boolean allSelected = mAdapter.isAllSelected();
                mAdapter.setAllSelected(!allSelected);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean interceptBackPress() {
        return mAdapter.getMode() == RECORD_MODE_EDIT;
    }

    @Override
    public void onBackPressed() {
        if (mAdapter.getMode() == RECORD_MODE_EDIT) {
            mAdapter.setMode(RECORD_MODE_NORMAL);
            mAdapter.setAllSelected(false);
            refreshActionBarByMode();
        } else {
            super.onBackPressed();
        }
    }

    private void removeSelectedItems() {
        final List<SmsMsg> itemsToRemove = mAdapter.removeSelectedItems();
        mSwipeRefreshLayout.setEnabled(false);
        String text = getString(R.string.some_items_removed, itemsToRemove.size());
        SnackbarHelper.makeLong(mRecyclerView, text)
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        if (event != DISMISS_EVENT_ACTION) {
                            mPresenter.removeSmsMsg(itemsToRemove);
                            mSwipeRefreshLayout.setEnabled(true);
                        }
                    }
                })
                .setAction(R.string.revoke, v -> mAdapter.addItems(itemsToRemove))
                .show();

        mAdapter.setMode(RECORD_MODE_NORMAL);
        refreshActionBarByMode();
    }

    private void refreshActionBarByMode() {
        if (mAdapter.getMode() == RECORD_MODE_NORMAL) {
            mActivity.setTitle(R.string.smscode_records);
            mActivity.invalidateOptionsMenu();
        } else {
            mActivity.setTitle(R.string.edit_smscode_records);
            mActivity.invalidateOptionsMenu();
        }
    }

    @Override
    public void showRefreshing() {
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public void stopRefresh() {
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void displayData(List<SmsMsg> smsMsgList) {
        mAdapter.addItems(smsMsgList);
    }

}
