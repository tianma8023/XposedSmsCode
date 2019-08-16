package com.tianma.xsmscode.data.http.entity;

import com.google.gson.annotations.SerializedName;

//    {
//        "url": "https://api.github.com/repos/tianma8023/XposedSmsCode/releases/18839789",
//        "assets_url": "https://api.github.com/repos/tianma8023/XposedSmsCode/releases/18839789/assets",
//        "upload_url": "https://uploads.github.com/repos/tianma8023/XposedSmsCode/releases/18839789/assets{?name,label}",
//        "html_url": "https://github.com/tianma8023/XposedSmsCode/releases/tag/2.1.2",
//        "id": 18839789,
//        "node_id": "MDc6UmVsZWFzZTE4ODM5Nzg5",
//        "tag_name": "2.1.2",
//        "target_commitish": "master",
//        "name": "2.1.2",
//        "draft": false,
//        "author": {
//        "login": "tianma8023",
//        "id": 7833704,
//        "node_id": "MDQ6VXNlcjc4MzM3MDQ=",
//        "avatar_url": "https://avatars0.githubusercontent.com/u/7833704?v=4",
//        "gravatar_id": "",
//        "url": "https://api.github.com/users/tianma8023",
//        "html_url": "https://github.com/tianma8023",
//        "followers_url": "https://api.github.com/users/tianma8023/followers",
//        "following_url": "https://api.github.com/users/tianma8023/following{/other_user}",
//        "gists_url": "https://api.github.com/users/tianma8023/gists{/gist_id}",
//        "starred_url": "https://api.github.com/users/tianma8023/starred{/owner}{/repo}",
//        "subscriptions_url": "https://api.github.com/users/tianma8023/subscriptions",
//        "organizations_url": "https://api.github.com/users/tianma8023/orgs",
//        "repos_url": "https://api.github.com/users/tianma8023/repos",
//        "events_url": "https://api.github.com/users/tianma8023/events{/privacy}",
//        "received_events_url": "https://api.github.com/users/tianma8023/received_events",
//        "type": "User",
//        "site_admin": false
//        },
//        "prerelease": false,
//        "created_at": "2019-07-24T14:49:04Z",
//        "published_at": "2019-07-24T16:47:01Z",
//        "assets": [
//        {
//        "url": "https://api.github.com/repos/tianma8023/XposedSmsCode/releases/assets/13897694",
//        "id": 13897694,
//        "node_id": "MDEyOlJlbGVhc2VBc3NldDEzODk3Njk0",
//        "name": "XposedSmsCode_v2.1.2_190724_r.apk",
//        "label": null,
//        "uploader": {
//        "login": "tianma8023",
//        "id": 7833704,
//        "node_id": "MDQ6VXNlcjc4MzM3MDQ=",
//        "avatar_url": "https://avatars0.githubusercontent.com/u/7833704?v=4",
//        "gravatar_id": "",
//        "url": "https://api.github.com/users/tianma8023",
//        "html_url": "https://github.com/tianma8023",
//        "followers_url": "https://api.github.com/users/tianma8023/followers",
//        "following_url": "https://api.github.com/users/tianma8023/following{/other_user}",
//        "gists_url": "https://api.github.com/users/tianma8023/gists{/gist_id}",
//        "starred_url": "https://api.github.com/users/tianma8023/starred{/owner}{/repo}",
//        "subscriptions_url": "https://api.github.com/users/tianma8023/subscriptions",
//        "organizations_url": "https://api.github.com/users/tianma8023/orgs",
//        "repos_url": "https://api.github.com/users/tianma8023/repos",
//        "events_url": "https://api.github.com/users/tianma8023/events{/privacy}",
//        "received_events_url": "https://api.github.com/users/tianma8023/received_events",
//        "type": "User",
//        "site_admin": false
//        },
//        "content_type": "application/vnd.android.package-archive",
//        "state": "uploaded",
//        "size": 1342478,
//        "download_count": 64,
//        "created_at": "2019-07-24T16:46:34Z",
//        "updated_at": "2019-07-24T16:46:47Z",
//        "browser_download_url": "https://github.com/tianma8023/XposedSmsCode/releases/download/2.1.2/XposedSmsCode_v2.1.2_190724_r.apk"
//        }
//        ],
//        "tarball_url": "https://api.github.com/repos/tianma8023/XposedSmsCode/tarball/2.1.2",
//        "zipball_url": "https://api.github.com/repos/tianma8023/XposedSmsCode/zipball/2.1.2",
//        "body": "1. New: copy code to clipboard when notification clicked\r\n2. Optimization: optimize the algorithm of parsing SMS code.\r\n3. Change: Don't copy code to clipboard as default. Show code notification as default.\r\n\r\n<br/>\r\n\r\n1. 新增: 点击验证码通知，自动复制验证码到剪切板\r\n2. 优化：处理中文短信中验证码之间带空格的问题\r\n3. 变化：默认关闭 \"复制验证码到剪切板\" 选项，默认打开 “显示验证码通知\" 选项"
//    }
public class GithubRelease {

    @SerializedName("tag_name")
    private String tagName;
    @SerializedName("name")
    private String name;
    @SerializedName("body")
    private String body;

    public GithubRelease() {

    }

    public String getTagName() {
        return tagName;
    }

    public String getName() {
        return name;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "GithubRelease{" +
                "tagName='" + tagName + '\'' +
                ", name='" + name + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
