package com.tianma.xsmscode.ui.block;

import com.tianma.xsmscode.common.utils.JsonUtils;
import com.tianma.xsmscode.common.utils.StorageUtils;
import com.tianma.xsmscode.common.utils.XLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Put and get blocked app info in files.
 */
public class BlockedAppStoreManager {

    private static final String CODE_RULE_TEMPLATE_FILE_NAME = "code_rule_template";
    private static final String CODE_RULES_FILE_NAME = "code_rules";
    private static final String BLOCKED_APPS_FILE_NAME = "blocked_apps";

    public enum EntityType {
        BLOCKED_APP,
        CODE_RULES,
        CODE_RULE_TEMPLATE,
    }

    private BlockedAppStoreManager() {
    }

    public static <T> void storeEntitiesToFile(EntityType entityType, List<T> entities) {
        OutputStreamWriter osw = null;
        try {
            File storeFile = getStoreFile(entityType);
            osw = new OutputStreamWriter(new FileOutputStream(storeFile), StandardCharsets.UTF_8);

            JsonUtils.listToJson(entities, osw, true);

            // set file world writable
            StorageUtils.setFileWorldWritable(storeFile, 0);
        } catch (Exception e) {
            XLog.e("store entities to file failed", e);
        } finally {
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException ioException) {
                    // ignore
                }
            }
        }
    }

    private static <T> File getStoreFile(EntityType entityType) {
        String filename;
        switch (entityType) {
            case BLOCKED_APP:
                filename = BLOCKED_APPS_FILE_NAME;
                break;
            case CODE_RULES:
                filename = CODE_RULES_FILE_NAME;
                break;
            case CODE_RULE_TEMPLATE:
                filename = CODE_RULE_TEMPLATE_FILE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unknown EntityType:" + entityType.toString());
        }
        return new File(StorageUtils.getFilesDir(), filename);
    }

    public static <T> List<T> loadEntitiesFromFile(EntityType entityType, Class<T> entityClass) {
        File templateFile = getStoreFile(entityType);
        if (!templateFile.exists()) {
            return new ArrayList<>();
        }
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(
                    new FileInputStream(templateFile), StandardCharsets.UTF_8);

            return JsonUtils.jsonToList(isr, entityClass, true);
        } catch (Exception e) {
            XLog.e("load entities from file failed", e);
        } finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new ArrayList<>();
    }

}
