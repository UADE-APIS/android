package com.example.xplorenow.network.dto.auth;

import com.google.gson.annotations.SerializedName;

public class AuthTokensResponse {
    @SerializedName("refresh")
    public String refresh;

    @SerializedName("access")
    public String access;

    @SerializedName("user")
    public UserDto user;
}

