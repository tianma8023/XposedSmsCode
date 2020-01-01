package com.tianma.xsmscode.ui.rule.list;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.github.tianma8023.xposed.smscode.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.tianma.xsmscode.common.TextWatcherAdapter;
import com.tianma.xsmscode.common.adapter.BaseItemCallback;
import com.tianma.xsmscode.common.utils.SnackbarHelper;
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
import org.jetbrains.annotations.NotNull;

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

    private static final int REQUEST_CODE_EXPORT_RULES = 0xfff;
    private static final int REQUEST_CODE_IMPORT_RULES = 0xffe;

    static final String EXTRA_IMPORT_URI = "extra_import_uri";

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
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));
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
        mPresenter.saveRulesToFile(mRuleAdapter.getRuleList());
    }

    @Override
    public void onDestroy() {
        mPresenter.onDetach();
        super.onDestroy();
        mRuleAdapter.unregisterAdapterDataObserver(mDataObserver);
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

    private ItemTouchHelper.Callback mSwipeToRemoveCallback =
            new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END | ItemTouchHelper.START) {
                @Override
                public boolean onMove(@NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, @NotNull RecyclerView.ViewHolder target) {
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

        SnackbarHelper.makeLong(mRecyclerView, R.string.removed)
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
        if (mRuleAdapter.getItemCount() == 0) {
            SnackbarHelper.makeLong(mRecyclerView, R.string.rule_list_empty_snack_prompt).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android Q 及以后，使用 SAF (Storage Access Framework) 来导入导出文档文件
            Intent exportIntent = BackupManager.getExportRuleListSAFIntent();
            try {
                startActivityForResult(exportIntent, REQUEST_CODE_EXPORT_RULES);
            } catch (Exception e) {
                // 防止某些 Rom 将 DocumentUI 阉割掉
                SnackbarHelper.makeLong(mRecyclerView, R.string.documents_ui_not_found).show();
            }
        } else {
            // 考虑到在低版本的 Android 系统中，不少 Rom 将 DocumentUI 阉割掉了，无法使用 SAF
            // Android P 及以前，使用原有方式进行文件导入导出
            String perm = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            if (ContextCompat.checkSelfPermission(mActivity, perm) != PackageManager.PERMISSION_GRANTED) {
                showNoPermissionInfo();
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
                        mPresenter.exportRulesBelowQ(mRuleAdapter.getRuleList(), file, getString(R.string.exporting));
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
    }

    private void attemptImportRuleList() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android Q 及以后，使用 SAF (Storage Access Framework) 来导入导出文档文件
            Intent importIntent = BackupManager.getImportRuleListSAFIntent();
            try {
                startActivityForResult(importIntent, REQUEST_CODE_IMPORT_RULES);
            } catch (Exception e) {
                // 防止某些 Rom 将 DocumentUI 阉割掉
                SnackbarHelper.makeLong(mRecyclerView, R.string.documents_ui_not_found).show();
            }
        } else {
            // 考虑到在低版本的 Android 系统中，不少 Rom 将 DocumentUI 阉割掉了，无法使用 SAF
            // Android P 及以前，使用原有方式进行文件导入导出
            String perm = Manifest.permission.READ_EXTERNAL_STORAGE;
            if (ContextCompat.checkSelfPermission(mActivity, perm) != PackageManager.PERMISSION_GRANTED) {
                showNoPermissionInfo();
                return;
            }

            final File[] files = BackupManager.getBackupFiles();

            if (files == null || files.length == 0) {
                SnackbarHelper.makeLong(mRecyclerView, R.string.no_backup_exists).show();
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
    }


    private void showNoPermissionInfo() {
        SnackbarHelper.makeShort(mRecyclerView, R.string.no_permission_prompt).show();
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
    public void onExportCompletedBelowQ(boolean success, File file) {
        int msgId = success ? R.string.export_succeed : R.string.export_failed;
        Snackbar snackbar = SnackbarHelper.makeLong(mRecyclerView, msgId);
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
    public void onExportCompletedAboveQ(boolean success) {
        int msgId = success ? R.string.export_succeed : R.string.export_failed;
        SnackbarHelper.makeLong(mRecyclerView, msgId).show();
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
        SnackbarHelper.makeLong(mRecyclerView, msg).show();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_EXPORT_RULES) {
                mPresenter.exportRulesAboveQ(mRuleAdapter.getRuleList(), mActivity, data.getData(), getString(R.string.exporting));
            } else if (requestCode == REQUEST_CODE_IMPORT_RULES) {
                showImportDialogConfirm(data.getData());
            }

        }
    }
}
