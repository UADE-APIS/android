package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

public class QrPayload {
    @SerializedName("availability_id")
    private Integer availabilityId;

    public Integer getAvailabilityId() {
        return availabilityId;
    }
}