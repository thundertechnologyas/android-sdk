package com.linhua.locky.api;

import com.linhua.locky.bean.TokenModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface HttpApi {
    @GET("api/simpleauth/start")
    Call<Integer> startVerify(@Query("email")String email, @Query("domain")String domain);

    @GET("api/simpleauth/verify")
    Call<TokenModel> verify(@Query("email")String email, @Query("code")String code, @Query("domain")String domain);

    @POST("api/simpleauth/mobilekeys")
    Call<String> getMobileKeys(@Field("domain") String domain, @Query("token")String token);

    @GET("lockyapi/mobilekey/devices")
    Call<TokenModel> getAllLocks(@Header("tenantId")String tenantId, @Header("token")String token);
}