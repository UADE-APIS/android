package com.example.xplorenow.network.dto;

public class LoginOtpRequest {
    public final String email;
    public final String code;

    public LoginOtpRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }
}

