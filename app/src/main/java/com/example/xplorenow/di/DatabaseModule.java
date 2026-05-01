package com.example.xplorenow.di;

import android.content.Context;

import androidx.room.Room;

import com.example.xplorenow.data.local.AppDatabase;
import com.example.xplorenow.data.local.CachedBookingDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "xplorenow_db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    public CachedBookingDao provideCachedBookingDao(AppDatabase database) {
        return database.cachedBookingDao();
    }
}
