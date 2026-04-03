package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("errors")
    private Object errors;

    public String getMessage() { return message; }
    public T getData() { return data; }
    public Object getErrors() { return errors; }
}