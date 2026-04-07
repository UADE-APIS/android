package com.example.xplorenow.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitProvider {
    private static volatile Retrofit retrofit;

    private RetrofitProvider() {}

    // Android emulator -> host machine localhost
    public static final String BASE_URL = "http://10.0.2.2:8000/";

    public static Retrofit getRetrofit(OkHttpClient client) {
        if (retrofit == null) {
            synchronized (RetrofitProvider.class) {
                if (retrofit == null) {
                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static OkHttpClient buildDefaultClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
    }
}

