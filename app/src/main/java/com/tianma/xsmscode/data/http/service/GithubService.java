package com.tianma.xsmscode.data.http.service;

import com.tianma.xsmscode.data.http.entity.GithubRelease;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Retrofit Service for GitHub
 */
public interface GithubService {

    @GET("/repos/{username}/{repoName}/releases/latest")
    Observable<GithubRelease> getLatestRelease(@Path("username") String username,
                                               @Path("repoName") String repoName);

}
