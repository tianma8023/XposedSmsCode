package com.github.tianma8023.xposed.smscode.app.rule;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.tianma8023.xposed.smscode.R;
import com.github.tianma8023.xposed.smscode.constant.Const;
import com.github.tianma8023.xposed.smscode.db.DBManager;
import com.github.tianma8023.xposed.smscode.entity.SmsCodeRule;
import com.github.tianma8023.xposed.smscode.event.Event;
import com.github.tianma8023.xposed.smscode.event.XEventBus;
import com.github.tianma8023.xposed.smscode.utils.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Rule edit fragment
 */
public class RuleEditFragment extends Fragment {

    public static final int EDIT_TYPE_CREATE = 1;
    public static final int EDIT_TYPE_UPDATE = 2;

    @IntDef({EDIT_TYPE_CREATE, EDIT_TYPE_UPDATE})
    public @interface RuleEditType {
    }

    private static final String KEY_RULE_EDIT_TYPE = "rule_edit_type";
    private static final String KEY_CODE_RULE = "code_rule";

    @BindView(R.id.rule_company_edit_text)
    TextInputEditText mCompanyEditText;

    @BindView(R.id.rule_keyword_edit_text)
    TextInputEditText mKeywordEditText;

    @BindView(R.id.rule_code_regex_quick_choose)
    Button mQuickChooseBtn;

    @BindView(R.id.rule_code_regex_edit_text)
    TextInputEditText mCodeRegexEditText;

    private Activity mActivity;

    private int mCodeTypeIndex = 0;

    private int mRuleEditType;
    private SmsCodeRule mCodeRule;

