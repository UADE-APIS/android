package com.example.xplorenow.data.network;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitProvider {

    private static final String BASE_URL = "http://10.0.2.2:8000/";

    private RetrofitProvider() {
    }

    public static OkHttpClient buildDefaultClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
    }

    public static OkHttpClient buildAuthedClient(Interceptor authInterceptor) {
        return buildDefaultClient().newBuilder()
                .addInterceptor(authInterceptor)
                .build();
    }

    public static Retrofit getRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
