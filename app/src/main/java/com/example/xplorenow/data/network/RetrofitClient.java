package com.example.xplorenow.data.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// instancia única de Retrofit que usan todos los fragments
public class RetrofitClient {

    // 10.0.2.2 es el localhost visto desde el emulador
    private static final String BASE_URL = "http://10.0.2.2:8000/";

    private static Retrofit instance;

    public static Retrofit getInstance() {
        if (instance == null) {
            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }
}