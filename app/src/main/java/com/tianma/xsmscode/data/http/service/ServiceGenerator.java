package com.tianma.xsmscode.data.http.service;

import android.util.ArrayMap;

import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ServiceGenerator {

    private Map<String, Retrofit> mRetrofitMap;

    private OkHttpClient mOkHttpClient;

    private static class InstanceHolder {
        private static final ServiceGenerator INSTANCE = new ServiceGenerator();
    }

    private ServiceGenerator() {
        mRetrofitMap = new ArrayMap<>();
        mOkHttpClient = new OkHttpClient.Builder().build();
    }

    public static ServiceGenerator getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public <T> T createService(String baseUrl, Class<T> serviceClass) {
        Retrofit retrofit = mRetrofitMap.get(baseUrl);
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(mOkHttpClient)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            mRetrofitMap.put(baseUrl, retrofit);
        }
        return retrofit.create(serviceClass);
    }
}
