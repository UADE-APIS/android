package com.example.xplorenow.network;

import com.example.xplorenow.network.dto.LogoutRequest;
import com.example.xplorenow.network.dto.LoginClassicRequest;
import com.example.xplorenow.network.dto.LoginOtpRequest;
import com.example.xplorenow.network.dto.RequestOtpRequest;
import com.example.xplorenow.network.dto.WrappedResponse;
import com.example.xplorenow.network.dto.auth.AuthTokensResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/auth/request-login-otp/")
    Call<WrappedResponse<Void>> requestLoginOtp(@Body RequestOtpRequest body);

    @POST("api/auth/verify-login-otp/")
    Call<WrappedResponse<AuthTokensResponse>> verifyLoginOtp(@Body LoginOtpRequest body);

    @POST("api/auth/login/")
    Call<WrappedResponse<AuthTokensResponse>> loginClassic(@Body LoginClassicRequest body);

    @POST("api/auth/logout/")
    Call<WrappedResponse<Void>> logout(@Body LogoutRequest body);
}

