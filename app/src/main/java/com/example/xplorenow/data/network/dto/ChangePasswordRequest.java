package com.example.xplorenow.data.network.dto;

public class ChangePasswordRequest {
    public String old_password;
    public String current_password;
    public String new_password;
    public String confirm_password;
    public String new_password1;
    public String new_password2;

    public ChangePasswordRequest(String old_password, String new_password) {
        this.old_password = old_password;
        this.current_password = old_password;
        this.new_password = new_password;
        this.confirm_password = new_password;
        this.new_password1 = new_password;
        this.new_password2 = new_password;
    }
}