    public static RuleEditFragment newInstance(int ruleEditType, SmsCodeRule codeRule) {
        Bundle args = new Bundle();
        args.putInt(KEY_RULE_EDIT_TYPE, ruleEditType);
        args.putParcelable(KEY_CODE_RULE, codeRule);
        RuleEditFragment fragment = new RuleEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rule_edit, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
        mQuickChooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuickChooseDialog();
            }
        });

        mCodeRegexEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    saveIfValid();
                    return true;
                }
                return false;
            }
        });
        initWithArgs();
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

    private void initWithArgs() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        mRuleEditType = args.getInt(KEY_RULE_EDIT_TYPE);
        mCodeRule = args.getParcelable(KEY_CODE_RULE);
        if (mRuleEditType == EDIT_TYPE_UPDATE && mCodeRule != null) {
            setText(mCompanyEditText, mCodeRule.getCompany());
            setText(mKeywordEditText, mCodeRule.getCodeKeyword());
            setText(mCodeRegexEditText, mCodeRule.getCodeRegex());
        } else {
            loadTemplate();
        }
    }

    private void showQuickChooseDialog() {
        final String[] codeTypes = mActivity.getResources().getStringArray(R.array.sms_code_type_list);

        final View dialogView = LayoutInflater.from(mActivity).inflate(R.layout.dialog_code_regex_quick_chcoose, null);
        final AppCompatSpinner spinner = dialogView.findViewById(R.id.rule_code_type_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCodeTypeIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final TextInputEditText codeLenEditText = dialogView.findViewById(R.id.code_rule_length_edit_text);

        MaterialDialog quickChooseDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.quick_choose)
                .customView(dialogView, false)
                .negativeText(R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .positiveText(R.string.confirm)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String codeType = codeTypes[mCodeTypeIndex];
                        String codeLenText = codeLenEditText.getText().toString();

                        if (TextUtils.isEmpty(codeLenText)) {
                            codeLenEditText.setError(getString(R.string.code_length_empty_prompt));
                            return;
                        }
                        // (?<![0-9])[0-9]{4}(?![0-9])
                        String format = "(?<!%s)%s{%s}(?!%s)";
                        String codeRegex = String.format(format, codeType, codeType, codeLenText, codeType);
                        setText(mCodeRegexEditText, codeRegex);
                        dialog.dismiss();
                    }
                })
                .autoDismiss(false)
                .build();
        quickChooseDialog.show();
    }

    private void setText(EditText editText, CharSequence text) {
        if (!TextUtils.isEmpty(text)) {
            editText.setText(text);
            editText.setSelection(text.length());
        }
    }

    private void setError(EditText editText,@StringRes int textId) {
        setError(editText, getString(textId));
    }

    private void setError(EditText editText, String error) {
        editText.setError(error);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_rule, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rules_tick:
                saveIfValid();
                break;
            case R.id.action_save_as_template:
                saveAsTemplate();
                break;
            case R.id.action_rule_help:
                showCodeRuleHelp();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void saveIfValid() {
        if (!checkValid()) {
            return;
        }

        InputMethodManager imeManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imeManager != null && imeManager.isActive()) {
            imeManager.hideSoftInputFromWindow(
                    mCodeRegexEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        String company = mCompanyEditText.getText().toString();
        String keyword = mKeywordEditText.getText().toString();
        String codeRegex = mCodeRegexEditText.getText().toString();

        mCodeRule.setCompany(company);
        mCodeRule.setCodeKeyword(keyword);
        mCodeRule.setCodeRegex(codeRegex);

        DBManager dbManager = DBManager.get(mActivity);
        if (mRuleEditType == EDIT_TYPE_CREATE) {
            boolean duplicate = dbManager.isExist(mCodeRule);
            if (duplicate) {
                Toast.makeText(mActivity, R.string.rule_duplicated_prompt, Toast.LENGTH_LONG).show();
            } else {
                long id = dbManager.addSmsCodeRule(mCodeRule);
                mCodeRule.setId(id);
                XEventBus.post(new Event.OnRuleCreateOrUpdate(mRuleEditType, mCodeRule));
                mActivity.onBackPressed();
            }
        } else if (mRuleEditType == EDIT_TYPE_UPDATE) {
            dbManager.updateSmsCodeRule(mCodeRule);
            XEventBus.post(new Event.OnRuleCreateOrUpdate(mRuleEditType, mCodeRule));
            mActivity.onBackPressed();
        }
    }

    private boolean checkValid() {
        boolean companyValid = true;
        if (isEmpty(mCompanyEditText)) {
            setError(mCompanyEditText, R.string.rule_company_empty_hint);
            companyValid = false;
        }

        boolean keywordValid = true;
        if (isEmpty(mKeywordEditText)) {
            setError(mKeywordEditText, R.string.rule_keyword_empty_hint);
            keywordValid = false;
        }

        boolean codeRegexValid = true;
        if (isEmpty(mCodeRegexEditText)) {
            setError(mCodeRegexEditText, R.string.rule_code_regex_empty_hint);
            codeRegexValid = false;
        }

        return companyValid && keywordValid && codeRegexValid;
    }

    private boolean isEmpty(EditText editText) {
        return TextUtils.isEmpty(editText.getText());
    }

    private void loadTemplate() {
        ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
        singleThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                SmsCodeRule template = TemplateRuleManager.loadTemplate(mActivity);
                XEventBus.post(new Event.TemplateLoadEvent(template));
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTemplateLoaded(Event.TemplateLoadEvent event) {
        mCodeRule = event.template;

        setText(mCompanyEditText, mCodeRule.getCompany());
        setText(mKeywordEditText, mCodeRule.getCodeKeyword());
        setText(mCodeRegexEditText, mCodeRule.getCodeRegex());
    }

    private void saveAsTemplate() {
        // clear error info
        setError(mCompanyEditText, null);
        setError(mKeywordEditText, null);
        setError(mCodeRegexEditText, null);

        ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
        singleThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                String company = mCompanyEditText.getText().toString();
                String keyword = mKeywordEditText.getText().toString();
                String codeRegex = mCodeRegexEditText.getText().toString();

                SmsCodeRule template = new SmsCodeRule();

                template.setCompany(company);
                template.setCodeKeyword(keyword);
                template.setCodeRegex(codeRegex);
                boolean result = TemplateRuleManager.saveTemplate(mActivity, template);
                XEventBus.post(new Event.TemplateSaveEvent(result));
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTemplateSaved(Event.TemplateSaveEvent event) {
        @StringRes int msg;
        if (event.success) {
            msg = R.string.save_template_succeed;
        } else {
            msg = R.string.save_template_failed;
        }
        Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
    }

    private void showCodeRuleHelp() {
        String ruleHelpUrl = Utils.getProjectDocUrl(Const.PROJECT_DOC_BASE_URL, Const.DOC_SMS_CODE_RULE_HELP);
        Utils.showWebPage(mActivity, ruleHelpUrl);
    }

}
