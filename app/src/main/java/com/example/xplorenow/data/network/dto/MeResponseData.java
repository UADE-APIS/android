package com.example.xplorenow.data.network.dto;

import com.example.xplorenow.data.model.User;
import com.google.gson.annotations.SerializedName;

public class MeResponseData {

    @SerializedName("user")
    private User user;

    public User getUser() {
        return user;
    }
}

