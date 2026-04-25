package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

public class BookingRequest {

    @SerializedName("activity")
    private int activity;

    @SerializedName("availability")
    private Integer availability; // Puede ser null si la actividad no tiene fechas específicas

    @SerializedName("quantity")
    private int quantity;

    public BookingRequest(int activity, Integer availability, int quantity) {
        this.activity = activity;
        this.availability = availability;
        this.quantity = quantity;
    }

    public int getActivity() { return activity; }
    public Integer getAvailability() { return availability; }
    public int getQuantity() { return quantity; }
}