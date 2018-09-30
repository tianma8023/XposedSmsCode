package com.github.tianma8023.xposed.smscode.app.rule;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
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
import com.github.tianma8023.xposed.smscode.adapter.BaseItemCallback;
import com.github.tianma8023.xposed.smscode.backup.BackupManager;
import com.github.tianma8023.xposed.smscode.backup.ExportResult;
import com.github.tianma8023.xposed.smscode.backup.ImportResult;
import com.github.tianma8023.xposed.smscode.db.DBManager;
import com.github.tianma8023.xposed.smscode.entity.SmsCodeRule;
import com.github.tianma8023.xposed.smscode.event.Event;
import com.github.tianma8023.xposed.smscode.event.XEventBus;
import com.github.tianma8023.xposed.smscode.utils.Utils;
import com.github.tianma8023.xposed.smscode.utils.XLog;
import com.github.tianma8023.xposed.smscode.widget.DialogAsyncTask;
import com.github.tianma8023.xposed.smscode.widget.FabScrollBehavior;
import com.github.tianma8023.xposed.smscode.widget.TextWatcherAdapter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * SMS code codeRule list fragment
 */
public class RuleListFragment extends Fragment {

    private static final int TYPE_EXPORT = 1;
    private static final int TYPE_IMPORT = 2;
    private static final int TYPE_IMPORT_DIRECT = 3;

    public static final String EXTRA_IMPORT_URI = "extra_import_uri";

    @IntDef({TYPE_EXPORT, TYPE_IMPORT, TYPE_IMPORT_DIRECT})
    @interface BackupType {
    }

    @BindView(R.id.rule_list_recycler_view)
    RecyclerView mRecyclerView;

    private RuleAdapter mRuleAdapter;

    @BindView(R.id.rule_list_fab)
    FloatingActionButton mFabButton;

    @BindView(R.id.empty_view)
    View mEmptyView;

    private int mSelectedPosition = -1;

    private Activity mActivity;

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rule_list, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = getActivity();

        List<SmsCodeRule> rules = DBManager.get(mActivity).queryAllSmsCodeRules();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRuleAdapter = new RuleAdapter(mActivity, rules);
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

        mFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SmsCodeRule emptyRule = new SmsCodeRule();
                XEventBus.post(new Event.StartRuleEditEvent(
                        RuleEditFragment.EDIT_TYPE_CREATE, emptyRule));
            }
        });

        refreshEmptyView();

        onHandleArguments(getArguments());
    }

    /**
     * Handle arguments
     */
    private void onHandleArguments(Bundle args) {
        if (args == null) {
            return;
        }

        final Uri importUri = args.getParcelable(EXTRA_IMPORT_URI);
        if (importUri != null) {
            args.remove(EXTRA_IMPORT_URI);

            if (ContentResolver.SCHEME_FILE.equals(importUri.getScheme())) {
                // file:// URI need storage permission
                attemptImportRuleListDirectly(importUri);
            } else {
                // content:// URI don't need storage permission
                showImportDialogConfirm(importUri);
            }
        }
    }

    private void refreshData() {
        List<SmsCodeRule> rules = DBManager.get(mActivity).queryAllSmsCodeRules();
        mRuleAdapter.setRules(rules);
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

        Snackbar snackbar = Snackbar.make(mRecyclerView, R.string.removed, Snackbar.LENGTH_LONG);
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (event != DISMISS_EVENT_ACTION) {
                    try {
                        DBManager.get(mActivity).removeSmsCodeRule(itemToRemove);
                    } catch (Exception e) {
                        XLog.e("Remove " + itemToRemove.toString() + " failed", e);
                    }
                }
            }
        });
        snackbar.setAction(R.string.revoke, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRuleAdapter.addRule(position, itemToRemove);
            }
        });
        snackbar.show();
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

