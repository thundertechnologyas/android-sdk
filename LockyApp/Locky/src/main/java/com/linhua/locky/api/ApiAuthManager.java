package com.linhua.locky.api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiAuthManager {
    private static ApiAuthManager manager;
    private String baseUrl = "https://auth.thundertech.no/";
    private HttpApi httpApi;

    private ApiAuthManager() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).client(okHttpClient).addConverterFactory(
                GsonConverterFactory.create()).build();
        httpApi = retrofit.create(HttpApi.class);
    }

    public HttpApi getHttpApi() {
        return httpApi;
    }

    public static ApiAuthManager getInstance() {
        if (manager == null) {
            synchronized (ApiManager.class) {
                if (manager == null) {
                    manager = new ApiAuthManager();
                }
            }
        }
        return manager;
    }
}
