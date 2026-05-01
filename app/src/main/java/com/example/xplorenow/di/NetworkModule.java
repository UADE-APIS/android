package com.example.xplorenow.di;

import com.example.xplorenow.data.network.ApiService;
import com.example.xplorenow.data.session.TokenManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public final class NetworkModule {

    private static final String BASE_URL = "http://localhost:8000/";

    private NetworkModule() {
    }

    @Provides
    @Singleton
    static OkHttpClient provideOkHttpClient(TokenManager tokenManager) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    String token = tokenManager.getAccessToken();
                    if (token == null || token.trim().isEmpty()) {
                        return chain.proceed(original);
                    }
                    String normalized = token.trim();
                    if (normalized.regionMatches(true, 0, "Bearer ", 0, 7)) {
                        normalized = normalized.substring(7).trim();
                    }
                    Request authed = original.newBuilder()
                            .header("Authorization", "Bearer " + normalized)
                            .build();
                    return chain.proceed(authed);
                })
                .build();
    }

    @Provides
    @Singleton
    static Retrofit provideRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    static ApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }
}
