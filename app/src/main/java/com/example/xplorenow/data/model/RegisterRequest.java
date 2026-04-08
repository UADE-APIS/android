package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("confirm_password")
    private String confirmPassword;

    @SerializedName("username")
    private String username;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    public RegisterRequest(String email, String password, String confirmPassword,
                           String username, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getConfirmPassword() { return confirmPassword; }
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
}