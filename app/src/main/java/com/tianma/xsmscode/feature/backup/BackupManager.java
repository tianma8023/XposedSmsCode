package com.tianma.xsmscode.feature.backup;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.tianma.xsmscode.common.utils.StorageUtils;
import com.tianma.xsmscode.common.utils.XLog;
import com.tianma.xsmscode.data.db.entity.SmsCodeRule;
import com.tianma.xsmscode.feature.backup.exception.BackupInvalidException;
import com.tianma.xsmscode.feature.backup.exception.VersionInvalidException;
import com.tianma.xsmscode.feature.backup.exception.VersionMissedException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BackupManager {

    private static final String BACKUP_DIRECTORY = "SmsCode";
    private static final String BACKUP_FILE_EXTENSION = ".scebak";
    private static final String BACKUP_FILE_NAME_PREFIX = "SmsCode-";

    private static final String BACKUP_MIME_TYPE = "application/json";
    private static final String BACKUP_FILE_AUTHORITY = BuildConfig.APPLICATION_ID + ".files";

    private BackupManager() {
    }

    public static File getBackupDir() {
        File sdcard = StorageUtils.getPublicDocumentsDir();
        return new File(sdcard, BACKUP_DIRECTORY);
    }

    public static String getBackupFileExtension() {
        return BACKUP_FILE_EXTENSION;
    }

    public static String getDefaultBackupFilename() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault());
        String dateStr = sdf.format(new Date());
        File backupDir = getBackupDir();
        String basename = BACKUP_FILE_NAME_PREFIX + dateStr;
        String filename = basename + BACKUP_FILE_EXTENSION;
        for (int i = 2; new File(backupDir, filename).exists(); i++) {
            filename = basename + "-" + i + BACKUP_FILE_EXTENSION;
        }
        return filename;
    }

    public static File[] getBackupFiles() {
        File backupDir = getBackupDir();
        File[] files = backupDir.listFiles((dir, name) -> name.endsWith(BACKUP_FILE_EXTENSION));

        if (files != null) {
            Arrays.sort(files, (f1, f2) -> {
                String s1 = f1.getName();
                String s2 = f2.getName();
                int extLength = BACKUP_FILE_EXTENSION.length();
                s1 = s1.substring(0, s1.length() - extLength);
                s2 = s2.substring(0, s2.length() - extLength);
                return s1.compareTo(s2);
            });
        }
        return files;
    }

    public static ExportResult exportRuleList(File file, List<SmsCodeRule> ruleList) {
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }

        try (RuleExporter exporter = new RuleExporter(file)) {
            exporter.doExport(ruleList);
            return ExportResult.SUCCESS;
        } catch (IOException e) {
            XLog.e("Export SmsCode rules failed", e);
            return ExportResult.FAILED;
        }
    }

    public static ExportResult exportRuleList(Context context, Uri uri, List<SmsCodeRule> ruleList) {
        try (RuleExporter exporter = new RuleExporter(context.getContentResolver().openOutputStream(uri))) {
            exporter.doExport(ruleList);
            return ExportResult.SUCCESS;
        } catch (IOException e) {
            XLog.e("Export SmsCode rules failed", e);
            return ExportResult.FAILED;
        }
    }

    /**
     * 获取导出规则列表的 SAF (Storage Access Framework) 的 Intent
     */
    public static Intent getExportRuleListSAFIntent() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(BACKUP_MIME_TYPE);
        intent.putExtra(Intent.EXTRA_TITLE, getDefaultBackupFilename());

        return intent;
    }

    public static ImportResult importRuleList(Context context, Uri uri, boolean retain) {
        RuleImporter ruleImporter = null;
        try {
            ruleImporter = new RuleImporter(context.getContentResolver().openInputStream(uri));
            ruleImporter.doImport(context, retain);
            return ImportResult.SUCCESS;
        } catch (IOException e) {
            XLog.e("Error occurs in importRuleList", e);
            return ImportResult.READ_FAILED;
        } catch (VersionMissedException e) {
            XLog.e("Error occurs in importRuleList", e);
            return ImportResult.VERSION_MISSED;
        } catch (VersionInvalidException e) {
            XLog.e("Error occurs in importRuleList", e);
            return ImportResult.VERSION_UNKNOWN;
        } catch (BackupInvalidException e) {
            XLog.e("Error occurs in importRuleList", e);
            return ImportResult.BACKUP_INVALID;
        } finally {
            if (ruleImporter != null) {
                ruleImporter.close();
            }
        }
    }

    /**
     * 获取导入规则列表的 SAF (Storage Access Framework) 的 Intent
     */
    public static Intent getImportRuleListSAFIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(BACKUP_MIME_TYPE);
        intent.putExtra(Intent.EXTRA_TITLE, getDefaultBackupFilename());

        return intent;
    }

    public static void shareBackupFile(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_SEND);

        Uri uri = FileProvider.getUriForFile(context, BACKUP_FILE_AUTHORITY, file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType(BACKUP_MIME_TYPE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(Intent.createChooser(intent, null));
    }
}
