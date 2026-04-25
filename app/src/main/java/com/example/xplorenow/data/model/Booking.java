package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

public class Booking {

    @SerializedName("id")
    private int id;

    @SerializedName("activity")
    private int activityId;

    @SerializedName("activity_detail")
    private Activity activityDetail;

    @SerializedName("availability")
    private Integer availabilityId;

    @SerializedName("availability_detail")
    private ActivityAvailability availabilityDetail;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("status")
    private String status;

    public String getStatus() { return status; }

    @SerializedName("created_at")
    private String createdAt;

    public int getId() { return id; }
    public int getActivityId() { return activityId; }
    public Activity getActivityDetail() { return activityDetail; }
    public Integer getAvailabilityId() { return availabilityId; }
    public ActivityAvailability getAvailabilityDetail() { return availabilityDetail; }
    public int getQuantity() { return quantity; }
    public String getCreatedAt() { return createdAt; }
}