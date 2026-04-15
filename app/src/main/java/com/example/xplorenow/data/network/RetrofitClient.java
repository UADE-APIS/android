package com.example.xplorenow.data.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit sin auth para endpoints públicos (registro, OTP, listado).
 */
public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8000/";

    private static Retrofit instance;
    private static ApiService apiService;

    public static synchronized Retrofit getInstance() {
        if (instance == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();
            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }

    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            apiService = getInstance().create(ApiService.class);
        }
        return apiService;
    }
}
