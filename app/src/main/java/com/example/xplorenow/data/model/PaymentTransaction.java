package com.example.xplorenow.data.model;

import java.io.Serializable;

public class PaymentTransaction implements Serializable {

    private String id;
    private Integer bookingId;
    private int activityId;
    private String activityTitle;
    private String bookingDate;
    private int quantity;
    private double amount;
    private String createdAt;
    private String maskedCard;
    private String status;
    private String rejectionReason;

    public PaymentTransaction(String id, Integer bookingId, int activityId, String activityTitle,
                              String bookingDate, int quantity, double amount, String createdAt,
                              String maskedCard, String status, String rejectionReason) {
        this.id = id;
        this.bookingId = bookingId;
        this.activityId = activityId;
        this.activityTitle = activityTitle;
        this.bookingDate = bookingDate;
        this.quantity = quantity;
        this.amount = amount;
        this.createdAt = createdAt;
        this.maskedCard = maskedCard;
        this.status = status;
        this.rejectionReason = rejectionReason;
    }

    public String getId() {
        return id;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public int getActivityId() {
        return activityId;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public String getMaskedCard() {
        return maskedCard;
    }

    public String getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public boolean isApproved() {
        return "APPROVED".equals(status);
    }
}
