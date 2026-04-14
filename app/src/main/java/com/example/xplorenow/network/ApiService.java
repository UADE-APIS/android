package com.example.xplorenow.network;

import com.example.xplorenow.models.Activity;
import com.example.xplorenow.models.ApiResponse;
import com.example.xplorenow.network.dto.LogoutRequest;
import com.example.xplorenow.network.dto.LoginClassicRequest;
import com.example.xplorenow.network.dto.LoginOtpRequest;
import com.example.xplorenow.network.dto.RequestOtpRequest;
import com.example.xplorenow.network.dto.WrappedResponse;
import com.example.xplorenow.network.dto.auth.AuthTokensResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface ApiService {

    @GET("api/activities")
    Call<ApiResponse<List<Activity>>> getActivities(@QueryMap Map<String, String> filters);

    @GET("api/activities/{id}")
    Call<ApiResponse<Activity>> getActivityDetail(@Path("id") int id);

    @POST("api/auth/request-login-otp/")
    Call<WrappedResponse<Void>> requestLoginOtp(@Body RequestOtpRequest body);

    @POST("api/auth/verify-login-otp/")
    Call<WrappedResponse<AuthTokensResponse>> verifyLoginOtp(@Body LoginOtpRequest body);

    @POST("api/auth/login/")
    Call<WrappedResponse<AuthTokensResponse>> loginClassic(@Body LoginClassicRequest body);

    @POST("api/auth/logout/")
    Call<WrappedResponse<Void>> logout(@Body LogoutRequest body);
}
