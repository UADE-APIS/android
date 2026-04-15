package com.example.xplorenow.data.network;

import com.example.xplorenow.data.model.ActivitiesListResponse;
import com.example.xplorenow.data.model.ApiResponse;
import com.example.xplorenow.data.model.CheckUsernameRequest;
import com.example.xplorenow.data.model.OtpRequest;
import com.example.xplorenow.data.model.RegisterData;
import com.example.xplorenow.data.model.RegisterRequest;
import com.example.xplorenow.data.model.VerifyOtpData;
import com.example.xplorenow.data.model.VerifyOtpRequest;
import com.example.xplorenow.data.network.dto.LoginClassicRequest;
import com.example.xplorenow.data.network.dto.LoginOtpRequest;
import com.example.xplorenow.data.network.dto.LogoutRequest;
import com.example.xplorenow.data.network.dto.RequestOtpRequest;
import com.example.xplorenow.data.network.dto.WrappedResponse;
import com.example.xplorenow.data.network.dto.auth.AuthTokensResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface ApiService {

    @GET("api/activities/")
    Call<ActivitiesListResponse> getActivities(@QueryMap Map<String, String> query);

    @POST("api/auth/login/")
    Call<WrappedResponse<AuthTokensResponse>> loginClassic(@Body LoginClassicRequest body);

    @POST("api/auth/request-login-otp/")
    Call<WrappedResponse<Void>> requestLoginOtp(@Body RequestOtpRequest body);

    @POST("api/auth/verify-login-otp/")
    Call<WrappedResponse<AuthTokensResponse>> verifyLoginOtp(@Body LoginOtpRequest body);

    @POST("api/auth/logout/")
    Call<WrappedResponse<Void>> logout(@Body LogoutRequest body);

    @POST("api/auth/check-email/")
    Call<ApiResponse<Void>> checkEmail(@Body OtpRequest body);

    @POST("api/auth/check-username/")
    Call<ApiResponse<Void>> checkUsername(@Body CheckUsernameRequest body);

    @POST("api/auth/request-otp/")
    Call<ApiResponse<Void>> requestOtp(@Body OtpRequest body);

    @POST("api/auth/resend-otp/")
    Call<ApiResponse<Void>> resendOtp(@Body OtpRequest body);

    @POST("api/auth/verify-otp/")
    Call<ApiResponse<VerifyOtpData>> verifyOtp(@Body VerifyOtpRequest body);

    @POST("api/auth/register/")
    Call<ApiResponse<RegisterData>> register(@Body RegisterRequest body);
}
