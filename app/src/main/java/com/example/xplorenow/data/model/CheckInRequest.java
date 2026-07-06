package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

public class CheckInRequest {
    @SerializedName("availability_id")
    private Integer availabilityId;

    public CheckInRequest(Integer availabilityId) {
        this.availabilityId = availabilityId;
    }

    public Integer getAvailabilityId() {
        return availabilityId;
    }
}