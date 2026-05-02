package com.example.xplorenow.data.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.List;

public class BookingsListResponse {

    private static final Gson GSON = new Gson();

    private static final Type BOOKING_LIST_TYPE =
            new com.google.gson.reflect.TypeToken<List<Booking>>() {}.getType();

    private String message;

    @SerializedName("data")
    private JsonElement data;

    public String getMessage() {
        return message;
    }

    public List<Booking> getResults() {
        if (data == null || data.isJsonNull()) {
            return null;
        }

        if (data.isJsonArray()) {
            return GSON.fromJson(data, BOOKING_LIST_TYPE);
        }

        JsonElement results = data.getAsJsonObject().get("results");
        if (results != null && results.isJsonArray()) {
            return GSON.fromJson(results, BOOKING_LIST_TYPE);
        }

        return null;
    }

    public Pagination getPagination() {
        if (data == null || data.isJsonNull() || !data.isJsonObject()) {
            return null;
        }

        JsonElement pagination = data.getAsJsonObject().get("pagination");
        if (pagination == null || pagination.isJsonNull()) {
            return null;
        }

        return GSON.fromJson(pagination, Pagination.class);
    }
}
