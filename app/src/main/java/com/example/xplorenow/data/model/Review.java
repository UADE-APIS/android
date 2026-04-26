package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

public class Review {

    @SerializedName("id")
    private int id;

    @SerializedName("activity_rating")
    private int activityRating;

    @SerializedName("guide_rating")
    private int guideRating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("created_at")
    private String createdAt;

    public int getId() { return id; }
    public int getActivityRating() { return activityRating; }
    public int getGuideRating() { return guideRating; }
    public String getComment() { return comment; }
    public String getCreatedAt() { return createdAt; }
}
