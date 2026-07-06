package com.example.xplorenow.di;

import android.content.Context;

import com.example.xplorenow.notifications.FcmTokenRegistrar;

import dagger.hilt.android.EntryPointAccessors;

public final class FcmTokenRegistrarAccessor {

    private FcmTokenRegistrarAccessor() {
    }

    public static FcmTokenRegistrar from(Context context) {
        return EntryPointAccessors.fromApplication(
                context.getApplicationContext(),
                FcmTokenRegistrarEntryPoint.class
        ).fcmTokenRegistrar();
    }
}
