package com.example.xplorenow.data.network;

import com.example.xplorenow.data.model.ApiResponse;
import com.example.xplorenow.data.model.CheckUsernameRequest;
import com.example.xplorenow.data.model.OtpRequest;
import com.example.xplorenow.data.model.RegisterData;
import com.example.xplorenow.data.model.RegisterRequest;
import com.example.xplorenow.data.model.VerifyOtpData;
import com.example.xplorenow.data.model.VerifyOtpRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

// define todos los endpoints que consume la app
public interface ApiService {

    // -- auth --

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

    @POST("api/auth/request-login-otp/")
    Call<ApiResponse<Void>> requestLoginOtp(@Body OtpRequest body);

    @POST("api/auth/verify-login-otp/")
    Call<ApiResponse<VerifyOtpData>> verifyLoginOtp(@Body VerifyOtpRequest body);
}