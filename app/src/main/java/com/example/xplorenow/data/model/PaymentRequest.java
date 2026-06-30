package com.example.xplorenow.data.model;

public class PaymentRequest {

    private final String activityTitle;
    private final String bookingDate;
    private final int quantity;
    private final double amount;
    private final PaymentCard card;

    public PaymentRequest(String activityTitle, String bookingDate, int quantity, double amount, PaymentCard card) {
        this.activityTitle = activityTitle;
        this.bookingDate = bookingDate;
        this.quantity = quantity;
        this.amount = amount;
        this.card = card;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentCard getCard() {
        return card;
    }
}
