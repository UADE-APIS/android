package com.example.xplorenow.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Activity implements Serializable {
    private int id;
    private String title;
    private String description;
    private String location;
    private String category;
    private String price;
    private int duration;
    private int capacity;
    private String includes;
    
    @SerializedName("meeting_point")
    private String meetingPoint;
    
    private String language;
    
    @SerializedName("cancellation_policy")
    private String cancellationPolicy;
    
    @SerializedName("assigned_guide")
    private String assignedGuide;
    
    @SerializedName("is_featured")
    private boolean isFeatured;
    
    @SerializedName("is_active")
    private boolean isActive;
    
    private List<String> images;
    
    @SerializedName("available_slots")
    private int availableSlots;
    
    @SerializedName("reserved_slots")
    private int reservedSlots;

    // Getters and Setters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }
    public String getPrice() { return price; }
    public int getDuration() { return duration; }
    public int getCapacity() { return capacity; }
    public String getIncludes() { return includes; }
    public String getMeetingPoint() { return meetingPoint; }
    public String getLanguage() { return language; }
    public String getCancellationPolicy() { return cancellationPolicy; }
    public String getAssignedGuide() { return assignedGuide; }
    public boolean isFeatured() { return isFeatured; }
    public boolean isActive() { return isActive; }
    public List<String> getImages() { return images; }
    public int getAvailableSlots() { return availableSlots; }
    public int getReservedSlots() { return reservedSlots; }
}
