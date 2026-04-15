package com.example.xplorenow.data.network.dto;

public class UpdateProfileRequest {

    public String first_name;
    public String last_name;
    public String username;

    public UpdateProfileRequest(String first_name, String last_name, String username) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.username = username;
    }
}