package com.example.xplorenow.network.dto.auth;

import com.google.gson.annotations.SerializedName;

public class UserDto {
    @SerializedName("id")
    public int id;

    @SerializedName("email")
    public String email;

    @SerializedName("username")
    public String username;

    @SerializedName("first_name")
    public String firstName;

    @SerializedName("last_name")
    public String lastName;
}

