package com.github.tianma8023.xposed.smscode.event;

import com.github.tianma8023.xposed.smscode.app.rule.RuleEditFragment;
import com.github.tianma8023.xposed.smscode.backup.ExportResult;
import com.github.tianma8023.xposed.smscode.entity.SmsCodeRule;

import java.io.File;

public class Event {

    private Event() {
    }
    /**
     * Start to edit codeRule event
     */
    public static class StartRuleEditEvent {
        public @RuleEditFragment.RuleEditType int type;
        public SmsCodeRule codeRule;
        public StartRuleEditEvent(@RuleEditFragment.RuleEditType int type, SmsCodeRule codeRule) {
            this.type = type;
            this.codeRule = codeRule;
        }
    }

    /**
     * Rule create or update event
     */
    public static class OnRuleCreateOrUpdate {
        public SmsCodeRule codeRule;
        public @RuleEditFragment.RuleEditType int type;
        public OnRuleCreateOrUpdate(@RuleEditFragment.RuleEditType int type, SmsCodeRule rule) {
            this.codeRule = rule;
            this.type = type;
        }
    }

    /**
     * Save template rule event
     */
    public static class TemplateSaveEvent {
        public boolean success;
        public TemplateSaveEvent(boolean success) {
            this.success = success;
        }
    }

    /**
     * Load template rule event
     */
    public static class TemplateLoadEvent {
        public SmsCodeRule template;
        public TemplateLoadEvent(SmsCodeRule template) {
            this.template = template;
        }
    }

    public static class ExportEvent {
        public ExportResult result;
        public File file;
        public ExportEvent(ExportResult result, File file) {
            this.result = result;
            this.file = file;
        }
    }

}
