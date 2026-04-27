package com.example.xplorenow.data.network;

import com.example.xplorenow.data.model.ActivitiesListResponse;
import com.example.xplorenow.data.model.Activity;
import com.example.xplorenow.data.model.ApiResponse;
import com.example.xplorenow.data.model.Booking;
import com.example.xplorenow.data.model.BookingRequest;
import com.example.xplorenow.data.model.CheckUsernameRequest;
import com.example.xplorenow.data.model.HistoryListResponse;
import com.example.xplorenow.data.model.News;
import com.example.xplorenow.data.model.NewsListResponse;
import com.example.xplorenow.data.model.OtpRequest;
import com.example.xplorenow.data.model.RegisterData;
import com.example.xplorenow.data.model.RegisterRequest;
import com.example.xplorenow.data.model.Review;
import com.example.xplorenow.data.model.ReviewRequest;
import com.example.xplorenow.data.model.User;
import com.example.xplorenow.data.model.VerifyOtpData;
import com.example.xplorenow.data.model.VerifyOtpRequest;
import com.example.xplorenow.data.network.dto.ChangePasswordRequest;
import com.example.xplorenow.data.network.dto.LoginClassicRequest;
import com.example.xplorenow.data.network.dto.LoginOtpRequest;
import com.example.xplorenow.data.network.dto.LogoutRequest;
import com.example.xplorenow.data.network.dto.MeResponseData;
import com.example.xplorenow.data.network.dto.RequestOtpRequest;
import com.example.xplorenow.data.network.dto.UpdateProfileRequest;
import com.example.xplorenow.data.network.dto.WrappedResponse;
import com.example.xplorenow.data.network.dto.auth.AuthTokensResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
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

    // PROFILE

    @GET("api/auth/me/")
    Call<WrappedResponse<MeResponseData>> getMe();

    @PATCH("api/auth/update-profile/")
    Call<WrappedResponse<MeResponseData>> updateProfile(@Body UpdateProfileRequest request);

    @POST("api/auth/change-password/")
    Call<WrappedResponse<Void>> changePassword(@Body ChangePasswordRequest request);

    // BOOKING ACTIVITIES

    @GET("api/activities/{id}/")
    Call<ApiResponse<Activity>> getActivity(@Path("id") int id);

    @POST("api/activities/bookings/create/")
    Call<ApiResponse<Booking>> createBooking(@Body BookingRequest request);

    @GET("api/activities/bookings/me/")
    Call<ApiResponse<List<Booking>>> getMyBookings();

    @POST("api/activities/bookings/{id}/cancel/")
    Call<ApiResponse<Booking>> cancelBooking(@Path("id") int id);

    // HISTORY

    @GET("api/activities/history/")
    Call<HistoryListResponse> getHistory(@QueryMap Map<String, String> query);

    @POST("api/activities/history/{booking_id}/review/")
    Call<ApiResponse<Review>> createReview(@Path("booking_id") int bookingId, @Body ReviewRequest body);

    // FAVORITES

    @POST("api/activities/{id}/favorite/")
    Call<WrappedResponse<Void>> toggleFavorite(@Path("id") int id);

    @GET("api/activities/favorites/")
    Call<ActivitiesListResponse> getMyFavorites(@QueryMap Map<String, String> query);

    @GET("api/activities/news/")
    Call<NewsListResponse> getNews(@QueryMap Map<String, String> query);

    @GET("api/activities/news/{id}/")
    Call<ApiResponse<News>> getNewsDetail(@Path("id") int id);
}
