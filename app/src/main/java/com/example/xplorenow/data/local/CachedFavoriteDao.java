package com.example.xplorenow.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface CachedFavoriteDao {
    @Query("SELECT * FROM cached_favorites WHERE activityId = :activityId LIMIT 1")
    CachedFavorite getFavoriteByActivityId(int activityId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorite(CachedFavorite favorite);

    @Query("DELETE FROM cached_favorites WHERE activityId = :activityId")
    void deleteFavorite(int activityId);
}