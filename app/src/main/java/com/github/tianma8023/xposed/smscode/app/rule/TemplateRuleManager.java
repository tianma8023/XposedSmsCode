package com.github.tianma8023.xposed.smscode.app.rule;

import android.content.Context;

import com.github.tianma8023.xposed.smscode.entity.SmsCodeRule;
import com.github.tianma8023.xposed.smscode.utils.StorageUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class TemplateRuleManager {

    private static final String RULE_TEMPLATE_DIRECTORY = "template";

    private static final String RULE_TEMPLATE_FILENAME = "rule-template.sce";


    /**
     * Get SMS code rule template dir
     */
    public static File getRuleTemplateDir(Context context) {
        if (StorageUtils.isSDCardMounted()) {
            return context.getExternalFilesDir(RULE_TEMPLATE_DIRECTORY);
        } else {
            return new File(context.getFilesDir(), RULE_TEMPLATE_DIRECTORY);
        }
    }

    /**
     * Get SMS code rule template file
     */
    public static File getRuleTemplateFile(Context context) {
        return new File(getRuleTemplateDir(context), RULE_TEMPLATE_FILENAME);
    }

    public static boolean saveTemplate(Context context, SmsCodeRule template) {
        File templateFile = getRuleTemplateFile(context);
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter(
                    new FileOutputStream(templateFile), "UTF-8");
            Gson gson = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create();
            gson.toJson(template, osw);
            return true;
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static SmsCodeRule loadTemplate(Context context) {
        File templateFile = getRuleTemplateFile(context);
        InputStreamReader isr = null;
        SmsCodeRule smsCodeRule = new SmsCodeRule();
        try {
            isr = new InputStreamReader(
                    new FileInputStream(templateFile), "UTF-8");

            Gson gson = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create();
            smsCodeRule = gson.fromJson(isr, SmsCodeRule.class);
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return smsCodeRule;
    }

}
