package com.linhua.locky.api;

import com.linhua.locky.bean.LockModel;
import com.linhua.locky.bean.LockyPackage;
import com.linhua.locky.bean.TokenModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface HttpApi {
    @GET("api/simpleauth/start")
    Call<Void> startVerify(@Query("email")String email, @Query("domain")String domain);

    @GET("api/simpleauth/verify")
    Call<TokenModel> verify(@Query("email")String email, @Query("code")String code, @Query("domain")String domain);

    @POST("api/simpleauth/mobilekeys")
    Call<ArrayList<String>> getMobileKeys(@Query("domain") String domain, @Query("token")String token);

    @GET("lockyapi/mobilekey/devices")
    Call<ArrayList<LockModel>> getAllLocks(@Header("tenantId")String tenantId, @Header("token")String token);

    @GET("lockyapi/mobilekey/{signal}")
    Call<LockyPackage> downloadPackage(@Path(value = "signal", encoded = true) String signal, @Query("deviceId")String deviceId, @Header("tenantId")String tenantId, @Header("token")String token);

    @POST("lockyapi/mobilekey/msgdelivered?deviceId={deviceId}")
    Call<Void> messageDelivered(@Path(value = "deviceId", encoded = true) String deviceId, @Body LockyPackage payload, @Header("Content-Type")String contentType, @Header("tenantId")String tenantId, @Header("token")String token);


}