package com.example.xplorenow.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cached_bookings")
public class CachedBooking {
    @PrimaryKey
    @NonNull
    private String id;
    private String activityTitle;
    private String date;
    private String meetingPoint;
    private String status;
    private String activityImageUrl;

    public CachedBooking(@NonNull String id, String activityTitle, String date, String meetingPoint, String status, String activityImageUrl) {
        this.id = id;
        this.activityTitle = activityTitle;
        this.date = date;
        this.meetingPoint = meetingPoint;
        this.status = status;
        this.activityImageUrl = activityImageUrl;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getActivityTitle() { return activityTitle; }
    public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getMeetingPoint() { return meetingPoint; }
    public void setMeetingPoint(String meetingPoint) { this.meetingPoint = meetingPoint; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getActivityImageUrl() { return activityImageUrl; }
    public void setActivityImageUrl(String activityImageUrl) { this.activityImageUrl = activityImageUrl; }
}
