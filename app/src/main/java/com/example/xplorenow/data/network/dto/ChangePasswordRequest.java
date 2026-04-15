package com.example.xplorenow.data.network.dto;

public class ChangePasswordRequest {
    public String old_password;
    public String new_password;

    public ChangePasswordRequest(String old_password, String new_password) {
        this.old_password = old_password;
        this.new_password = new_password;
    }
}