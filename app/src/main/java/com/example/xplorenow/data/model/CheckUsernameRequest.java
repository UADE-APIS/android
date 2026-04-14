package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

public class CheckUsernameRequest {

    @SerializedName("username")
    private String username;

    public CheckUsernameRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}