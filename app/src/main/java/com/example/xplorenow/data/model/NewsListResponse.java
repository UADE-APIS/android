package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NewsListResponse {

    private String message;

    @SerializedName("data")
    private DataBlock data;

    public String getMessage() { return message; }

    public List<News> getResults() {
        return data != null ? data.results : null;
    }

    public Pagination getPagination() {
        return data != null ? data.pagination : null;
    }

    private static class DataBlock {
        @SerializedName("results")
        private List<News> results;
        @SerializedName("pagination")
        private Pagination pagination;
    }
}
