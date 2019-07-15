package com.tianma.xsmscode.ui.rule.list;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.github.tianma8023.xposed.smscode.R;
import com.tianma.xsmscode.common.TextWatcherAdapter;
import com.tianma.xsmscode.common.adapter.BaseItemCallback;
import com.tianma.xsmscode.common.utils.Utils;
import com.tianma.xsmscode.common.widget.FabScrollBehavior;
import com.tianma.xsmscode.data.db.entity.SmsCodeRule;
import com.tianma.xsmscode.data.eventbus.Event;
import com.tianma.xsmscode.data.eventbus.XEventBus;
import com.tianma.xsmscode.feature.backup.BackupManager;
import com.tianma.xsmscode.feature.backup.ImportResult;
import com.tianma.xsmscode.ui.rule.edit.RuleEditFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerFragment;


/**
 * SMS code codeRule list fragment
 */
public class RuleListFragment extends DaggerFragment implements RuleListContract.View {

    public static final String EXTRA_IMPORT_URI = "extra_import_uri";

    @BindView(R.id.rule_list_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.rule_list_fab)
    FloatingActionButton mFabButton;

    @BindView(R.id.empty_view)
    View mEmptyView;

    private Activity mActivity;
    private MaterialDialog mProgressDialog;
    private RuleAdapter mRuleAdapter;

    private int mSelectedPosition = -1;

    @Inject
    RuleListContract.Presenter mPresenter;

