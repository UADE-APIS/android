package com.example.xplorenow.network.dto;

import com.google.gson.annotations.SerializedName;

public class WrappedResponse<T> {
    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public T data;
}

