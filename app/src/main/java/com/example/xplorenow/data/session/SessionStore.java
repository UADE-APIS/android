package com.example.xplorenow.data.session;

import android.content.Context;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public final class SessionStore {
    private static final Preferences.Key<String> KEY_ACCESS = PreferencesKeys.stringKey("access_token");
    private static final Preferences.Key<String> KEY_REFRESH = PreferencesKeys.stringKey("refresh_token");

    private static volatile SessionStore instance;

    private final RxDataStore<Preferences> dataStore;

    private SessionStore(Context appContext) {
        this.dataStore = new RxPreferenceDataStoreBuilder(appContext, "session").build();
    }

    public static SessionStore getInstance(Context context) {
        if (instance == null) {
            synchronized (SessionStore.class) {
                if (instance == null) {
                    instance = new SessionStore(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public Completable saveTokens(String access, String refresh) {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences prefs = prefsIn.toMutablePreferences();
            prefs.set(KEY_ACCESS, access);
            prefs.set(KEY_REFRESH, refresh);
            return Single.just(prefs);
        }).ignoreElement();
    }

    public Single<String> getAccessToken() {
        return dataStore.data()
                .firstOrError()
                .map(prefs -> prefs.get(KEY_ACCESS));
    }

    public Single<String> getRefreshToken() {
        return dataStore.data()
                .firstOrError()
                .map(prefs -> prefs.get(KEY_REFRESH));
    }

    public Flowable<Preferences> observe() {
        return dataStore.data();
    }

    public Single<Boolean> isLoggedIn() {
        return getAccessToken().map(token -> token != null && !token.trim().isEmpty());
    }

    public Completable clear() {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences prefs = prefsIn.toMutablePreferences();
            prefs.remove(KEY_ACCESS);
            prefs.remove(KEY_REFRESH);
            return Single.just(prefs);
        }).ignoreElement();
    }
}

