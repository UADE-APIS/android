package com.example.xplorenow.data.network.dto;

import java.util.List;

public class UpdateProfileRequest {

    public String first_name;
    public String last_name;
    public String username;
    public String phone;
    public List<String> preferred_categories;

    public UpdateProfileRequest(String first_name, String last_name, String username, String phone, List<String> preferred_categories) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.username = username;
        this.phone = phone;
        this.preferred_categories = preferred_categories;
    }
}