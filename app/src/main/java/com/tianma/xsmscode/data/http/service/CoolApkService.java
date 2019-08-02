package com.tianma.xsmscode.data.http.service;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * retrofit service for coolapk.com
 */
public interface CoolApkService {

    @GET("/apk/{packageName}")
    Observable<String> getLatestRelease(@Path("packageName") String packageName);

}
