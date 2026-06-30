package com.example.xplorenow.data.model;

public class PaymentResult {

    private final boolean approved;
    private final String status;
    private final String message;
    private final String maskedCard;

    public PaymentResult(boolean approved, String status, String message, String maskedCard) {
        this.approved = approved;
        this.status = status;
        this.message = message;
        this.maskedCard = maskedCard;
    }

    public boolean isApproved() {
        return approved;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getMaskedCard() {
        return maskedCard;
    }
}
