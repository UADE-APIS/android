package com.example.xplorenow.notifications;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.xplorenow.data.network.ApiService;
import com.example.xplorenow.data.network.dto.FcmTokenRequest;
import com.example.xplorenow.data.network.dto.WrappedResponse;
import com.example.xplorenow.data.session.TokenManager;
import com.google.firebase.messaging.FirebaseMessaging;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class FcmTokenRegistrar {

    private static final String TAG = "XploreFCM";
    private static final String PLATFORM_ANDROID = "android";

    private final ApiService api;
    private final TokenManager tokenManager;

    @Inject
    public FcmTokenRegistrar(ApiService api, TokenManager tokenManager) {
        this.api = api;
        this.tokenManager = tokenManager;
    }

    public void registerCurrentToken() {
        if (!tokenManager.isLoggedIn()) {
            return;
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(this::registerToken)
                .addOnFailureListener(e -> Log.w(TAG, "No se pudo obtener el token FCM", e));
    }

    public void registerToken(String token) {
        if (token == null || token.trim().isEmpty() || !tokenManager.isLoggedIn()) {
            return;
        }

        api.registerFcmToken(new FcmTokenRequest(token.trim(), PLATFORM_ANDROID))
                .enqueue(new Callback<WrappedResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull Call<WrappedResponse<Void>> call,
                                           @NonNull Response<WrappedResponse<Void>> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Token FCM registrado en backend");
                        } else {
                            Log.w(TAG, "No se pudo registrar token FCM. HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<WrappedResponse<Void>> call, @NonNull Throwable t) {
                        Log.w(TAG, "Error registrando token FCM", t);
                    }
                });
    }
}
