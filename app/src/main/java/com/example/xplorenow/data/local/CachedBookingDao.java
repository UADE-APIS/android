package com.example.xplorenow.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CachedBookingDao {
    @Query("SELECT * FROM cached_bookings")
    List<CachedBooking> getAllBookings();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBookings(List<CachedBooking> bookings);

    @Query("DELETE FROM cached_bookings")
    void clearAllBookings();
}
