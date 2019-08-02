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

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * SMS code records fragment
 */
public class CodeRecordFragment extends DaggerBackPressFragment implements CodeRecordContract.View {

    // normal mode
    private static final int RECORD_MODE_NORMAL = 0;
    // edit mode
    private static final int RECORD_MODE_EDIT = 1;

    @IntDef({RECORD_MODE_NORMAL, RECORD_MODE_EDIT})
    @interface RecordMode {
    }

    private Activity mActivity;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.code_records_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.empty_view)
    View mEmptyView;

    private CodeRecordAdapter mCodeRecordAdapter;

    @RecordMode
    private int mCurrentMode = RECORD_MODE_NORMAL;

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

        mCodeRecordAdapter = new CodeRecordAdapter(mActivity, records);
        mCodeRecordAdapter.setItemCallback(new BaseItemCallback<RecordItem>() {
            @Override
            public void onItemClicked(View itemView, RecordItem item, int position) {
                itemClicked(item, position);
            }

            @Override
            public boolean onItemLongClicked(View itemView, RecordItem item, int position) {
                return itemLongClicked(item, position);
            }
        });
        mCodeRecordAdapter.setItemChildCallback((childView, item, position) -> {
            int viewId = childView.getId();
            if (viewId == R.id.record_details_view) {
                showSmsDetails(item);
            }
        });
        mCodeRecordAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                refreshEmptyView();
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRecyclerView.setAdapter(mCodeRecordAdapter);
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
        if (mCodeRecordAdapter.getItemCount() > 0) {
            mEmptyView.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    private void itemClicked(RecordItem item, int position) {
        if (mCurrentMode == RECORD_MODE_EDIT) {
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
                .show();
    }

    private void copySmsCode(RecordItem item) {
        String smsCode = item.getSmsMsg().getSmsCode();
        ClipboardUtils.copyToClipboard(mActivity, smsCode);
        String prompt = getString(R.string.prompt_sms_code_copied, smsCode);
        SnackbarHelper.makeShort(mRecyclerView, prompt).show();
    }

    private void selectRecordItem(int position) {
        if (mCurrentMode == RECORD_MODE_NORMAL) {
            mCurrentMode = RECORD_MODE_EDIT;
            refreshActionBarByMode();
        }
        boolean selected = mCodeRecordAdapter.isItemSelected(position);
        mCodeRecordAdapter.setItemSelected(position, !selected);
        if (selected) {
            boolean isAllUnselected = mCodeRecordAdapter.isAllUnselected();
            if (isAllUnselected) {
                mCurrentMode = RECORD_MODE_NORMAL;
                refreshActionBarByMode();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mCurrentMode == RECORD_MODE_EDIT) {
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
                boolean isAllSelected = mCodeRecordAdapter.isAllSelected();
                mCodeRecordAdapter.setAllSelected(!isAllSelected);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean interceptBackPress() {
        return mCurrentMode == RECORD_MODE_EDIT;
    }

    @Override
    public void onBackPressed() {
        if (mCurrentMode == RECORD_MODE_EDIT) {
            mCurrentMode = RECORD_MODE_NORMAL;
            mCodeRecordAdapter.setAllSelected(false);
            refreshActionBarByMode();
        } else {
            super.onBackPressed();
        }
    }

    private void removeSelectedItems() {
        final List<SmsMsg> itemsToRemove = mCodeRecordAdapter.removeSelectedItems();
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
                .setAction(R.string.revoke, v -> mCodeRecordAdapter.addItems(itemsToRemove))
                .show();

        mCurrentMode = RECORD_MODE_NORMAL;
        refreshActionBarByMode();
    }

    private void refreshActionBarByMode() {
        if (mCurrentMode == RECORD_MODE_NORMAL) {
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
        mCodeRecordAdapter.addItems(smsMsgList);
    }

}
