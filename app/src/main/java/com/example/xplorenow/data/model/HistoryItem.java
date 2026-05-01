package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

public class HistoryItem {

    @SerializedName("id")
    private int id;

    @SerializedName("activity_id")
    private int activityId;

    @SerializedName("activity_title")
    private String activityTitle;

    @SerializedName("activity_location")
    private String activityLocation;

    @SerializedName("activity_duration")
    private int activityDuration;

    @SerializedName("assigned_guide")
    private String assignedGuide;

    @SerializedName("date")
    private String date;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("review")
    private Review review;

    @SerializedName("created_at")
    private String createdAt;

    public int getId() { return id; }
    public int getActivityId() { return activityId; }
    public String getActivityTitle() { return activityTitle; }
    public String getActivityLocation() { return activityLocation; }
    public int getActivityDuration() { return activityDuration; }
    public String getAssignedGuide() { return assignedGuide; }
    public String getDate() { return date; }
    public int getQuantity() { return quantity; }
    public Review getReview() { return review; }
    public String getCreatedAt() { return createdAt; }
}
