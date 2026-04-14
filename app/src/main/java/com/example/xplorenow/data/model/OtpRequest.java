package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

public class OtpRequest {

    @SerializedName("email")
    private String email;

    public OtpRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}