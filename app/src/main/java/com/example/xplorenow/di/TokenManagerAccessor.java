package com.example.xplorenow.di;

import android.content.Context;

import com.example.xplorenow.data.session.TokenManager;

import dagger.hilt.android.EntryPointAccessors;

public final class TokenManagerAccessor {

    private TokenManagerAccessor() {
    }

    public static TokenManager from(Context context) {
        return EntryPointAccessors.fromApplication(
                context.getApplicationContext(),
                TokenManagerEntryPoint.class
        ).tokenManager();
    }
}
