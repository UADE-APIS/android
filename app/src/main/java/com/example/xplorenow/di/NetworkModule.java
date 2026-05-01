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

    private static final String BASE_URL = "http://10.0.2.2:8000/";

    private NetworkModule() {
    }

    @Provides
    @Singleton
    static OkHttpClient provideOkHttpClient(TokenManager tokenManager, AuthEventBus authEventBus) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    String token = tokenManager.getAccessToken();
                    Request request = original;
                    if (token != null && !token.trim().isEmpty()) {
                        String normalized = token.trim();
                        if (normalized.regionMatches(true, 0, "Bearer ", 0, 7)) {
                            normalized = normalized.substring(7).trim();
                        }
                        request = original.newBuilder()
                                .header("Authorization", "Bearer " + normalized)
                                .build();
                    }
                    okhttp3.Response response = chain.proceed(request);
                    if (response.code() == 401) {
                        tokenManager.clear();
                        authEventBus.emitSessionExpired();
                    }
                    return response;
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
