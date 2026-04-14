package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

public class VerifyOtpRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("code")
    private String code;

    public VerifyOtpRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }
}