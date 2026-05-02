package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ActivityImage implements Serializable {
    private int id;
    @SerializedName("image_url")
    private String imageUrl;
    @SerializedName("created_at")
    private String createdAt;

    public ActivityImage() {}

    public ActivityImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
