package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

public class VerifyOtpData {

    @SerializedName("user")
    private User user;

    public User getUser() { return user; }
}