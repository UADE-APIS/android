package com.example.xplorenow.network;

import com.example.xplorenow.models.Activity;
import com.example.xplorenow.models.ApiResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface ApiService {
    
    @GET("api/activities")
    Call<ApiResponse<List<Activity>>> getActivities(@QueryMap Map<String, String> filters);

    @GET("api/activities/{id}")
    Call<ApiResponse<Activity>> getActivityDetail(@Path("id") int id);
}
