package com.example.xplorenow.data.network;

import android.content.Context;

import com.example.xplorenow.data.session.SessionStore;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final Context appContext;

    public AuthInterceptor(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        String token = null;
        try {
            token = SessionStore.getInstance(appContext).getAccessToken().blockingGet();
        } catch (Throwable ignored) {
        }

        if (token == null || token.trim().isEmpty()) {
            return chain.proceed(original);
        }

        Request authed = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(authed);
    }
}
