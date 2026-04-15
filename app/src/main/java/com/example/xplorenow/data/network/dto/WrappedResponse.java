package com.example.xplorenow.data.network.dto;

public class WrappedResponse<T> {
    public String message;
    private T data;
    public T getData(){
        return data;
    }
}
