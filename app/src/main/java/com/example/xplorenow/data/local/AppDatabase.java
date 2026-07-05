package com.example.xplorenow.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {CachedBooking.class, CachedFavorite.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CachedBookingDao cachedBookingDao();
    public abstract CachedFavoriteDao cachedFavoriteDao();
}