package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

public class ActivityAvailability {

    @SerializedName("id")
    private int id;

    @SerializedName("date")
    private String date;

    @SerializedName("capacity")
    private int capacity;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("reserved_slots")
    private int reservedSlots;

    @SerializedName("available_slots")
    private int availableSlots;

    public int getId() { return id; }
    public String getDate() { return date; }
    public int getCapacity() { return capacity; }
    public boolean isActive() { return isActive; }
    public int getReservedSlots() { return reservedSlots; }
    public int getAvailableSlots() { return availableSlots; }
}