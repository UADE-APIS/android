package com.example.xplorenow.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cached_favorites")
public class CachedFavorite {
    @PrimaryKey
    private int activityId;
    private double lastSeenPrice;
    private int lastSeenSlots;

    public CachedFavorite(int activityId, double lastSeenPrice, int lastSeenSlots) {
        this.activityId = activityId;
        this.lastSeenPrice = lastSeenPrice;
        this.lastSeenSlots = lastSeenSlots;
    }

    public int getActivityId() { return activityId; }
    public void setActivityId(int activityId) { this.activityId = activityId; }

    public double getLastSeenPrice() { return lastSeenPrice; }
    public void setLastSeenPrice(double lastSeenPrice) { this.lastSeenPrice = lastSeenPrice; }

    public int getLastSeenSlots() { return lastSeenSlots; }
    public void setLastSeenSlots(int lastSeenSlots) { this.lastSeenSlots = lastSeenSlots; }
}