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

    public void setStatus(String status) {
        this.status = status;
    }

    @SerializedName("created_at")
    private String createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getActivityId() { return activityId; }
    public void setActivityId(int activityId) { this.activityId = activityId; }
    public Activity getActivityDetail() { return activityDetail; }
    public void setActivityDetail(Activity activityDetail) { this.activityDetail = activityDetail; }
    public Integer getAvailabilityId() { return availabilityId; }
    public void setAvailabilityId(Integer availabilityId) { this.availabilityId = availabilityId; }
    public ActivityAvailability getAvailabilityDetail() { return availabilityDetail; }
    public void setAvailabilityDetail(ActivityAvailability availabilityDetail) { this.availabilityDetail = availabilityDetail; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getDate() {
        if (availabilityDetail != null && availabilityDetail.getDate() != null) {
            return availabilityDetail.getDate();
        }
        return createdAt;
    }
}