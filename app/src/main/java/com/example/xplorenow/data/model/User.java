package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class User {

    @SerializedName("id")
    private int id;

    @SerializedName("email")
    private String email;

    @SerializedName("username")
    private String username;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("is_verified")
    private boolean isVerified;

    @SerializedName("is_registration_completed")
    private boolean isRegistrationCompleted;

    @SerializedName("preferred_categories")
    private List<String> preferredCategories;

    @SerializedName("preferred_locations")
    private List<String> preferredLocations;

    @SerializedName("phone")
    private String phone;

    @SerializedName("profile_image_url")
    private String profileImageUrl;

    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public boolean isVerified() { return isVerified; }
    public boolean isRegistrationCompleted() { return isRegistrationCompleted; }
    public List<String> getPreferredCategories() { return preferredCategories; }
    public List<String> getPreferredLocations() { return preferredLocations; }
    public String getPhone() { return phone; }
    public String getProfileImageUrl() { return profileImageUrl; }

    public User getUser() {
        return this;
    }
}