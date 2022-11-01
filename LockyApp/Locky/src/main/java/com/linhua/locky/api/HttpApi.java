package com.linhua.locky.api;

import retrofit2.http.GET;
import retrofit2.http.Query;

public interface HttpApi {
    @GET("api/simpleauth/start")
    void startVerify(@Query("email")String email, @Query("domain")String domain);

}