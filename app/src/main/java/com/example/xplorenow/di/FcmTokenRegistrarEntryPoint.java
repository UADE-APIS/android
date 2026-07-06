package com.example.xplorenow.di;

import com.example.xplorenow.notifications.FcmTokenRegistrar;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@EntryPoint
@InstallIn(SingletonComponent.class)
public interface FcmTokenRegistrarEntryPoint {
    FcmTokenRegistrar fcmTokenRegistrar();
}