    public static RuleListFragment newInstance(Uri importUri) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_IMPORT_URI, importUri);

        RuleListFragment fragment = new RuleListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static RuleListFragment newInstance() {
        return newInstance(null);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rule_list, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = requireActivity();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRuleAdapter = new RuleAdapter(mActivity, new ArrayList<>());
        mRecyclerView.setAdapter(mRuleAdapter);

        // swipe to remove
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(mSwipeToRemoveCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        mRuleAdapter.setItemCallback(new BaseItemCallback<SmsCodeRule>() {
            @Override
            public void onItemClicked(SmsCodeRule item, int position) {
                mSelectedPosition = position;
                XEventBus.post(new Event.StartRuleEditEvent(
                        RuleEditFragment.EDIT_TYPE_UPDATE, item));
            }

            @Override
            public void onCreateItemContextMenu(ContextMenu menu, View v,
                                                ContextMenu.ContextMenuInfo menuInfo,
                                                SmsCodeRule item, int position) {
                mSelectedPosition = position;
                onCreateContextMenu(menu, v, menuInfo);
            }
        });

        mRuleAdapter.registerAdapterDataObserver(mDataObserver);

        // fab settings
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mFabButton.getLayoutParams();
        params.setBehavior(new FabScrollBehavior());
        mFabButton.setLayoutParams(params);

        mFabButton.setOnClickListener(v -> {
            SmsCodeRule emptyRule = new SmsCodeRule();
            XEventBus.post(new Event.StartRuleEditEvent(RuleEditFragment.EDIT_TYPE_CREATE, emptyRule));
        });

        refreshData();
        mPresenter.handleArguments(getArguments());
    }

    private void refreshData() {
        mPresenter.loadAllRules();
    }

    @Override
    public void onStart() {
        super.onStart();
        XEventBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        XEventBus.unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRuleAdapter.unregisterAdapterDataObserver(mDataObserver);
        mPresenter.onDetach();
        cancelProgress();
        mProgressDialog = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_rule_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_import_rules:
                attemptImportRuleList();
                break;
            case R.id.action_export_rules:
                attemptExportRuleList();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = mActivity.getMenuInflater();
        inflater.inflate(R.menu.context_rule_list, menu);
        menu.setHeaderTitle(R.string.actions);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        SmsCodeRule smsCodeRule = mRuleAdapter.getItemAt(mSelectedPosition);
        switch (item.getItemId()) {
            case R.id.action_edit_rule:
                XEventBus.post(new Event.StartRuleEditEvent(
                        RuleEditFragment.EDIT_TYPE_UPDATE, smsCodeRule));
                break;
            case R.id.action_remove_rule:
                removeItemAt(mSelectedPosition);
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    ItemTouchHelper.Callback mSwipeToRemoveCallback =
            new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END | ItemTouchHelper.START) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    final int position = viewHolder.getAdapterPosition();
                    removeItemAt(position);
                }
            };

    private void removeItemAt(final int position) {
        final SmsCodeRule itemToRemove = mRuleAdapter.getItemAt(position);
        mRuleAdapter.removeItemAt(position);

        Snackbar.make(mRecyclerView, R.string.removed, Snackbar.LENGTH_LONG)
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        if (event != DISMISS_EVENT_ACTION) {
                            mPresenter.removeRule(itemToRemove);
                        }
                    }
                }).setAction(R.string.revoke, v -> mRuleAdapter.addRule(position, itemToRemove))
                .show();
    }

    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            refreshEmptyView();
        }
    };

    private void refreshEmptyView() {
        if (mRuleAdapter.getItemCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRuleSaveOrUpdate(Event.OnRuleCreateOrUpdate event) {
        if (event.type == RuleEditFragment.EDIT_TYPE_CREATE) {
            mRuleAdapter.addRule(event.codeRule);
        } else if (event.type == RuleEditFragment.EDIT_TYPE_UPDATE) {
            mRuleAdapter.updateAt(mSelectedPosition, event.codeRule);
        }
    }

    private void attemptExportRuleList() {
        String perm = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(mActivity, perm) != PackageManager.PERMISSION_GRANTED) {
            showNoPermissionInfo();
            return;
        }

        if (mRuleAdapter.getItemCount() == 0) {
            Snackbar.make(mRecyclerView, R.string.rule_list_empty_snack_prompt, Snackbar.LENGTH_LONG).show();
            return;
        }

        final String defaultFilename = BackupManager.getDefaultBackupFilename();
        String hint = getString(R.string.backup_file_name);
        String content = getString(R.string.backup_file_dir, BackupManager.getBackupDir().getAbsolutePath());
        final MaterialDialog exportFilenameDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.backup_file_name)
                .content(content)
                .input(hint, defaultFilename, (dialog, input) -> {
                    File file = new File(BackupManager.getBackupDir(), input.toString());
                    mPresenter.exportRules(mRuleAdapter.getRuleList(), file, getString(R.string.exporting));
                })
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .negativeText(R.string.cancel)
                .build();

        final EditText editText = exportFilenameDialog.getInputEditText();
        if (editText != null) {
            exportFilenameDialog.setOnShowListener(dialog -> {
                int stop = defaultFilename.length() - BackupManager.getBackupFileExtension().length();
                editText.setSelection(0, stop);
            });

            final MDButton positiveBtn = exportFilenameDialog.getActionButton(DialogAction.POSITIVE);

            editText.addTextChangedListener(new TextWatcherAdapter() {
                @Override
                public void afterTextChanged(Editable s) {
                    positiveBtn.setEnabled(Utils.isValidFilename(s.toString()));
                }
            });
        }
        exportFilenameDialog.show();
    }

    private void attemptImportRuleList() {
        String perm = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(mActivity, perm) != PackageManager.PERMISSION_GRANTED) {
            showNoPermissionInfo();
            return;
        }

        final File[] files = BackupManager.getBackupFiles();

        if (files == null || files.length == 0) {
            Snackbar.make(mRecyclerView, R.string.no_backup_exists, Snackbar.LENGTH_LONG).show();
            return;
        }

        String[] filenames = new String[files.length];
        for (int i = 0; i < filenames.length; i++) {
            filenames[i] = files[i].getName();
        }

        new MaterialDialog.Builder(mActivity)
                .title(R.string.choose_backup_file)
                .items(filenames)
                .itemsCallback((dialog, itemView, position, text) -> {
                    File file = files[position];
                    Uri uri = Uri.fromFile(file);
                    showImportDialogConfirm(uri);
                })
                .show();
    }

    private void showNoPermissionInfo() {
        Snackbar.make(mRecyclerView, R.string.no_permission_prompt, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void displayRules(List<SmsCodeRule> rules) {
        mRuleAdapter.setRules(rules);
    }

    @Override
    public void attemptImportRuleListDirectly(Uri uri) {
        String perm = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(mActivity, perm) != PackageManager.PERMISSION_GRANTED) {
            showNoPermissionInfo();
            return;
        }

        showImportDialogConfirm(uri);
    }

    @Override
    public void showImportDialogConfirm(final Uri uri) {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.import_confirmation_title)
                .content(R.string.import_confirmation_message)
                .positiveText(R.string.yes)
                .onPositive((dialog, which) -> mPresenter.importRules(uri, true, getString(R.string.importing)))
                .negativeText(R.string.no)
                .onNegative((dialog, which) -> mPresenter.importRules(uri, false, getString(R.string.importing)))
                .show();
    }

    @Override
    public void onExportCompleted(boolean success, File file) {
        int msgId = success ? R.string.export_succeed : R.string.export_failed;
        Snackbar snackbar = Snackbar.make(mRecyclerView, msgId, Snackbar.LENGTH_LONG);
        if (success) {
            snackbar.setAction(R.string.share, v -> {
                if (mActivity != null) {
                    BackupManager.shareBackupFile(mActivity, file);
                }
            });
        }
        snackbar.show();
    }

    @Override
    public void onImportComplete(ImportResult importResult) {
        @StringRes int msg;
        switch (importResult) {
            case SUCCESS:
                refreshData();
                msg = R.string.import_succeed;
                break;
            case VERSION_MISSED:
                msg = R.string.import_failed_version_missed;
                break;
            case VERSION_UNKNOWN:
                msg = R.string.import_failed_version_unknown;
                break;
            case BACKUP_INVALID:
                msg = R.string.import_failed_backup_invalid;
                break;
            case READ_FAILED:
            default:
                msg = R.string.import_failed_read_error;
                break;
        }
        Snackbar.make(mRecyclerView, msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showProgress(String progressMsg) {
        if (mProgressDialog == null) {
            mProgressDialog = new MaterialDialog.Builder(mActivity)
                    .content(progressMsg)
                    .progress(true, 100)
                    .cancelable(true)
                    .build();
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    @Override
    public void cancelProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }
}
