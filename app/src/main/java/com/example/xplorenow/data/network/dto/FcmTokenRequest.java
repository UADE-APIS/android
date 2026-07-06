package com.example.xplorenow.data.network.dto;

public class FcmTokenRequest {
    public final String token;
    public final String platform;

    public FcmTokenRequest(String token, String platform) {
        this.token = token;
        this.platform = platform;
    }
}
