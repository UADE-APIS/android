package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

public class ReviewRequest {

    @SerializedName("activity_rating")
    private final int activityRating;

    @SerializedName("guide_rating")
    private final int guideRating;

    @SerializedName("comment")
    private final String comment;

    public ReviewRequest(int activityRating, int guideRating, String comment) {
        this.activityRating = activityRating;
        this.guideRating = guideRating;
        this.comment = comment;
    }
}
