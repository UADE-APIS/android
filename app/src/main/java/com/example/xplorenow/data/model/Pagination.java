package com.example.xplorenow.data.model;

import com.google.gson.annotations.SerializedName;

public class Pagination {
    private int total;
    private int page;
    @SerializedName("page_size")
    private int pageSize;
    @SerializedName("total_pages")
    private int totalPages;

    public int getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
