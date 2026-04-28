package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class News implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("related_activity")
    private NewsActivity relatedActivity;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public NewsActivity getRelatedActivity() { return relatedActivity; }
    public boolean isActive() { return isActive; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
