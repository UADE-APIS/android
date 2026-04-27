package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

public class NewsActivity {

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    public int getId() { return id; }
    public String getTitle() { return title; }
}
