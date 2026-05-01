package com.example.xplorenow.data.session;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class TokenManager {

    private static final String PREFS_NAME = "auth_tokens";
    private static final String KEY_ACCESS = "access_token";
    private static final String KEY_REFRESH = "refresh_token";
    private static final String KEY_BIOMETRIC = "biometric_enabled";

    private static final String ENCRYPTED_PREFS_NAME = "auth_tokens_enc";
    private static final String KEY_ENCRYPTED_ACCESS = "encrypted_access_token";

    private final Context context;
    private final SharedPreferences prefs;

    @Inject
    public TokenManager(@ApplicationContext Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveTokens(String access, String refresh) {
        prefs.edit()
                .putString(KEY_ACCESS, access)
                .putString(KEY_REFRESH, refresh)
                .apply();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH, null);
    }

    public boolean isLoggedIn() {
        String t = getAccessToken();
        return t != null && !t.trim().isEmpty();
    }

    public boolean isBiometricEnabled() {
        return prefs.getBoolean(KEY_BIOMETRIC, false);
    }

    public void setBiometricEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BIOMETRIC, enabled).apply();
    }

    public void clear() {
        prefs.edit()
                .remove(KEY_ACCESS)
                .remove(KEY_REFRESH)
                .remove(KEY_BIOMETRIC)
                .apply();
    }

    public void saveEncryptedToken(String token) {
        try {
            buildEncryptedPrefs().edit().putString(KEY_ENCRYPTED_ACCESS, token).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getEncryptedToken() {
        try {
            return buildEncryptedPrefs().getString(KEY_ENCRYPTED_ACCESS, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private SharedPreferences buildEncryptedPrefs() throws Exception {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
        return EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }
}
