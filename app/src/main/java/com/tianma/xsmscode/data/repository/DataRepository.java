package com.tianma.xsmscode.data.repository;

import com.github.tianma8023.xposed.smscode.BuildConfig;
import com.tianma.xsmscode.data.db.entity.ApkVersion;
import com.tianma.xsmscode.data.http.ApiConst;
import com.tianma.xsmscode.data.http.service.CoolApkService;
import com.tianma.xsmscode.data.http.service.GithubService;
import com.tianma.xsmscode.data.http.service.ServiceGenerator;

import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public class DataRepository {

    private DataRepository() {

    }

    private static boolean isInChina() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        return "zh".equalsIgnoreCase(language);
    }

    public static Observable<ApkVersion> getLatestVersion() {
        boolean isInChina = isInChina();

        CoolApkService coolApkService = ServiceGenerator.getInstance()
                .createService(ApiConst.COOLAPK_BASE_URL, CoolApkService.class);
        Observable<ApkVersion> dataFromCoolApk = coolApkService.getLatestRelease(BuildConfig.APPLICATION_ID)
                .map(ApkVersionHelper::parseFromCoolApk);

        GithubService githubService = ServiceGenerator.getInstance()
                .createService(ApiConst.GITHUB_BASE_URL, GithubService.class);
        Observable<ApkVersion> dataFromGithub = githubService.getLatestRelease(ApiConst.GITHUB_USERNAME, ApiConst.GITHUB_REPO_NAME)
                .map(githubRelease -> {
                    String regex = "<br/>|<br>";
                    String[] arr = githubRelease.getBody().split(regex);
                    String versionInfo;
                    if (arr.length >= 2) {
                        versionInfo = isInChina ? arr[1].trim() : arr[0].trim();
                    } else {
                        versionInfo = githubRelease.getBody().replaceAll(regex, "");
                    }
                    return new ApkVersion(githubRelease.getName(), versionInfo);
                });

        if (isInChina) {
            // In China region
            // Firstly, request data from coolapk.
            // If error throws, then request data from github.
            return dataFromCoolApk
                    .onErrorResumeNext((Function<Throwable, ObservableSource<? extends ApkVersion>>) throwable -> dataFromGithub);
        } else {
            // In other regions
            // Firstly, request data from GitHub.
            // If error throws, then request data from coolapk.
            return dataFromGithub
                    .onErrorResumeNext((Function<Throwable, ObservableSource<? extends ApkVersion>>) throwable -> dataFromCoolApk);
        }
    }

}
