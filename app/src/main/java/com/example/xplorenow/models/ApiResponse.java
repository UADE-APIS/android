package com.example.xplorenow.models;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    private String message;
    private DataWrapper<T> data;

    public String getMessage() {
        return message;
    }

    public T getData() {
        return (data != null) ? data.results : null;
    }

    private static class DataWrapper<T> {
        @SerializedName("results")
        private T results;
    }
}
