package com.tianma.xsmscode.feature.backup;

import android.content.Context;

import com.tianma.xsmscode.feature.backup.exception.BackupInvalidException;
import com.tianma.xsmscode.feature.backup.exception.VersionInvalidException;
import com.tianma.xsmscode.feature.backup.exception.VersionMissedException;
import com.tianma.xsmscode.data.db.DBManager;
import com.tianma.xsmscode.data.db.entity.SmsCodeRule;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * SmsCode rule importer
 */
public class RuleImporter implements Closeable{

    private InputStream mJsonStream;

    public RuleImporter(InputStream in) {
        mJsonStream = in;
    }

    public RuleImporter(File file) throws FileNotFoundException {
        this(new FileInputStream(file));
    }

    /**
     * perform import data from backup file.
     * @param context context
     * @param retain whether it retains current rules or not.
     */
    public void doImport(Context context, boolean retain) throws BackupInvalidException {
        JsonReader jsonReader = new JsonReader(new InputStreamReader(mJsonStream));

        try {
            JsonElement jsonElement = new JsonParser().parse(jsonReader);
            if(!jsonElement.isJsonObject()) {
                throw new BackupInvalidException();
            }
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            if(jsonObject.has(BackupConst.KEY_VERSION)) {
                int version = jsonObject.get(BackupConst.KEY_VERSION).getAsInt();
                if (version == 1) {
                    doImportVersion1(context, jsonObject, retain);
                } else {
                    throw new VersionInvalidException("Invalid backup version");
                }
            } else {
                throw new VersionMissedException("Backup version property missed");
            }
        } catch (JsonParseException ex) {
            // json syntax exception or json parse exception
            throw new BackupInvalidException(ex);
        }

    }

    private void doImportVersion1(Context context, JsonObject jsonObject, boolean retain) throws BackupInvalidException {
        JsonArray ruleArray = jsonObject.get(BackupConst.KEY_RULES).getAsJsonArray();
        if (ruleArray == null) {
            return;
        }
        List<SmsCodeRule> ruleList = readRuleList(ruleArray);
        if (ruleList != null && !ruleList.isEmpty()) {
            writeRuleListToDB(context, ruleList, retain);
        }
    }

    private List<SmsCodeRule> readRuleList(JsonArray ruleArray) throws BackupInvalidException {
        List<SmsCodeRule> ruleList = new ArrayList<>();
        for(JsonElement ruleJson : ruleArray) {
            ruleList.add(readRule(ruleJson.getAsJsonObject()));
        }
        return ruleList;
    }

    private SmsCodeRule readRule(JsonObject ruleObject) throws BackupInvalidException {
        try {
            String company = ruleObject.get(BackupConst.KEY_COMPANY).getAsString();
            String codeKeyword = ruleObject.get(BackupConst.KEY_CODE_KEYWORD).getAsString();
            String codeRegex = ruleObject.get(BackupConst.KEY_CODE_REGEX).getAsString();

            return new SmsCodeRule(company, codeKeyword, codeRegex);
        } catch (Exception e) {
            throw new BackupInvalidException(e);
        }
    }

    private void writeRuleListToDB(Context context, List<SmsCodeRule> ruleList, boolean retain) {
        DBManager dbManager = DBManager.get(context);
        if (!retain) {
            dbManager.removeAllSmsCodeRules();
        }
        dbManager.addSmsCodeRules(ruleList);
    }

    @Override
    public void close() {
        if (mJsonStream != null) {
            try {
                mJsonStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
