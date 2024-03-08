package com.tianma.xsmscode.common.utils;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestUtils {
    private int times = 0;

    public synchronized void run(HashMap<String, String> data, String str_url) {
        String errmsg = null;
        while (times < 5) {
            errmsg = "";
            OkHttpClient client = new OkHttpClient();

            JSONObject jsonBody = new JSONObject(data);
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, jsonBody.toString());

            Request request = new Request.Builder()
                    .url(str_url)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String html = response.body().string();
                XLog.i(html);
                if (response.isSuccessful()) {
                    XLog.i("请求成功");
                } else {
                    errmsg = String.format("请求失败,状态码为:%s", response.code());
                    XLog.e("请求失败:%s", html.substring(0, 20));
                    continue;
                }
                break;
            } catch (Exception e) {
                times += 1;
                errmsg = Log.getStackTraceString(e);
                XLog.e(errmsg);
            }
        }
        if (!TextUtils.isEmpty(errmsg)) {
            XLog.e(errmsg);
        }

    }

}