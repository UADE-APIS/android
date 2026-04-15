package com.example.xplorenow.data.session;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class TokenManager {

    private static final String PREFS_NAME = "auth_tokens";
    private static final String KEY_ACCESS = "access_token";
    private static final String KEY_REFRESH = "refresh_token";

    private final SharedPreferences prefs;

    @Inject
    public TokenManager(@ApplicationContext Context context) {
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

    public void clear() {
        prefs.edit().remove(KEY_ACCESS).remove(KEY_REFRESH).apply();
    }
}
