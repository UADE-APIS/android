package com.example.xplorenow.di;

import com.example.xplorenow.data.session.TokenManager;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@EntryPoint
@InstallIn(SingletonComponent.class)
public interface TokenManagerEntryPoint {
    TokenManager tokenManager();
}
