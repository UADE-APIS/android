package com.example.xplorenow.data.network.dto;

public class LoginClassicRequest {
    public final String email;
    public final String password;

    public LoginClassicRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