//    private void requestPermission(final @BackupType int type, final Uri importUri) {
//        String[] permission ;
//        if (type == TYPE_EXPORT) {
//            permission = new String[] {
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            };
//        } else {
//            permission = new String[] {
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//            };
//        }
//    }

    private void attemptExportRuleList() {
        String perm = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(mActivity, perm) != PackageManager.PERMISSION_GRANTED) {
            // todo
            return;
        }

        final String defaultFilename = BackupManager.getDefaultBackupFilename();
        String hint = getString(R.string.backup_file_name);
        String content = getString(R.string.backup_file_dir, BackupManager.getBackupDir().getAbsolutePath());
        final MaterialDialog exportFilenameDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.backup_file_name)
                .content(content)
                .input(hint, defaultFilename, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        File file = new File(BackupManager.getBackupDir(), input.toString());
                        new ExportAsyncTask(mActivity, mRuleAdapter,
                                file, getString(R.string.exporting)).execute();
                    }
                })
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .negativeText(R.string.cancel)
                .build();

        final EditText editText = exportFilenameDialog.getInputEditText();
        if (editText != null) {
            exportFilenameDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    editText.setSelection(0, defaultFilename.length() - BackupManager.getBackupFileExtension().length());
                }
            });
            final MDButton positiveBtn =
                    exportFilenameDialog.getActionButton(DialogAction.POSITIVE);
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
            // todo
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

        final MaterialDialog importDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.choose_backup_file)
                .items(filenames)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        File file = files[position];
                        Uri uri = Uri.fromFile(file);
                        showImportDialogConfirm(uri);
                    }
                })
                .build();
        importDialog.show();
    }

    private void attemptImportRuleListDirectly(Uri uri) {
        String perm = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(mActivity, perm) != PackageManager.PERMISSION_GRANTED) {
            // todo
            return;
        }

        showImportDialogConfirm(uri);
    }

    private void showImportDialogConfirm(final Uri uri) {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.import_confirmation_title)
                .content(R.string.import_confirmation_message)
                .positiveText(R.string.yes)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        new ImportAsyncTask(mActivity, uri,
                                getString(R.string.importing), true).execute();
                    }
                })
                .negativeText(R.string.no)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        new ImportAsyncTask(mActivity, uri,
                                getString(R.string.importing), false).execute();
                    }
                })
                .show();
    }

    private static class ExportAsyncTask extends DialogAsyncTask<Void, Void, ExportResult> {

        private File mFile;
        private RuleAdapter mRuleAdapter;

        ExportAsyncTask(Context context, RuleAdapter ruleAdapter, File file, String progressMsg) {
            this(context, progressMsg, false);
            mRuleAdapter = ruleAdapter;
            mFile = file;
        }

        private ExportAsyncTask(Context context, String progressMsg, boolean cancelable) {
            super(context, progressMsg, cancelable);
        }

        @Override
        protected ExportResult doInBackground(Void... voids) {
            return BackupManager.exportRuleList(mFile, mRuleAdapter.getRuleList());
        }

        @Override
        protected void onPostExecute(ExportResult exportResult) {
            super.onPostExecute(exportResult);
            XEventBus.post(new Event.ExportEvent(exportResult, mFile));
        }
    }

    private static class ImportAsyncTask extends DialogAsyncTask<Void, Void, ImportResult> {

        private WeakReference<Context> mContextRef;
        private Uri mUri;
        private boolean mRetain;

        ImportAsyncTask(Context context, Uri uri, String progressMsg, boolean retain) {
            this(context, progressMsg, false);
            mContextRef = new WeakReference<>(context);
            mUri = uri;
            mRetain = retain;
        }

        ImportAsyncTask(Context context, String progressMsg, boolean cancelable) {
            super(context, progressMsg, cancelable);
        }

        @Override
        protected ImportResult doInBackground(Void... voids) {
            Context context;
            if ((context = mContextRef.get()) != null) {
                return BackupManager.importRuleList(context, mUri, mRetain);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ImportResult importResult) {
            super.onPostExecute(importResult);
            XEventBus.post(importResult);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExportCompleted(final Event.ExportEvent event) {
        int msgId;
        if (event.result == ExportResult.SUCCESS) {
            msgId = R.string.export_succeed;
        } else {
            // ExportResult.FAILED
            msgId = R.string.export_failed;
        }
        Snackbar snackbar = Snackbar.make(mRecyclerView, msgId, Snackbar.LENGTH_LONG);
        if (event.result == ExportResult.SUCCESS) {
            snackbar.setAction(R.string.share, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mActivity != null) {
                        BackupManager.shareBackupFile(mActivity, event.file);
                    }
                }
            });
        }
        snackbar.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
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
}
